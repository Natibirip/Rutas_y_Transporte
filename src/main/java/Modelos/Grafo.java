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


    public Map<Parada, List<Ruta>> getAdyacencia() {
        return adyacencia;
    }
    /*
    Nombre: getAdyacencia

    Parámetros:
    Ninguno

    Retorno:
    Map<Parada, List<Ruta>> → Estructura de adyacencia que representa el grafo.

    Descripción:
    Retorna el mapa de adyacencia del grafo, donde cada clave es una parada
    y su valor es la lista de rutas (conexiones) que salen de dicha parada.
    Permite acceder a la estructura interna del grafo para consultas o procesamiento.
*/

    public void agregarParada(Parada p) {
        if (p == null) {
            throw new IllegalArgumentException("La parada no puede ser null");
        }

        adyacencia.putIfAbsent(p, new ArrayList<>());
    }
    /*
    Nombre: agregarParada

    Parámetros:
    @param p Parada que se desea agregar al grafo.

    Retorno: void

    Descripción:
    Agrega una nueva parada (nodo) al grafo.
    Valida que la parada no sea null y, si no existe previamente en la estructura,
    la inserta en el mapa de adyacencia con una lista vacía de rutas asociadas.
*/

    public void agregarRuta(Parada origen, Ruta ruta) {

        if (origen == null || ruta == null) {
            throw new IllegalArgumentException("Origen o ruta no pueden ser null");
        }

        if (!adyacencia.containsKey(origen)) {
            throw new IllegalArgumentException("La parada origen no existe en el grafo");
        }

        adyacencia.get(origen).add(ruta);
    }
