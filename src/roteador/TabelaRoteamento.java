/*
 * Trabalho Final da Disciplina de Fundamentos de Redes de Computadores
 * Grupo: Bruno Ramos, Fernanda Mello, Lucas Salbego, Matheus Pozzer
 */
package roteador;

import roteador.dto.RegistroTabelaRoteamento;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class TabelaRoteamento {
    private final List<RegistroTabelaRoteamento> registros;

    public TabelaRoteamento() {
        this.registros = new ArrayList<>();
    }

    public void inicializaTabela(List<String> vizinhos) {
        vizinhos.forEach((vizinho) -> {
            // cadastra os endereços IPs vizinhos em uma tabela de roteamento com métrica 1 e saída direta
            this.registros.add(new RegistroTabelaRoteamento(vizinho, 1, vizinho));
        });
    }

    /*
    * TODO Uma atualização deverá ser feita sempre que:
    *  - for recebido um IP de Destino não presente na tabela local. Neste caso a rota deve ser adicionada,
    *    a Métrica deve ser incrementada em 1 e o IP de Saída deve ser o endereço do roteador que ensinou esta informação
    *  - for recebida uma Métrica menor para um IP Destino presente na tabela local. Neste caso,a Métrica e o IP de Saída devem ser atualizadas
    *  - um IP Destino deixar de ser divulgado. Neste caso, a rota deve ser retirada da tabela de roteamento
    * */
    public void atualizaTabela(String tabela_s, InetAddress IPAddress) {
        /* Atualize a tabela de rotamento a partir da string recebida. */
        System.out.println(IPAddress.getHostAddress() + ": " + tabela_s);
    }

    public String getTabelaComoString() {
        // TODO logo que um roteador entrar na rede e não tiver nenhuma rota pré-configurada, deverá anunciar-se para os vizinhos com a msg !
        String tabelaStr = "!"; /* Tabela de roteamento vazia conforme especificado no protocolo */
        StringBuilder tabelaStrBuilder = new StringBuilder();
        registros.forEach((registro) -> {
            tabelaStrBuilder.append("*").append(registro.getIpDestino()).append(";").append(registro.getMetrica().toString());
        });

        return tabelaStrBuilder.toString();
    }
}
