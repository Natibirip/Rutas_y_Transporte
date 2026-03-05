package Modelos;

public class Ruta {

    private final Parada destino;
    private final double tiempo;
    private final double costo;
    private final double distancia;

    public Ruta(Parada destino,
                double tiempo,
                double costo,
                double distancia) {

        this.destino = destino;
        this.tiempo = tiempo;
        this.costo = costo;
        this.distancia = distancia;
    }

    public Parada getDestino() { return destino; }
    public double getTiempo() { return tiempo; }
    public double getCosto() { return costo; }
    public double getDistancia() { return distancia; }

    @Override
    public String toString() {
        return "→ " + destino.getNombre() +
                " | Tiempo: " + tiempo +
                " | Costo: " + costo +
                " | Distancia: " + distancia;
    }
}