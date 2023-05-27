/*
 * Trabalho Final da Disciplina de Fundamentos de Redes de Computadores
 * Grupo: Bruno Ramos, Fernanda Mello, Lucas Salbego, Matheus Pozzer
 */
package roteador;

import roteador.dto.RegistroTabelaRoteamento;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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

    public void atualizaTabela(String tabela_s, InetAddress IPAddress, AtomicBoolean existeAlteracaoTabela) {
        List<RegistroTabelaRoteamento> registrosRecebidos = getStringParaTabela(tabela_s, IPAddress);
        registrosRecebidos.forEach(registroRecebido -> {
            Optional<RegistroTabelaRoteamento> routeByDestinationIp = registros.stream()
                    .filter(registro -> registro.getIpDestino().equals(registroRecebido.getIpDestino()))
                    .findFirst();
            // Adiciona rota se o IP de destino recebido nao esta na tabela local
            if (routeByDestinationIp.isEmpty()) {
                registros.add(registroRecebido);
                existeAlteracaoTabela.set(true);
            // Atualiza metrica e saida se for recebida metrica menor para um IP destino presente na tabela
            } else if (routeByDestinationIp.get().getMetrica() > registroRecebido.getMetrica()) {
                RegistroTabelaRoteamento foundRoute = routeByDestinationIp.get();
                foundRoute.setMetrica(registroRecebido.getMetrica());
                foundRoute.setIpSaida(registroRecebido.getIpSaida());
            }
        });

        // Retira rotas se IP destino deixar de ser divulgado
        List<RegistroTabelaRoteamento> registrosARemover = registros.stream().filter(registro -> {
            // se rota presente na tabela possuia este vizinho como saida && o vizinho nao divulgou mais alguma rota com este destino
            return registro.getIpSaida().equals(IPAddress.getHostAddress())
                    && registrosRecebidos.stream().noneMatch(registroRecebido -> registroRecebido.getIpDestino().equals(registro.getIpDestino()));
        }).collect(Collectors.toList());

        if (!registrosARemover.isEmpty()) {
            registros.removeAll(registrosARemover);
            existeAlteracaoTabela.set(true);
        }
    }

    public void removeRegistrosPorIP(InetAddress IPtoRemove, AtomicBoolean existeAlteracaoTabela){
        boolean hasRemoved = registros.removeIf((registro) -> {
            return IPtoRemove.getHostAddress().equals(registro.getIpSaida());
        });
        if (hasRemoved){existeAlteracaoTabela.set(true);}
    }

    public String getTabelaComoString() {
        /* Tabela de roteamento vazia conforme especificado no protocolo */
        if (registros.isEmpty())
            return "!";
        StringBuilder tabelaStrBuilder = new StringBuilder();
        registros.forEach((registro) -> {
            tabelaStrBuilder.append("*").append(registro.getIpDestino()).append(";").append(registro.getMetrica().toString());
        });

        return tabelaStrBuilder.toString();
    }

    public List<RegistroTabelaRoteamento> getStringParaTabela(String tabela_s, InetAddress IPAddress) {
        String[] registrosTabelaStr = tabela_s.split("\\*");
        return Arrays.stream(registrosTabelaStr).map(registro -> {
            String[] registroCampos = registro.split(";");
            return new RegistroTabelaRoteamento(registroCampos[0], Integer.valueOf(registroCampos[1]), IPAddress.getHostAddress());
        }).collect(Collectors.toList());
    }
}
