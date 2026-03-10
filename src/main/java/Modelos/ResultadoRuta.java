package Modelos;

import java.util.List;

public class ResultadoRuta {
    private final List<Parada> camino;
    private final double tiempoTotal;
    private final double costoTotal;
    private final double distanciaTotal;
    private final int trasbordos;

    public ResultadoRuta(List<Parada> camino,
                         double tiempoTotal,
                         double costoTotal,
                         double distanciaTotal,
                         int trasbordos) {

        this.camino = camino;
        this.tiempoTotal = tiempoTotal;
        this.costoTotal = costoTotal;
        this.distanciaTotal = distanciaTotal;
        this.trasbordos = trasbordos;
    }

    public List<Parada> getCamino() {
        return camino;
    }

    public double getTiempoTotal() {
        return tiempoTotal;
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    public double getDistanciaTotal() {
        return distanciaTotal;
    }

    public int getTrasbordos() {
        return trasbordos;
    }

}