package roteador;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageSender implements Runnable {
    TabelaRoteamento tabela; /*Tabela de roteamento */
    ArrayList<String> vizinhos; /* Lista de IPs dos roteadores vizinhos */

    public MessageSender(TabelaRoteamento t, ArrayList<String> v) {
        tabela = t;
        vizinhos = v;
    }

    @Override
    public void run() {
        byte[] sendData;
        InetAddress ipAddress = null;

        /* Cria socket para envio de mensagem */
        try (DatagramSocket clientSocket = new DatagramSocket()) {
            while (true) {
                /* Pega a tabela de roteamento no formato string, conforme especificado pelo protocolo. */
                String tabelaStr = tabela.getTabelaComoString();

                /* Converte string para array de bytes para envio pelo socket. */
                sendData = tabelaStr.getBytes();

                /* Anuncia a tabela de roteamento para cada um dos vizinhos */
                for (String ip : vizinhos) {
                    /* Converte string com o IP do vizinho para formato InetAddress */
                    try {
                        ipAddress = InetAddress.getByName(ip);
                    } catch (UnknownHostException ex) {
                        Logger.getLogger(MessageSender.class.getName()).log(Level.SEVERE, null, ex);
                        continue;
                    }

                    /* Configura pacote para envio da menssagem para o roteador vizinho na porta 5000*/
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, 5000);

                    /* Realiza envio da mensagem. */
                    try {
                        clientSocket.send(sendPacket);
                    } catch (IOException ex) {
                        Logger.getLogger(MessageSender.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                /* Espera 10 segundos antes de realizar o próximo envio. CONTUDO, caso
                 * a tabela de roteamento sofra uma alteração, ela deve ser reenvida aos
                 * vizinho imediatamente.
                 */
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MessageSender.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (SocketException ex) {
            Logger.getLogger(MessageSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}