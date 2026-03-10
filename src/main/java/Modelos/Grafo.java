package Modelos;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;

public class Grafo {

    private Map<Parada, List<Ruta>> adyacencia;

    public Grafo() {
        adyacencia = new HashMap<>();
    }

    public void agregarParada(Parada p) {
        if (p == null) {
            throw new IllegalArgumentException("La parada no puede ser null");
        }

        adyacencia.putIfAbsent(p, new ArrayList<>());
    }

    public void agregarRuta(Parada origen, Ruta ruta) {

        if (origen == null || ruta == null) {
            throw new IllegalArgumentException("Origen o ruta no pueden ser null");
        }

        if (!adyacencia.containsKey(origen)) {
            throw new IllegalArgumentException("La parada origen no existe en el grafo");
        }

        adyacencia.get(origen).add(ruta);
    }

    public Map<Parada, List<Ruta>> getAdyacencia() {
        return adyacencia;
    }


    public ResultadoRuta calcularRuta(Parada origen,Parada destino,Criterio criterio) {

        int AlgoritmoAUsar = DecidirAlgoritmo(criterio);
        List<Parada> camino;
        if(AlgoritmoAUsar == 1){
             camino = TrasbordosBfs(origen, destino);
        }
        else {
             camino = dijkstra(origen, destino, criterio);
        }


        if (camino.isEmpty()) {
            return null; // (((((Recordatorio para agregar excepción))))
        }

        double tiempoTotal = 0;
        double costoTotal = 0;
        double distanciaTotal = 0;
        int transbordos = camino.size() - 1;

        for (int i = 0; i < camino.size() - 1; i++) {

            Parada actual = camino.get(i);
            Parada siguiente = camino.get(i + 1);

            for (Ruta ruta : adyacencia.get(actual)) {

                if (ruta.getDestino().equals(siguiente)) {

                    tiempoTotal += ruta.getTiempo();
                    costoTotal += ruta.getCosto();
                    distanciaTotal += ruta.getDistancia();
                    break;
                }
            }
        }

        return new ResultadoRuta(camino, tiempoTotal, costoTotal, distanciaTotal, transbordos);

    }

    private double obtenerPeso(Ruta r, Criterio criterio) {
        switch (criterio) {
            case TIEMPO:
                return r.getTiempo();
            case COSTO:
                return r.getCosto();
            case DISTANCIA:
                return r.getDistancia();
            default:
                throw new IllegalArgumentException("Criterio inválido");
        }
    }



    public int DecidirAlgoritmo( Criterio criterio) {
        switch (criterio) {
            case TRASBORDOS:
                return 1;
            case COSTO:
            case TIEMPO:
            case DISTANCIA:
                return 2;

        }
        return 0;
    }

    public List<Parada> TrasbordosBfs(Parada origen, Parada destino) {

        Map<Parada, Parada> anteriores = new HashMap<>();
        Set<Parada> visitados = new HashSet<>();
        Queue<Parada> cola = new LinkedList<>();

        cola.add(origen);
        visitados.add(origen);

        while (!cola.isEmpty()) {

            Parada actual = cola.poll();

            if (actual.equals(destino)) {
                break;
            }

            for (Ruta ruta : adyacencia.get(actual)) {

                Parada vecino = ruta.getDestino();

                if (!visitados.contains(vecino)) {

                    visitados.add(vecino);
                    anteriores.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }

        return reconstruirCamino(anteriores, origen, destino);
    }

    public List<Parada> dijkstra (Parada origen, Parada destino, Criterio criterio) {

        Map<Parada, Double> distancias = new HashMap<>();
        Map<Parada, Parada> anteriores = new HashMap<>();
        Set<Parada> visitados = new HashSet<>();

        PriorityQueue<Parada> cola =
                new PriorityQueue<>(Comparator.comparingDouble(distancias::get));

        // Inicializar las distancias
        for (Parada p : adyacencia.keySet()) {
            distancias.put(p, Double.POSITIVE_INFINITY);
        }

        distancias.put(origen, 0.0);
        cola.add(origen);

        while (!cola.isEmpty()) {

            Parada actual = cola.poll();

            if (visitados.contains(actual)) continue;
            visitados.add(actual);


            if (actual.equals(destino)) break;

            for (Ruta ruta : adyacencia.get(actual)) {

                Parada vecino = ruta.getDestino();

                if (visitados.contains(vecino)) continue;

                double nuevaDistancia =
                        distancias.get(actual)
                                + obtenerPeso(ruta, criterio);

                if (nuevaDistancia < distancias.get(vecino)) {

                    distancias.put(vecino, nuevaDistancia);
                    anteriores.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }

        return reconstruirCamino(anteriores, origen, destino);
    }

    private List<Parada> reconstruirCamino(
            Map<Parada, Parada> anteriores,
            Parada origen,
            Parada destino) {

        List<Parada> camino = new ArrayList<>();

        Parada actual = destino;

        while (actual != null) {
            camino.add(0, actual);
            actual = anteriores.get(actual);
        }

        if (!camino.isEmpty() && camino.get(0).equals(origen)) {
            return camino;
        }

        return new ArrayList<>(); // no hay camino
    }


    public void verificarEntradasYSalidas() {

        Map<Parada, Integer> conteoEntradas = new HashMap<>();

        // Inicializar conteo de entradas
        for (Parada parada : adyacencia.keySet()) {
            conteoEntradas.put(parada, 0);
        }

        // Revisar salidas y contar entradas
        for (Parada origen : adyacencia.keySet()) {

            List<Ruta> rutas = adyacencia.get(origen);

            // Verificar si hay salidas para origen
            if (rutas.isEmpty()) {
                System.out.println("La parada " + origen + " no tiene rutas de salida.");
            }

            // Conteo de entradas para el origen
            for (Ruta ruta : rutas) {
                Parada destino = ruta.getDestino();
                conteoEntradas.put(destino, conteoEntradas.get(destino) + 1);
            }
        }

        // Verificar si hay entradas entradas para todas las pradas
        for (Parada parada : conteoEntradas.keySet()) {

            if (conteoEntradas.get(parada) == 0) {
                System.out.println("La parada " + parada + " no tiene rutas de entrada.");
            }
        }
    }
    public void eliminarParada(Parada p) {
        if (!adyacencia.containsKey(p)) return;

        //Elimina ruta
        for (List<Ruta> rutas : adyacencia.values()) {
            rutas.removeIf(ruta -> ruta.getDestino().equals(p));
        }

        // Eliminar la parada
        adyacencia.remove(p);
    }

    public void eliminarRuta(Parada origen, Parada destino) {
        if (adyacencia.containsKey(origen)) {
            adyacencia.get(origen).removeIf(ruta -> ruta.getDestino().equals(destino));
        }
    }

}