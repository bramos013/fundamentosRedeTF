package roteador;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Roteador {

    public static void main(String[] args) throws IOException {
        /* Lista de endereço IPs dos vizinhos */
        ArrayList<String> ipList = new ArrayList<>();

        /* Le arquivo de entrada com lista de IPs dos roteadores vizinhos. */
        try (BufferedReader inputFile = new BufferedReader(new FileReader("IPVizinhos.txt"))) {
            String ip;

            while ((ip = inputFile.readLine()) != null) {
                ipList.add(ip);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Roteador.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

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
