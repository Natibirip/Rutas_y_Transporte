package Modelos;

public class Ruta {
    private final Parada destino;
    private final double tiempo;
    private final double costo;
    private final double distancia;
    private final TipoVehiculo vehiculo; // Nuevo atributo

    public Ruta(Parada destino, double tiempo, double costo, double distancia, TipoVehiculo vehiculo) {
        this.destino = destino;
        this.tiempo = tiempo;
        this.costo = costo;
        this.distancia = distancia;
        this.vehiculo = vehiculo;
    }

    public Parada getDestino() { return destino; }
    public double getTiempo() { return tiempo; }
    public double getCosto() { return costo; }
    public double getDistancia() { return distancia; }
    public TipoVehiculo getVehiculo() { return vehiculo; }

    @Override
    public String toString() {
        return "→ " + destino.getNombre() +
                " | Tiempo: " + tiempo +
                " | Costo: " + costo +
                " | Distancia: " + distancia +
                " | Vehiculo: " + vehiculo;
    }
}