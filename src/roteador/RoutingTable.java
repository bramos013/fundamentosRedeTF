/*
 * Trabalho Final da Disciplina de Fundamentos de Redes de Computadores
 * Grupo: Bruno Ramos, Fernanda Mello, Lucas Salbego, Matheus Pozzer
 */
package roteador;

import roteador.dto.RoutingTableRegister;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class RoutingTable {
    private final List<RoutingTableRegister> routes;
    private final String currentHostIpAddress;

    public RoutingTable(String currentHostIpAddress) {
        this.routes = new ArrayList<>();
        this.currentHostIpAddress = currentHostIpAddress;
    }

    public void initializeTable(List<String> neighbors) {
        neighbors.forEach((neighbor) -> {
            // cadastra os endereços IPs vizinhos em uma tabela de roteamento com métrica 1 e saída direta
            this.routes.add(new RoutingTableRegister(neighbor, 1, neighbor));
        });
    }

    public void updateTable(String tableStr, InetAddress IPAddress, AtomicBoolean tableWasChanged) {
        List<RoutingTableRegister> receivedRoutes = convertStringToTableRoutes(tableStr, IPAddress);
        BiPredicate<RoutingTableRegister, RoutingTableRegister> compareRoutesByDestinationIp = (route1, route2) -> route1.getDestinationIp().equals(route2.getDestinationIp());

        System.out.println("ROUTES BEFORE TABLE UPDATE:\n" + this.toString());

        // Para cada registro recebido do vizinho
        receivedRoutes.stream()
                .filter(receivedRoute -> !receivedRoute.getDestinationIp().equals(currentHostIpAddress))
                .forEach(receivedRoute -> {
                    System.out.println("Received route: " + receivedRoute);
                    Optional<RoutingTableRegister> routeByDestinationIp = routes.stream()
                            .filter(route -> compareRoutesByDestinationIp.test(route, receivedRoute))
                            .findFirst();

                    routeByDestinationIp.ifPresent(foundRoute -> System.out.println("Found route with same destination IP address: " + foundRoute.getDestinationIp()));

                    // Adiciona rota se o IP de destino recebido nao esta na tabela local
                    if (routeByDestinationIp.isEmpty()) {
                        System.out.println("Did not have route with destination IP " + receivedRoute.getDestinationIp() + ". Adding new route");
                        receivedRoute.setMetric(receivedRoute.getMetric() + 1);
                        routes.add(receivedRoute);
                        tableWasChanged.set(true);
                        // Atualiza metrica e saida se for recebida metrica menor para um IP destino presente na tabela
                    } else if (routeByDestinationIp.get().getMetric() > (receivedRoute.getMetric() + 1)) {
                        System.out.println("Received route has smaller metric than current route. Updating route to destination IP " + receivedRoute.getDestinationIp());
                        RoutingTableRegister foundRoute = routeByDestinationIp.get();
                        foundRoute.setMetric(receivedRoute.getMetric() + 1);
                        foundRoute.setOutputIp(receivedRoute.getOutputIp());
                        tableWasChanged.set(true);
                    }
                });

        // Retira rotas se IP destino deixar de ser divulgado
        boolean routesWereRemoved = routes.removeIf(route -> {
            // se rota presente na tabela possuia este vizinho como saida && o vizinho nao divulgou mais alguma rota com este destino
            return route.getOutputIp().equals(IPAddress.getHostAddress())
                    && receivedRoutes.stream().noneMatch(receivedRoute -> compareRoutesByDestinationIp.test(receivedRoute, route))
                    && route.getMetric() != 1;
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
        boolean hasRemoved = routes.removeIf((route) -> IPtoRemove.equals(route.getOutputIp()));
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
            tableStrBuilder.append("*").append(route.getDestinationIp()).append(";").append(route.getMetric().toString());
        });

        return tableStrBuilder.toString();
    }

    public List<RoutingTableRegister> convertStringToTableRoutes(String tableStr, InetAddress IPAddress) {
        String[] routesStr = tableStr.split("\\*");
        return Arrays.stream(routesStr).map(route -> {
            String[] routeFields = route.split(";");
            if (routeFields.length > 1) {
                return new RoutingTableRegister(routeFields[0], Integer.valueOf(routeFields[1]), IPAddress.getHostAddress());
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DESTINO | METRICA | SAIDA\n");
        routes.forEach(route -> {
            stringBuilder.append(route.getDestinationIp()).append(" | ").append(route.getMetric()).append(" | ").append(route.getOutputIp()).append("\n");
        });
        return stringBuilder.toString();
    }
}
