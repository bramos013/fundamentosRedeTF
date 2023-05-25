package roteador;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageReceiver implements Runnable {
    private TabelaRoteamento tabela;
    private AtomicBoolean existeAlteracaoTabela;

    public MessageReceiver(TabelaRoteamento tabela, AtomicBoolean existeAlteracaoTabela) {
        this.tabela = tabela;
        this.existeAlteracaoTabela = existeAlteracaoTabela;
    }

    @Override
    public void run() {
        try (DatagramSocket serverSocket = new DatagramSocket(5000)) {
            byte[] receiveData = new byte[1024];

            while (true) {
                /* Cria um DatagramPacket */
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                try {
                    /* Aguarda o recebimento de uma mensagem */
                    serverSocket.receive(receivePacket);
                } catch (IOException ex) {
                    Logger.getLogger(MessageReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }

                /* Transforma a mensagem em string */
                String tabela_string = new String(receivePacket.getData());

                /* Obtem o IP de origem da mensagem */
                InetAddress IPAddress = receivePacket.getAddress();

                tabela.atualizaTabela(tabela_string, IPAddress);
            }
        } catch (SocketException ex) {
            Logger.getLogger(MessageReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
