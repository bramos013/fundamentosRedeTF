/*
 * Trabalho Final da Disciplina de Fundamentos de Redes de Computadores
 * Grupo: Bruno Ramos, Fernanda Mello, Lucas Salbego, Matheus Pozzer
 */
package roteador;

import roteador.dto.RegistroTabelaRoteamento;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class TabelaRoteamento {
    private final List<RegistroTabelaRoteamento> routes;
    private final String currentHostIpAddress;

    public TabelaRoteamento(String currentHostIpAddress) {
        this.routes = new ArrayList<>();
        this.currentHostIpAddress = currentHostIpAddress;
    }

    public void initializeTable(List<String> neighbors) {
        neighbors.forEach((neighbor) -> {
            // cadastra os endereços IPs vizinhos em uma tabela de roteamento com métrica 1 e saída direta
            this.routes.add(new RegistroTabelaRoteamento(neighbor, 1, neighbor));
        });
    }

    public void updateTable(String tableStr, InetAddress IPAddress, AtomicBoolean tableWasChanged) {
        List<RegistroTabelaRoteamento> receivedRoutes = convertStringToTableRoutes(tableStr, IPAddress);
        BiPredicate<RegistroTabelaRoteamento, RegistroTabelaRoteamento> compareRoutesByDestinationIp = (route1, route2) -> route1.getIpDestino().equals(route2.getIpDestino());

        System.out.println("ROUTES BEFORE TABLE UPDATE:\n" + this.toString());

        // Para cada registro recebido do vizinho
        receivedRoutes.stream()
                .filter(receivedRoute -> !receivedRoute.getIpDestino().equals(currentHostIpAddress))
                .forEach(receivedRoute -> {
                    System.out.println("Received route: " + receivedRoute);
                    Optional<RegistroTabelaRoteamento> routeByDestinationIp = routes.stream()
                            .filter(route -> compareRoutesByDestinationIp.test(route, receivedRoute))
                            .findFirst();

                    routeByDestinationIp.ifPresent(foundRoute -> System.out.println("Found route with same destination IP address: " + foundRoute.getIpDestino()));

                    // Adiciona rota se o IP de destino recebido nao esta na tabela local
                    if (routeByDestinationIp.isEmpty()) {
                        System.out.println("Did not have route with destination IP " + receivedRoute.getIpDestino() + ". Adding new route");
                        receivedRoute.setMetrica(receivedRoute.getMetrica() + 1);
                        routes.add(receivedRoute);
                        tableWasChanged.set(true);
                        // Atualiza metrica e saida se for recebida metrica menor para um IP destino presente na tabela
                    } else if (routeByDestinationIp.get().getMetrica() > (receivedRoute.getMetrica() + 1)) {
                        System.out.println("Received route has smaller metric than current route. Updating route to destination IP " + receivedRoute.getIpDestino());
                        RegistroTabelaRoteamento foundRoute = routeByDestinationIp.get();
                        foundRoute.setMetrica(receivedRoute.getMetrica() + 1);
                        foundRoute.setIpSaida(receivedRoute.getIpSaida());
                        tableWasChanged.set(true);
                    }
                });

        // Retira rotas se IP destino deixar de ser divulgado
        boolean routesWereRemoved = routes.removeIf(route -> {
            // se rota presente na tabela possuia este vizinho como saida && o vizinho nao divulgou mais alguma rota com este destino
            return route.getIpSaida().equals(IPAddress.getHostAddress())
                    && receivedRoutes.stream().noneMatch(receivedRoute -> compareRoutesByDestinationIp.test(receivedRoute, route))
                    && route.getMetrica() != 1;
        });

        if (routesWereRemoved) {
            System.out.println("Removed routes because host stopped sending them");
            tableWasChanged.set(true);
        }

        if (tableWasChanged.get()) {
            System.out.println("ROUTES WERE CHANGED AFTER TABLE UPDATE. CURRENT TABLE:\n" + this.toString());
        }
    }

    public void removeRoutesByOutputIPAddress(String IPtoRemove, AtomicBoolean tableWasChanged) {
        boolean hasRemoved = routes.removeIf((route) -> IPtoRemove.equals(route.getIpSaida()));
        if (hasRemoved) {
            tableWasChanged.set(true);
        }
    }

    public String getTableAsString() {
        /* Tabela de roteamento vazia conforme especificado no protocolo */
        if (routes.isEmpty())
            return "!";
        StringBuilder tableStrBuilder = new StringBuilder();
        routes.forEach((route) -> {
            tableStrBuilder.append("*").append(route.getIpDestino()).append(";").append(route.getMetrica().toString());
        });

        return tableStrBuilder.toString();
    }

    public List<RegistroTabelaRoteamento> convertStringToTableRoutes(String tableStr, InetAddress IPAddress) {
        String[] routesStr = tableStr.split("\\*");
        return Arrays.stream(routesStr).map(route -> {
            String[] routeFields = route.split(";");
            if (routeFields.length > 1) {
                return new RegistroTabelaRoteamento(routeFields[0], Integer.valueOf(routeFields[1]), IPAddress.getHostAddress());
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DESTINO | METRICA | SAIDA\n");
        routes.forEach(route -> {
            stringBuilder.append(route.getIpDestino()).append(" | ").append(route.getMetrica()).append(" | ").append(route.getIpSaida()).append("\n");
        });
        return stringBuilder.toString();
    }
}