/*
    Nombre: agregarRuta

    Parámetros:
    @param origen Parada desde la cual parte la ruta.
    @param ruta Ruta que contiene destino, tiempo, costo, distancia y tipo de vehículo.

    Retorno: void

    Descripción:
    Agrega una conexión (arista) al grafo desde una parada de origen hacia otra parada destino.
    Valida que los parámetros no sean nulos y que la parada de origen exista en el grafo.
    Si las validaciones son correctas, la ruta se añade a la lista de adyacencia del nodo origen.
*/



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
    /*
    Nombre: calcularRuta

    Parámetros:
    @param origen Parada inicial del recorrido.
    @param destino Parada final del recorrido.
    @param criterio Criterio de optimización (TIEMPO, COSTO, DISTANCIA, TRASBORDOS).

    Retorno:
    ResultadoRuta → Objeto que contiene el camino óptimo y sus métricas totales.

    Descripción:
    Calcula la mejor ruta entre dos paradas según el criterio especificado.
    Primero decide qué algoritmo utilizar (BFS 0-1 o Dijkstra) y obtiene el camino óptimo.
    Luego recorre dicho camino para calcular los valores acumulados de tiempo, costo,
    distancia y número de transbordos (cambios de vehículo).
    Finalmente retorna un objeto con toda la información de la ruta encontrada.

    Complejidad temporal:

    - BFS 0-1 (Trasbordos):
      O(V + E)

    - Dijkstra:
      O((V+E)log V)

    Complejidad total:
    - O((V+E) log V) en el peor caso (cuando usa Dijkstra)
    - Ω(V) en el mejor caso
*/

    public ResultadoRuta calcularRutaAlternativa(Parada origen, Parada destino, Criterio criterio, List<Parada> ruta1, int algoritmoElegir) {

        ResultadoRuta mejorAlternativa = null;

        // recorrerruta original
        for (int i = 0; i < ruta1.size() - 1; i++) {

            Parada desde = ruta1.get(i);
            Parada hacia = ruta1.get(i + 1);

            // eliminar temporalmente la ruta
            Ruta rutaEliminada = null;

            for (Ruta r : adyacencia.get(desde)) {
                if (r.getDestino().equals(hacia)) {
                    rutaEliminada = r;
                    break;
                }
            }

            if (rutaEliminada == null) continue;

            adyacencia.get(desde).remove(rutaEliminada);

            // recalcular ruta
            List<Parada> nuevoCamino = null;
            if (algoritmoElegir == 1) {
                nuevoCamino = bfs01Transbordos(origen, destino);
            } else if (algoritmoElegir == 2) {
                nuevoCamino = dijkstra(origen, destino, criterio);
            } else if (algoritmoElegir == 3) {
                nuevoCamino = bellmanFord(origen, destino, criterio);
            }

            //  verificar valides

            if (nuevoCamino != null && !nuevoCamino.isEmpty() && !nuevoCamino.equals(ruta1)) {

                ResultadoRuta resultado = calcularRuta(origen, destino, criterio);

                if(criterio == Criterio.TIEMPO){
                    if (mejorAlternativa == null || resultado.getTiempoTotal() < mejorAlternativa.getTiempoTotal()) {
                        mejorAlternativa = resultado;
                    }
                }
                if(criterio == Criterio.COSTO){
                    if (mejorAlternativa == null || resultado.getCostoTotal() < mejorAlternativa.getCostoTotal()) {
                        mejorAlternativa = resultado;
                    }
                }
                if(criterio == Criterio.DISTANCIA){
                    if (mejorAlternativa == null || resultado.getDistanciaTotal() < mejorAlternativa.getDistanciaTotal()) {
                        mejorAlternativa = resultado;
                    }
                }
                if(criterio == Criterio.TRASBORDOS){
                    if (mejorAlternativa == null || resultado.getTrasbordos() < mejorAlternativa.getTrasbordos()) {
                        mejorAlternativa = resultado;
                    }
                }

            }

            //  restaurar la ruta
            adyacencia.get(desde).add(rutaEliminada);
        }

        return mejorAlternativa;
    }
    /*
    Nombre: calcularRutaAlternativa

    Parámetros:
    @param origen Parada inicial del recorrido.
    @param destino Parada final del recorrido.
    @param criterio Criterio de optimización (TIEMPO, COSTO, DISTANCIA, TRASBORDOS).
    @param ruta1 Ruta original previamente calculada.
    @param algoritmoElegir Algoritmo a utilizar:
                           1: BFS 0-1 (trasbordos)
                           2: Dijkstra
                           3: Bellman-Ford

    Retorno:
    ResultadoRuta → Objeto que representa la mejor ruta alternativa encontrada.
                    Retorna null si no existe una alternativa válida.

    Descripción:
    Calcula una ruta alternativa distinta a una ruta original dada. Para ello,
    recorre cada segmento de la ruta original y elimina temporalmente esa conexión
    del grafo. Luego, recalcula una nueva ruta utilizando el algoritmo seleccionado.
    Si la nueva ruta es válida y diferente a la original, se evalúa según el criterio
    de optimización y se mantiene como mejor alternativa si mejora el resultado actual.
    Finalmente, se restaura la ruta eliminada y continúa el proceso hasta evaluar todas
    las posibles variaciones.

    Complejidad temporal:
    O(L * (V + E) log V) en el peor caso, donde L es la longitud de la ruta original,
    debido a que se ejecuta un algoritmo de búsqueda por cada arista eliminada
    (principalmente Dijkstra).

    Θ(L * (V + E) log V) en el caso promedio.

    Ω(L * V) en el mejor caso, cuando se encuentran rápidamente alternativas válidas.

    Complejidad espacial:
    O(V), debido al uso de estructuras auxiliares en los algoritmos de búsqueda.


*/

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
    /*
    Nombre: obtenerPeso

    Parámetros:
    @param r Ruta de la cual se desea obtener el peso.
    @param criterio Criterio de optimización (TIEMPO, COSTO, DISTANCIA).

    Retorno:
    double → Valor del peso correspondiente según el criterio.

    Descripción:
    Determina el peso de una ruta en función del criterio especificado.
    Retorna el tiempo, costo o distancia de la ruta según corresponda.
    Si el criterio no es válido, lanza una excepción.
*/


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

    /*
    Nombre: bfs01Transbordos

    Parámetros:
    @param origen Parada inicial del recorrido.
    @param destino Parada final del recorrido.

    Retorno:
    List<Parada> → Lista de paradas que representa el camino con menor número de transbordos.

    Descripción:
    Implementa una variante del algoritmo BFS 0-1 para encontrar el camino con menor cantidad
    de transbordos entre dos paradas. Utiliza una deque (cola doble) para priorizar los movimientos
    que no implican cambio de vehículo (peso 0) sobre aquellos que sí lo implican (peso 1).
    Mantiene un registro del número mínimo de transbordos hacia cada parada y reconstruye
    el camino óptimo al final.

    Complejidad temporal:
    O(V + E)

    Θ(V + E) en el caso promedio, ya que cada nodo y arista se procesa como máximo una vez
    con operaciones constantes en la deque.

    Ω(V) en el mejor caso, cuando el destino se encuentra rápidamente sin explorar el grapo completo.

    Complejidad espacial:
    O(V), debido al uso de mapas auxiliares (minTransbordos, anteriores, rutaLlegada)
    y la estructura deque.

*/

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
    /*
        Nombre: DecidirAlgoritmo

        Parámetros:
        @param criterio Criterio de optimización seleccionado.

        Retorno:
        int → Identificador del algoritmo a utilizar.
               1: BFS 0-1 (trasbordos)
               2: Dijkstra (tiempo, costo, distancia)
               0: Criterio no válido

        Descripción:
        Determina qué algoritmo de búsqueda se debe utilizar en función del criterio especificado.
        Si el criterio es TRASBORDOS, selecciona BFS 0-1 para minimizar cambios de vehículo.
        Si el criterio es TIEMPO, COSTO o DISTANCIA, selecciona Dijkstra para optimizar el peso correspondiente.
    */


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
/*
    Nombre: dijkstra

    Parámetros:
    @param origen Parada inicial del recorrido.
    @param destino Parada final del recorrido.
    @param criterio Criterio de optimización (TIEMPO, COSTO, DISTANCIA).

    Retorno:
    List<Parada> → Lista de paradas que representa el camino óptimo encontrado.

    Descripción:
    Implementa el algoritmo de Dijkstra para calcular el camino más corto entre dos paradas
    según el criterio especificado. Utiliza una cola de prioridad para seleccionar en cada
    iteración la parada con menor peso acumulado. A medida que recorre el grafo, actualiza
    las distancias mínimas hacia cada nodo y almacena el nodo anterior para reconstruir el camino.
    El proceso termina cuando se alcanza el destino o cuando no hay más nodos por explorar.

    Complejidad temporal:
    O((V+E) log V) en el peor caso, debido al uso de la cola de prioridad (PriorityQueue)
    y la posible inserción de múltiples elementos.

    Θ(E log V) en el caso promedio.

    Ω(V) en el mejor caso, cuando el destino se encuentra rápidamente con pocas exploraciones.

    Complejidad espacial:
    O(V), por el uso de estructuras auxiliares como mapas y conjuntos (peso, anteriores, visitados).

    Observaciones:
    - No soporta pesos negativos en las rutas.
    - Utiliza un enfoque greedy, seleccionando siempre el nodo con menor costo acumulado.
*/

    public List<Parada> bellmanFord(Parada origen, Parada destino, Criterio criterio) {

        Map<Parada, Double> pesos = new HashMap<>();
        Map<Parada, Parada> anteriores = new HashMap<>();


        for (Parada p : adyacencia.keySet()) {
            pesos.put(p, Double.POSITIVE_INFINITY);
        }

        pesos.put(origen, 0.0);

        int V = adyacencia.size();


        for (int i = 0; i < V - 1; i++) { // relajar aristas V-1 veces

            for (Parada actual : adyacencia.keySet()) {

                for (Ruta ruta : adyacencia.get(actual)) {

                    Parada vecino = ruta.getDestino();

                    double nuevaDistancia =
                            pesos.get(actual) + obtenerPeso(ruta, criterio);

                    if (nuevaDistancia < pesos.get(vecino)) {
                        pesos.put(vecino, nuevaDistancia);
                        anteriores.put(vecino, actual);
                    }
                }
            }
        }

        // Detectar si hay ciclos ngativos
        for (Parada actual : adyacencia.keySet()) {

            for (Ruta ruta : adyacencia.get(actual)) {

                Parada vecino = ruta.getDestino();

                double nuevaDistancia =
                        pesos.get(actual) + obtenerPeso(ruta, criterio);

                if (nuevaDistancia < pesos.get(vecino)) {
                    throw new RuntimeException("Ciclo negativo detectado");
                }
            }
        }


        return reconstruirCamino(anteriores, origen, destino);
    }
    /*
    Nombre: bellmanFord

    Parámetros:
    @param origen Parada inicial del recorrido.
    @param destino Parada final del recorrido.
    @param criterio Criterio de optimización (TIEMPO, COSTO, DISTANCIA).

    Retorno:
    List<Parada> → Lista de paradas que representa el camino óptimo encontrado.

    Descripción:
    Implementa el algoritmo de Bellman-Ford para calcular el camino más corto entre dos paradas.
    Inicializa las distancias y luego relaja todas las aristas del grafo V-1 veces, donde V es el
    número de nodos. Este proceso garantiza encontrar la solución óptima incluso en presencia de
    pesos negativos. Posteriormente, realiza una iteración adicional para detectar ciclos negativos.
    Finalmente, reconstruye el camino desde el destino hasta el origen.

    Complejidad temporal:
    O(V * E) en el peor y caso promedio, debido a que se recorren todas las aristas
    del grafo en cada una de las V-1 iteraciones.

    Ω(E) en el mejor caso, considerando que al menos se debe recorrer el grafo una vez.

    Complejidad espacial:
    O(V), por el uso de estructuras auxiliares como mapas de distancias y nodos anteriores.

    Observaciones:
    - Soporta pesos negativos, a diferencia de Dijkstra.
    - Permite detectar ciclos negativos en el grafo.
    - Es menos eficiente que Dijkstra, pero más flexible en escenarios complejos.
*/

    public List<Parada> floydWarshallCamino(Parada origen, Parada destino, Criterio criterio) {

        int n = adyacencia.size();

        List<Parada> listaParadas = new ArrayList<>(adyacencia.keySet());
        Map<Parada, Integer> indice = new HashMap<>();

        for (int i = 0; i < n; i++) {
            indice.put(listaParadas.get(i), i);
        }

        double[][] dist = new double[n][n];
        Parada[][] next = new Parada[n][n];

        // Inicializar
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    dist[i][j] = 0;
                } else {
                    dist[i][j] = Double.POSITIVE_INFINITY;
                }
                next[i][j] = null;
            }
        }

        // Llenar las rutas directas
        for (Parada origenP : adyacencia.keySet()) {
            int i = indice.get(origenP);

            for (Ruta ruta : adyacencia.get(origenP)) {
                int j = indice.get(ruta.getDestino());

                dist[i][j] = obtenerPeso(ruta, criterio);
                next[i][j] = ruta.getDestino();
            }
        }

        // Floyd-Warshall
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {

                    if (dist[i][k] + dist[k][j] < dist[i][j]) {

                        dist[i][j] = dist[i][k] + dist[k][j];


                        next[i][j] = next[i][k];//Actualizacion
                    }
                }
            }
        }

        // Detectar ciclos negativos
        for (int i = 0; i < n; i++) {
            if (dist[i][i] < 0) {
                throw new RuntimeException("Ciclo negativo detectado");
            }
        }

        // RECONSTRUIR CAMINO
        List<Parada> camino = new ArrayList<>();

        Integer i = indice.get(origen);
        Integer j = indice.get(destino);

        if (next[i][j] == null) {
            return camino; // no hay ruta
        }

        Parada actual = origen;
        camino.add(actual);

        while (!actual.equals(destino)) {
            actual = next[indice.get(actual)][j];
            camino.add(actual);
        }

        return camino;
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
    /*
    Nombre: reconstruirCamino

    Parámetros:
    @param anteriores Mapa que indica el nodo previo en el camino para cada parada.
    @param origen Parada inicial del recorrido.
    @param destino Parada final del recorrido.

    Retorno:
    List<Parada> → Lista de paradas que representa el camino reconstruido.
                   Retorna una lista vacía si no existe camino.

    Descripción:
    Reconstruye el camino desde el nodo destino hasta el origen utilizando el mapa
    de nodos anteriores generado por los algoritmos de búsqueda. Recorre el mapa
    desde el destino hacia atrás, insertando cada parada al inicio de la lista para
    obtener el orden correcto. Finalmente, verifica que el camino reconstruido comience
    en el origen; de lo contrario, retorna una lista vacía indicando que no existe ruta válida.
*/


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
    /*
    Nombre: esFuertementeConexo

    Parámetros:
    Ninguno

    Retorno:
    boolean → true si el grafo es fuertemente conexo, false en caso contrario.

    Descripción:
    Determina si el grafo es fuertemente conexo, es decir, si existe un camino
    entre cada par de paradas en ambas direcciones. Primero verifica si desde una
    parada inicial se puede alcanzar a todas las demás utilizando BFS. Luego construye
    el grafo transpuesto (invirtiendo las direcciones de las rutas) y verifica si desde
    esa misma parada se puede llegar nuevamente a todas las demás. Si ambas condiciones
    se cumplen, el grafo es fuertemente conexo.
*/

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
    /*
    Nombre: alcanzaTodas

    Parámetros:
    @param inicio Parada desde la cual se inicia el recorrido.
    @param grafo Estructura de adyacencia que representa el grafo.

    Retorno:
    boolean → true si se puede llegar a todas las paradas desde el inicio, false en caso contrario.

    Descripción:
    Realiza un recorrido BFS desde una parada inicial para determinar
    si es posible alcanzar todas las demás paradas del grafo. Utiliza una cola para explorar
    los nodos de manera progresiva y un conjunto para evitar visitar nodos repetidos.
    Al finalizar, compara la cantidad de nodos visitados con el total de nodos del grafo.

    Complejidad temporal:
    O(V + E), ya que cada parada (nodo) y cada ruta (arista) se procesan como máximo una vez.

    Θ(V + E) en el caso promedio.

    Ω(V) en el mejor caso, cuando el grafo es pequeño o se alcanza rápidamente a todos los nodos.

    Complejidad espacial:
    O(V), debido al uso de la cola y el conjunto de nodos visitados.

    Observaciones:
    - Implementa el algoritmo BFS clásico.
    - Es utilizado como parte de la verificación de conectividad del grafo.
*/

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
    /*
        Nombre: eliminarParada

        Parámetros:
        @param p Parada que se desea eliminar del grafo.

        Retorno: void

        Descripción:
        Elimina una parada del grafo junto con todas las rutas asociadas a ella.
        Primero verifica si la parada existe; si no existe, no realiza ninguna acción.
        Luego recorre todas las listas de adyacencia para eliminar las rutas que tienen
        como destino la parada indicada. Finalmente, elimina la parada del mapa de adyacencia.
    */
    public void eliminarRuta(Parada origen, Parada destino) {
        if (adyacencia.containsKey(origen)) {
            adyacencia.get(origen).removeIf(ruta -> ruta.getDestino().equals(destino));
        }
    }
    /*
    Nombre: eliminarRuta

    Parámetros:
    @param origen Parada desde la cual parte la ruta a eliminar.
    @param destino Parada destino de la ruta a eliminar.

    Retorno: void

    Descripción:
    Elimina una ruta específica del grafo desde una parada de origen hacia una parada destino.
    Verifica si la parada de origen existe en el grafo y, en caso afirmativo, elimina de su lista
    de adyacencia todas las rutas cuyo destino coincida con la parada indicada.
*/

}