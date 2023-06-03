/*
 * Trabalho Final da Disciplina de Fundamentos de Redes de Computadores
 * Grupo: Bruno Ramos, Fernanda Mello, Lucas Salbego, Matheus Pozzer
 */
package roteador.dto;

public class RoutingTableRegister {
    private String destinationIp;
    private Integer metric;
    private String outputIp;

    public RoutingTableRegister(String destinationIp, Integer metric, String outputIp) {
        this.destinationIp = destinationIp;
        this.metric = metric;
        this.outputIp = outputIp;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    public Integer getMetric() {
        return metric;
    }

    public void setMetric(Integer metric) {
        this.metric = metric;
    }

    public String getOutputIp() {
        return outputIp;
    }

    public void setOutputIp(String outputIp) {
        this.outputIp = outputIp;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Destino: ").append(destinationIp).append("  Metrica: ").append(metric).append("  Saida: ").append(outputIp).toString();
    }
}
