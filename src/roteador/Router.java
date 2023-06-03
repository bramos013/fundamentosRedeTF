/*
 * Trabalho Final da Disciplina de Fundamentos de Redes de Computadores
 * Grupo: Bruno Ramos, Fernanda Mello, Lucas Salbego, Matheus Pozzer
 */
package roteador;

import roteador.socket.MessageReceiver;
import roteador.socket.MessageSender;
import roteador.util.FileReader;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Router {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("ERRO: endereço IP da maquina deve ser informado por parametro");
            System.exit(1);
        }
        String ipAddress = args[0];
        /* Le arquivo de entrada com lista de enderecos IPs dos roteadores vizinhos. */
        List<String> neighborIpList = FileReader.readLinesFromFile("IPVizinhos.txt");

        /* Cria variavel AtomicBoolean para que ambas threads validem e atualizem
        se existem alteracoes na tabela nao enviadas para os vizinhos */
        AtomicBoolean tableWasChanged = new AtomicBoolean();

        /* Cria instâncias da tabela de roteamento e das threads de envio e recebimento de mensagens. */
        RoutingTable routingTable = new RoutingTable(ipAddress, neighborIpList);

        Thread sender = new Thread(new MessageReceiver(routingTable, neighborIpList, tableWasChanged));
        Thread receiver = new Thread(new MessageSender(routingTable, neighborIpList, tableWasChanged));

        sender.start();
        receiver.start();
    }
}
