package controladores;

import Modelos.Criterio;
import Modelos.Grafo;
import Modelos.Parada;
import Modelos.Ruta;
import Modelos.ResultadoRuta;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.event.ActionEvent;

// IMPORTANTE: Asegúrate de tener estas importaciones para dibujar
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class MainController {

    private Grafo redTransporte;

    // Aquí declaramos el diccionario para la Opción 1 (separar lógica de vista)
    private Map<Parada, Point2D> coordenadasMapa = new HashMap<>();

    @FXML private ComboBox<Parada> comboOrigen;
    @FXML private ComboBox<Parada> comboDestino;
    @FXML private ComboBox<Criterio> comboCriterio;
    @FXML private Pane mapaPane;
    @FXML private Label lblResultadoRuta;
    @FXML private Label lblTiempo;
    @FXML private Label lblDistancia;
    @FXML private Label lblCosto;

    @FXML
    public void initialize() {
        redTransporte = new Grafo();
        comboCriterio.getItems().setAll(Criterio.values());
        comboCriterio.setValue(Criterio.TIEMPO);

        cargarDatosDePrueba();
        dibujarGrafo(); // <--- Se llama aquí mismo al iniciar la app
    }

    // --------------------------------------------------------
    // AQUÍ ES DONDE PONES EL MÉTODO dibujarGrafo()
    // --------------------------------------------------------
    private void dibujarGrafo() {
        mapaPane.getChildren().clear();
        Map<Parada, List<Ruta>> adyacencia = redTransporte.getAdyacencia();

        // 1. Dibujar las Rutas (Líneas)
        for (Parada origen : adyacencia.keySet()) {
            for (Ruta ruta : adyacencia.get(origen)) {
                Parada destino = ruta.getDestino();

                Point2D posOrigen = coordenadasMapa.get(origen);
                Point2D posDestino = coordenadasMapa.get(destino);

                if (posOrigen != null && posDestino != null) {
                    Line linea = new Line(posOrigen.getX(), posOrigen.getY(), posDestino.getX(), posDestino.getY());
                    linea.setStrokeWidth(2);
                    linea.setStroke(Color.GRAY);
                    mapaPane.getChildren().add(linea);
                }
            }
        }

        // 2. Dibujar las Paradas (Círculos)
        for (Parada p : adyacencia.keySet()) {
            Point2D pos = coordenadasMapa.get(p);

            if (pos != null) {
                Circle nodo = new Circle(pos.getX(), pos.getY(), 15, Color.DODGERBLUE);
                nodo.setStroke(Color.DARKBLUE);
                nodo.setStrokeWidth(2);

                nodo.setOnMouseClicked(event -> {
                    if (comboOrigen.getValue() == null || (comboOrigen.getValue() != null && comboDestino.getValue() != null)) {
                        comboOrigen.setValue(p);
                        comboDestino.setValue(null);
                    } else {
                        comboDestino.setValue(p);
                    }
                });

                nodo.setOnMouseEntered(e -> nodo.setFill(Color.LIGHTBLUE));
                nodo.setOnMouseExited(e -> nodo.setFill(Color.DODGERBLUE));

                Label etiqueta = new Label(p.getNombre());
                etiqueta.setLayoutX(pos.getX() - 20);
                etiqueta.setLayoutY(pos.getY() - 35);
                etiqueta.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");

                mapaPane.getChildren().addAll(nodo, etiqueta);
            }
        }
    }

    // --------------------------------------------------------
    // AQUÍ PONES EL MÉTODO cargarDatosDePrueba()
    // --------------------------------------------------------
    private void cargarDatosDePrueba() {
        // 1. Crear las Paradas
        Parada p1 = new Parada("P1", "Estación Central", "Centro");
        Parada p2 = new Parada("P2", "Terminal Norte", "Norte");
        Parada p3 = new Parada("P3", "Plaza Sur", "Sur");
        Parada p4 = new Parada("P4", "Campus Univ.", "Este");
        Parada p5 = new Parada("P5", "Parque Ind.", "Oeste");
        Parada p6 = new Parada("P6", "Centro Médico", "Noreste");
        Parada p7 = new Parada("P7", "Zona Comercial", "Sureste");
        Parada p8 = new Parada("P8", "Barrio Res.", "Noroeste");

        Parada[] todasLasParadas = {p1, p2, p3, p4, p5, p6, p7, p8};
        for (Parada p : todasLasParadas) {
            redTransporte.agregarParada(p);
        }

        // 2. Asignar las coordenadas visuales
        coordenadasMapa.put(p1, new Point2D(400, 300));
        coordenadasMapa.put(p2, new Point2D(400, 100));
        coordenadasMapa.put(p3, new Point2D(400, 500));
        coordenadasMapa.put(p4, new Point2D(700, 300));
        coordenadasMapa.put(p5, new Point2D(100, 300));
        coordenadasMapa.put(p6, new Point2D(650, 120));
        coordenadasMapa.put(p7, new Point2D(650, 480));
        coordenadasMapa.put(p8, new Point2D(150, 120));

        // 3. Agregar Rutas (ESTRICTAMENTE DIRIGIDAS)
        // Parámetros: Destino, Tiempo (min), Costo ($), Distancia (km)

        // Salidas desde la Central (P1)
        redTransporte.agregarRuta(p1, new Ruta(p2, 10, 20.0, 4.0)); // P1 -> P2
        redTransporte.agregarRuta(p1, new Ruta(p3, 12, 20.0, 5.0)); // P1 -> P3

        // Retorno hacia la Central (P3 -> P1 es más lento por el tráfico)
        redTransporte.agregarRuta(p3, new Ruta(p1, 18, 20.0, 5.0));

        // Ruta circular periférica de una sola vía: P2 -> P6 -> P4 -> P7 -> P3
        redTransporte.agregarRuta(p2, new Ruta(p6, 12, 15.0, 5.0));
        redTransporte.agregarRuta(p6, new Ruta(p4, 8, 15.0, 3.0));
        redTransporte.agregarRuta(p4, new Ruta(p7, 11, 20.0, 4.5));
        redTransporte.agregarRuta(p7, new Ruta(p3, 10, 15.0, 4.0));

        // Conexiones hacia el Oeste (P5 y P8)
        redTransporte.agregarRuta(p1, new Ruta(p5, 15, 25.0, 6.0));
        redTransporte.agregarRuta(p5, new Ruta(p8, 8, 10.0, 3.0));
        redTransporte.agregarRuta(p8, new Ruta(p2, 14, 20.0, 5.5)); // P8 conecta de vuelta al Norte

        // LA RUTA TRAMPA (Directo de P1 a P6, pero carísima)
        redTransporte.agregarRuta(p1, new Ruta(p6, 5, 100.0, 2.0));

        // Retorno expreso desde la Universidad (P4) a la Central (P1)
        redTransporte.agregarRuta(p4, new Ruta(p1, 25, 30.0, 8.0));

        // 4. Poblar los selectores
        comboOrigen.getItems().addAll(todasLasParadas);
        comboDestino.getItems().addAll(todasLasParadas);
    }

    @FXML
    private void calcularRuta(ActionEvent event) {
        Parada origen = comboOrigen.getValue();
        Parada destino = comboDestino.getValue();
        Criterio criterio = comboCriterio.getValue();

        if (origen == null || destino == null || criterio == null) {
            System.out.println("Por favor seleccione todos los campos.");
            return;
        }

        ResultadoRuta resultado = redTransporte.calcularRuta(origen, destino, criterio);

        if (resultado != null) {
            lblResultadoRuta.setText("Ruta: " + resultado.getCamino().toString());
            lblTiempo.setText("Tiempo: " + resultado.getTiempoTotal() + " min");
            lblDistancia.setText("Distancia: " + resultado.getDistanciaTotal() + " km");
            lblCosto.setText("Costo: $" + resultado.getCostoTotal());

            // Dibujar la ruta resaltada en el mapaPane
        } else {
            lblResultadoRuta.setText("Ruta: No hay camino disponible");
        }
    }

    //abror ventana
    @FXML
    private void abrirGestor(ActionEvent event) {
        System.out.println("Abriendo ventana de gestión...");
    }
}