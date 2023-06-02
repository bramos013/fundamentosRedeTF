/*
 * Trabalho Final da Disciplina de Fundamentos de Redes de Computadores
 * Grupo: Bruno Ramos, Fernanda Mello, Lucas Salbego, Matheus Pozzer
 */
package roteador.socket;

import roteador.TabelaRoteamento;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageReceiver implements Runnable {
    private TabelaRoteamento tabelaRoteamento;
    private List<String> vizinhos; /* Lista de IPs dos roteadores vizinhos */
    private AtomicBoolean existeAlteracaoTabela;

    public MessageReceiver(TabelaRoteamento tabelaRoteamento, List<String> vizinhos, AtomicBoolean existeAlteracaoTabela) {
        this.tabelaRoteamento = tabelaRoteamento;
        this.vizinhos = vizinhos;
        this.existeAlteracaoTabela = existeAlteracaoTabela;
    }

    @Override
    public void run() {
        Map<String, Long> connections = new HashMap<>();
        inicializaConexoesDeVizinhos(connections);

        try (DatagramSocket serverSocket = new DatagramSocket(5000)) {
            byte[] receiveData = new byte[1024];

            while (true) {
                /* Cria um DatagramPacket */

                connections.forEach((ip, lastConnection) -> {
                    Long currentTime = System.currentTimeMillis();
                    if(((lastConnection - currentTime)/1000F) >= 30){
                        tabelaRoteamento.removeRegistrosPorIP(ip, existeAlteracaoTabela);
                    }
                });

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    /* Reseta o tamanho dos dados do pacote antes de receber */
                    receivePacket.setLength(receiveData.length);
                    /* Aguarda o recebimento de uma mensagem */
                    serverSocket.receive(receivePacket);

                } catch (IOException ex) {
                    Logger.getLogger(MessageReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }

                /* Transforma a mensagem em string */
                String tabela_string = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());

                /* Obtem o IP de origem da mensagem */
                InetAddress IPAddress = receivePacket.getAddress();
                connections.put(IPAddress.getHostAddress(), System.currentTimeMillis());

                tabelaRoteamento.atualizaTabela(tabela_string, IPAddress, existeAlteracaoTabela);
                System.out.println(tabelaRoteamento.toString());
            }
        } catch (SocketException ex) {
            Logger.getLogger(MessageReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void inicializaConexoesDeVizinhos(Map<String, Long> connections) {
        vizinhos.forEach(vizinho -> connections.put(vizinho, System.currentTimeMillis()));
    }
}
