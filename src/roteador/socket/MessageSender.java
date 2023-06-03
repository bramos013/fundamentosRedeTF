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
    private TabelaRoteamento routingTable; /*Tabela de roteamento */
    private List<String> neighbors; /* Lista de IPs dos roteadores vizinhos */
    private AtomicBoolean tableWasChanged;

    public MessageSender(TabelaRoteamento routingTable, List<String> neighbors, AtomicBoolean tableWasChanged) {
        this.routingTable = routingTable;
        this.neighbors = neighbors;
        this.tableWasChanged = tableWasChanged;
    }

    @Override
    public void run() {
        byte[] dataToSend;
        InetAddress ipAddress = null;

        try (DatagramSocket clientSocket = new DatagramSocket()) {
            boolean firstIteration = true;
            clientSocket.setSoTimeout(30000);

            while (true) {
                /* Pega a tabela de roteamento no formato string, conforme especificado pelo protocolo. */
                String routingTableAsString = this.routingTable.getTableAsString();
                /* Converte string para array de bytes para envio pelo socket. */
                dataToSend = routingTableAsString.getBytes();

                if (firstIteration) {
                    System.out.println("Initializing my routing table with neighbor routes");
                    this.routingTable.initializeTable(neighbors);
                    firstIteration = false;
                }

                /* Anuncia a tabela de roteamento para cada um dos vizinhos */
                for (String ip : neighbors) {
                    try {
                        /* Converte string com o IP do vizinho para formato InetAddress */
                        ipAddress = InetAddress.getByName(ip);

                        /* Configura pacote para envio da mensagem para o roteador vizinho na porta 5000 */
                        DatagramPacket sendPacket = new DatagramPacket(dataToSend, dataToSend.length, ipAddress, 5000);
                        System.out.println("SENDING MESSAGE TO IP ADDRESS " + ipAddress + ": " + routingTableAsString);
                        clientSocket.send(sendPacket);
                    } catch (IOException ex) {
                        Logger.getLogger(MessageSender.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                System.out.println(routingTable.toString());

                /* Espera 10 segundos antes de realizar o próximo envio. CONTUDO, caso
                 * a tabela de roteamento sofra uma alteração, envia aos
                 * vizinhoas imediatamente.
                 */
                if (tableWasChanged.get()) {
                    System.out.println("My routing table was changed. Must send message to neighbors immediately");
                    tableWasChanged.compareAndSet(true, false);
                } else {
                    System.out.println("Sleeping before next send...");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MessageSender.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(MessageSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
