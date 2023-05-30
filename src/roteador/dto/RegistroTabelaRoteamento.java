/*
 * Trabalho Final da Disciplina de Fundamentos de Redes de Computadores
 * Grupo: Bruno Ramos, Fernanda Mello, Lucas Salbego, Matheus Pozzer
 */
package roteador.dto;

public class RegistroTabelaRoteamento {
    private String ipDestino;
    private Integer metrica;
    private String ipSaida;

    public RegistroTabelaRoteamento(String ipDestino, Integer metrica, String ipSaida) {
        this.ipDestino = ipDestino;
        this.metrica = metrica;
        this.ipSaida = ipSaida;
    }

    public String getIpDestino() {
        return ipDestino;
    }

    public void setIpDestino(String ipDestino) {
        this.ipDestino = ipDestino;
    }

    public Integer getMetrica() {
        return metrica;
    }

    public void setMetrica(Integer metrica) {
        this.metrica = metrica;
    }

    public String getIpSaida() {
        return ipSaida;
    }

    public void setIpSaida(String ipSaida) {
        this.ipSaida = ipSaida;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Destino: ").append(ipDestino).append("  Metrica: ").append(metrica).append("  Saida: ").append(ipSaida).toString();
    }
}
