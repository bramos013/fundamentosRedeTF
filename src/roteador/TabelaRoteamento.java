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
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class TabelaRoteamento {
    private final List<RegistroTabelaRoteamento> routes;

    public TabelaRoteamento() {
        this.routes = new ArrayList<>();
    }

    public void inicializaTabela(List<String> neighbors) {
        neighbors.forEach((vizinho) -> {
            // cadastra os endereços IPs vizinhos em uma tabela de roteamento com métrica 1 e saída direta
            this.routes.add(new RegistroTabelaRoteamento(vizinho, 1, vizinho));
        });
    }

    public void atualizaTabela(String tableStr, InetAddress IPAddress, AtomicBoolean tableWasChanged) {
        List<RegistroTabelaRoteamento> receivedRoutes = getStringParaTabela(tableStr, IPAddress);
        BiPredicate<RegistroTabelaRoteamento, RegistroTabelaRoteamento> compareRoutesByDestinationIp = (route1, route2) -> route1.getIpDestino().equals(route2.getIpDestino());

        // Para cada registro recebido do vizinho
        receivedRoutes.forEach(receivedRoute -> {
            Optional<RegistroTabelaRoteamento> routeByDestinationIp = routes.stream()
                    .filter(route -> compareRoutesByDestinationIp.test(route, receivedRoute))
                    .findFirst();
            // Adiciona rota se o IP de destino recebido nao esta na tabela local
            if (routeByDestinationIp.isEmpty()) {
                routes.add(receivedRoute);
                tableWasChanged.set(true);
                // Atualiza metrica e saida se for recebida metrica menor para um IP destino presente na tabela
            } else if (routeByDestinationIp.get().getMetrica() > receivedRoute.getMetrica()) {
                RegistroTabelaRoteamento foundRoute = routeByDestinationIp.get();
                foundRoute.setMetrica(receivedRoute.getMetrica());
                foundRoute.setIpSaida(receivedRoute.getIpSaida());
                tableWasChanged.set(true);
            }
        });

        // Retira rotas se IP destino deixar de ser divulgado
        List<RegistroTabelaRoteamento> routesToRemove = routes.stream().filter(route -> {
            // se rota presente na tabela possuia este vizinho como saida && o vizinho nao divulgou mais alguma rota com este destino
            return route.getIpSaida().equals(IPAddress.getHostAddress())
                    && receivedRoutes.stream().noneMatch(receivedRoute -> compareRoutesByDestinationIp.test(receivedRoute, route));
        }).collect(Collectors.toList());

        if (!routesToRemove.isEmpty()) {
            routes.removeAll(routesToRemove);
            tableWasChanged.set(true);
        }
    }

    public String getTabelaComoString() {
        /* Tabela de roteamento vazia conforme especificado no protocolo */
        if (routes.isEmpty())
            return "!";
        StringBuilder tableStrBuilder = new StringBuilder();
        routes.forEach((route) -> {
            tableStrBuilder.append("*").append(route.getIpDestino()).append(";").append(route.getMetrica().toString());
        });

        return tableStrBuilder.toString();
    }

    public List<RegistroTabelaRoteamento> getStringParaTabela(String tableStr, InetAddress IPAddress) {
        String[] routesStr = tableStr.split("\\*");
        return Arrays.stream(routesStr).map(route -> {
            String[] routeFields = route.split(";");
            return new RegistroTabelaRoteamento(routeFields[0], Integer.valueOf(routeFields[1]), IPAddress.getHostAddress());
        }).collect(Collectors.toList());
    }
}
