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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageSender implements Runnable {
    private TabelaRoteamento tabelaRoteamento; /*Tabela de roteamento */
    private List<String> vizinhos; /* Lista de IPs dos roteadores vizinhos */
    private AtomicBoolean existeAlteracaoTabela;

    public MessageSender(TabelaRoteamento tabelaRoteamento, List<String> vizinhos, AtomicBoolean existeAlteracaoTabela) {
        this.tabelaRoteamento = tabelaRoteamento;
        this.vizinhos = vizinhos;
        this.existeAlteracaoTabela = existeAlteracaoTabela;
    }

    @Override
    public void run() {
        byte[] dataToSend;
        InetAddress ipAddress = null;

        try (DatagramSocket clientSocket = new DatagramSocket()) {
            boolean firstIteration = true;
            while (true) {
                /* Pega a tabela de roteamento no formato string, conforme especificado pelo protocolo. */
                String routingTable = tabelaRoteamento.getTabelaComoString();

                /* Converte string para array de bytes para envio pelo socket. */
                dataToSend = routingTable.getBytes();

                /* Anuncia a tabela de roteamento para cada um dos vizinhos */
                for (String ip : vizinhos) {
                    try {
                        /* Converte string com o IP do vizinho para formato InetAddress */
                        ipAddress = InetAddress.getByName(ip);

                        /* Configura pacote para envio da mensagem para o roteador vizinho na porta 5000 */
                        DatagramPacket sendPacket = new DatagramPacket(dataToSend, dataToSend.length, ipAddress, 5000);
                        clientSocket.send(sendPacket);
                    } catch (IOException ex) {
                        Logger.getLogger(MessageSender.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                /* Espera 10 segundos antes de realizar o próximo envio. CONTUDO, caso
                 * a tabela de roteamento sofra uma alteração, envia aos
                 * vizinhoas imediatamente.
                 */
                if (existeAlteracaoTabela.get()) {
                    existeAlteracaoTabela.compareAndSet(true, false);
                } else {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MessageSender.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (firstIteration) {
                    tabelaRoteamento.inicializaTabela(vizinhos);
                    firstIteration = false;
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(MessageSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
