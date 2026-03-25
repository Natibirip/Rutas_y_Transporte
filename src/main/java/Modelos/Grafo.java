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
import java.util.Deque;
import java.util.ArrayDeque;

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


    public ResultadoRuta calcularRuta(Parada origen, Parada destino, Criterio criterio) {
        List<Parada> camino = new ArrayList<>();

        // Decidir que algoritmo usar
        int opcionAlgoritmo = DecidirAlgoritmo(criterio);

        if (opcionAlgoritmo == 1) {
            camino = bfs01Transbordos(origen, destino);
        } else if (opcionAlgoritmo == 2) {
            camino = dijkstra(origen, destino, criterio);
        } else {
            System.out.println("Criterio no soportado o algoritmo no definido.");
            return null;
        }

        if (camino == null || camino.isEmpty()) {
            return null;
        }

        // totales del camino ganador
        double tiempoTotal = 0;
        double costoTotal = 0;
        double distanciaTotal = 0;
        int transbordos = 0;
        TipoVehiculo vehiculoAnterior = null;

        for (int i = 0; i < camino.size() - 1; i++) {
            Parada actual = camino.get(i);
            Parada siguiente = camino.get(i + 1);

            for (Ruta ruta : adyacencia.get(actual)) {
                if (ruta.getDestino().equals(siguiente)) {
                    tiempoTotal += ruta.getTiempo();
                    costoTotal += ruta.getCosto();
                    distanciaTotal += ruta.getDistancia();

                    // Lógica real de transbordo (cambio de vehículo)
                    if (vehiculoAnterior != null && vehiculoAnterior != ruta.getVehiculo()) {
                        transbordos++;
                    }
                    vehiculoAnterior = ruta.getVehiculo();
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


    private List<Parada> bfs01Transbordos(Parada origen, Parada destino) {
        Map<Parada, Integer> minTransbordos = new HashMap<>();
        Map<Parada, Parada> anteriores = new HashMap<>();
        Map<Parada, Ruta> rutaLlegada = new HashMap<>(); // Memoria de vehiculo

        //doble queue
        Deque<Parada> deque = new ArrayDeque<>();

        for (Parada p : adyacencia.keySet()) {
            minTransbordos.put(p, Integer.MAX_VALUE);
        }

        minTransbordos.put(origen, 0);
        deque.addFirst(origen);

        while (!deque.isEmpty()) {
            Parada actual = deque.pollFirst();

            if (actual.equals(destino)) break;

            for (Ruta ruta : adyacencia.get(actual)) {
                Parada vecino = ruta.getDestino();
                TipoVehiculo vehiculoRuta = ruta.getVehiculo();

                int peso = 0;
                Ruta rutaPrevia = rutaLlegada.get(actual);
                if (rutaPrevia != null && rutaPrevia.getVehiculo() != vehiculoRuta) {
                    peso = 1;
                }

                int nuevosTransbordos = minTransbordos.get(actual) + peso;

                if (nuevosTransbordos < minTransbordos.get(vecino)) {
                    minTransbordos.put(vecino, nuevosTransbordos);
                    anteriores.put(vecino, actual);
                    rutaLlegada.put(vecino, ruta);

                    if (peso == 0) {
                        deque.addFirst(vecino); //si es el mismo vehiculo al frente
                    } else {
                        deque.addLast(vecino);  //sino al final
                    }
                }
            }
        }

        return reconstruirCamino(anteriores, origen, destino);
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

        Map<Parada, Double> peso = new HashMap<>();
        Map<Parada, Parada> anteriores = new HashMap<>();
        Set<Parada> visitados = new HashSet<>();

        PriorityQueue<Parada> cola =
                new PriorityQueue<>(Comparator.comparingDouble(peso::get));

        for (Parada p : adyacencia.keySet()) {
            peso.put(p, Double.POSITIVE_INFINITY);
        }

        peso.put(origen, 0.0);
        cola.add(origen);

        while (!cola.isEmpty()) {

            Parada actual = cola.poll();

            if (visitados.contains(actual)) continue;
            visitados.add(actual);


            if (actual.equals(destino)) break;

            for (Ruta ruta : adyacencia.get(actual)) {

                Parada vecino = ruta.getDestino();

                if (visitados.contains(vecino)) continue;

                double nuevaDistancia = //nuevo peso
                        peso.get(actual)
                                + obtenerPeso(ruta, criterio);

                if (nuevaDistancia < peso.get(vecino)) {

                    peso.put(vecino, nuevaDistancia);
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


    public boolean esFuertementeConexo() {
        if (adyacencia.isEmpty()) return true;

        // una parada como punto de inicio
        Parada inicio = adyacencia.keySet().iterator().next();

        // chequea si se llega a todas las paradas desde 'inicio'
        if (!alcanzaTodas(inicio, adyacencia)) {
            return false;
        }

        // Grafo Transpuesto
        Map<Parada, List<Parada>> grafoInvertido = new HashMap<>();
        for (Parada p : adyacencia.keySet()) {
            grafoInvertido.put(p, new ArrayList<>());
        }
        for (Parada origen : adyacencia.keySet()) {
            for (Ruta ruta : adyacencia.get(origen)) {
                // se invierte
                grafoInvertido.get(ruta.getDestino()).add(origen);
            }
        }

        // chequea si inicio se llaga desde las demas paradas
        return alcanzaTodasInvertido(inicio, grafoInvertido);
    }

    // BFS normal
    private boolean alcanzaTodas(Parada inicio, Map<Parada, List<Ruta>> grafo) {
        Set<Parada> visitados = new HashSet<>();
        Queue<Parada> cola = new LinkedList<>();

        cola.add(inicio);
        visitados.add(inicio);

        while (!cola.isEmpty()) {
            Parada actual = cola.poll();
            for (Ruta ruta : grafo.get(actual)) {
                if (!visitados.contains(ruta.getDestino())) {
                    visitados.add(ruta.getDestino());
                    cola.add(ruta.getDestino());
                }
            }
        }
        return visitados.size() == grafo.keySet().size();
    }

    //BFS en el grafo invertido
    private boolean alcanzaTodasInvertido(Parada inicio, Map<Parada, List<Parada>> grafoInvertido) {
        Set<Parada> visitados = new HashSet<>();
        Queue<Parada> cola = new LinkedList<>();

        cola.add(inicio);
        visitados.add(inicio);

        while (!cola.isEmpty()) {
            Parada actual = cola.poll();
            for (Parada vecino : grafoInvertido.get(actual)) {
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    cola.add(vecino);
                }
            }
        }
        return visitados.size() == grafoInvertido.keySet().size();
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