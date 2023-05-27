/*
* Trabalho Final da Disciplina de Fundamentos de Redes de Computadores
* Grupo: Bruno Ramos, Fernanda Mello, Lucas Salbego, Matheus Pozzer
*/
package roteador.socket;

import roteador.TabelaRoteamento;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageReceiver implements Runnable {
    private TabelaRoteamento tabelaRoteamento;
    private AtomicBoolean existeAlteracaoTabela;

    public MessageReceiver(TabelaRoteamento tabelaRoteamento, AtomicBoolean existeAlteracaoTabela) {
        this.tabelaRoteamento = tabelaRoteamento;
        this.existeAlteracaoTabela = existeAlteracaoTabela;
    }

    @Override
    public void run() {
        HashMap<InetAddress, Long> connections = new HashMap<>();

        try (DatagramSocket serverSocket = new DatagramSocket(5000)) {
            byte[] receiveData = new byte[1024];

            while (true) {
                /* Cria um DatagramPacket */

                connections.forEach((ip, lastConnection) -> {
                    Long currentTime = System.currentTimeMillis();
                    if(((lastConnection - currentTime)/1000F) >= 30){
                        tabelaRoteamento.removeRegistrosPorIP(ip);
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
                connections.put(IPAddress, System.currentTimeMillis());

                tabelaRoteamento.atualizaTabela(tabela_string, IPAddress, existeAlteracaoTabela);

                /* TODO Periodicamente, a tabela de roteamento local deverá ser apresentada para o usuário. Além
                *   disso, alterações na tabela de roteamento deverão ser informadas para os usuários (através de prints)
                */
            }
        } catch (SocketException ex) {
            Logger.getLogger(MessageReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
