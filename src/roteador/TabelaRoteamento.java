package roteador;

import roteador.dto.RegistroTabelaRoteamento;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class TabelaRoteamento {
    /*Implemente uma estrutura de dados para manter a tabela de roteamento.
     * A tabela deve possuir: IP Destino, Métrica e IP de Saída.
     */
    private final List<RegistroTabelaRoteamento> registros;

    public TabelaRoteamento() {
        this.registros = new ArrayList<>();
    }

    public void inicializaTabela(List<String> vizinhos) {
        vizinhos.forEach((vizinho) -> {
            // cadastra os endereços IPs em uma tabela de roteamento com métrica 1 e saída direta
            this.registros.add(new RegistroTabelaRoteamento(vizinho, 1, vizinho));
        });
    }

    public void atualizaTabela(String tabela_s, InetAddress IPAddress) {
        /* Atualize a tabela de rotamento a partir da string recebida. */

        System.out.println(IPAddress.getHostAddress() + ": " + tabela_s);

    }

    public String getTabelaComoString() {
        String tabelaStr = "!"; /* Tabela de roteamento vazia conforme especificado no protocolo */
        StringBuilder tabelaStrBuilder = new StringBuilder();
        registros.forEach((registro) -> {
            tabelaStrBuilder.append("*").append(registro.getIpDestino()).append(";").append(registro.getMetrica().toString());
        });

        return tabelaStrBuilder.toString();
    }
}
