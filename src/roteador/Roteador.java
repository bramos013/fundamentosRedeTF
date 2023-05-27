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

public class Roteador {

    public static void main(String[] args) throws IOException {
        /* Le arquivo de entrada com lista de enderecos IPs dos roteadores vizinhos. */
        List<String> ipList = FileReader.readLinesFromFile("IPVizinhos.txt");

        /* Cria variavel AtomicBoolean para que ambas threads validem e atualizem
        se existem alteracoes na tabela nao enviadas para os vizinhos */
        AtomicBoolean existeAlteracaoTabela = new AtomicBoolean();

        /* Cria instâncias da tabela de roteamento e das threads de envio e recebimento de mensagens. */
        TabelaRoteamento tabela = new TabelaRoteamento();
        tabela.inicializaTabela(ipList);

        Thread sender = new Thread(new MessageReceiver(tabela, existeAlteracaoTabela));
        Thread receiver = new Thread(new MessageSender(tabela, ipList, existeAlteracaoTabela));

        sender.start();
        receiver.start();

    }
}
