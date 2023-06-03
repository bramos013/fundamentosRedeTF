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
    private TabelaRoteamento routingTable;
    private List<String> neighbors; /* Lista de IPs dos roteadores vizinhos */
    private AtomicBoolean tableWasChanged;

    public MessageReceiver(TabelaRoteamento routingTable, List<String> neighbors, AtomicBoolean tableWasChanged) {
        this.routingTable = routingTable;
        this.neighbors = neighbors;
        this.tableWasChanged = tableWasChanged;
    }

    @Override
    public void run() {
        Map<String, Long> connections = new HashMap<>();
        initializeNeighborConnections(connections);

        try (DatagramSocket serverSocket = new DatagramSocket(5000)) {
            byte[] receiveData = new byte[1024];
            serverSocket.setSoTimeout(30000);

            while (true) {
                connections.forEach((ip, lastConnection) -> {
                    Long currentTime = System.currentTimeMillis();
                    Float seconds = ((currentTime - lastConnection)/1000F);
                    if(seconds >= 30) {
                        System.out.println("Removing routes from neighbor " + ip + ". Stopped received routes from him for " + seconds + " seconds");
                        routingTable.removeRoutesByOutputIPAddress(ip, tableWasChanged);
                    }
                });

                /* Cria um DatagramPacket */
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    /* Reseta o tamanho dos dados do pacote antes de receber */
                    receivePacket.setLength(receiveData.length);
                    /* Aguarda o recebimento de uma mensagem */
                    serverSocket.receive(receivePacket);

                    /* Transforma a mensagem em string */
                    String receivedString = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());

                    /* Obtem o IP de origem da mensagem */
                    InetAddress IPAddress = receivePacket.getAddress();
                    connections.put(IPAddress.getHostAddress(), System.currentTimeMillis());

                    System.out.println("RECEIVED MESSAGE FROM IP ADDRESS " + IPAddress.getHostAddress() + ": " + receivedString);

                    routingTable.updateTable(receivedString, IPAddress, tableWasChanged);

                    System.out.println(routingTable.toString());

                } catch (IOException ex) {
                    Logger.getLogger(MessageReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(MessageReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initializeNeighborConnections(Map<String, Long> connections) {
        neighbors.forEach(neighbor -> connections.put(neighbor, System.currentTimeMillis()));
    }
}
