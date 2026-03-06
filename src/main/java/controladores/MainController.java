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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import javafx.scene.shape.Polygon;
import javafx.scene.Group;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.IOException;

public class MainController {

    private Grafo redTransporte;

    private Map<Parada, Point2D> coordenadasMapa = new HashMap<>();

    @FXML
    private ComboBox<Parada> comboOrigen;
    @FXML
    private ComboBox<Parada> comboDestino;
    @FXML
    private ComboBox<Criterio> comboCriterio;
    @FXML
    private Pane mapaPane;
    @FXML
    private Label lblResultadoRuta;
    @FXML
    private Label lblTiempo;
    @FXML
    private Label lblDistancia;
    @FXML
    private Label lblCosto;

    @FXML
    public void initialize() {
        redTransporte = new Grafo();
        comboCriterio.getItems().setAll(Criterio.values());
        comboCriterio.setValue(Criterio.TIEMPO);

        cargarDatosDePrueba();
        dibujarGrafo();
    }

    private Group crearFlecha(double startX, double startY, double endX, double endY, Color color, double grosor) {
        Group flechaGrupo = new Group();

        double dx = endX - startX;
        double dy = endY - startY;
        double angulo = Math.atan2(dy, dx);
        double radioNodo = 15.0;

        double ajusteStartX = startX + radioNodo * Math.cos(angulo);
        double ajusteStartY = startY + radioNodo * Math.sin(angulo);
        double ajusteEndX = endX - radioNodo * Math.cos(angulo);
        double ajusteEndY = endY - radioNodo * Math.sin(angulo);

        Line linea = new Line(ajusteStartX, ajusteStartY, ajusteEndX, ajusteEndY);
        linea.setStrokeWidth(grosor);
        linea.setStroke(color);

        double tamañoPunta = 10.0 + (grosor - 2.0) * 2;
        Polygon punta = new Polygon();
        punta.getPoints().addAll(
                ajusteEndX, ajusteEndY,
                ajusteEndX - tamañoPunta * Math.cos(angulo - Math.PI / 6), ajusteEndY - tamañoPunta * Math.sin(angulo - Math.PI / 6),
                ajusteEndX - tamañoPunta * Math.cos(angulo + Math.PI / 6), ajusteEndY - tamañoPunta * Math.sin(angulo + Math.PI / 6)
        );
        punta.setFill(color);

        flechaGrupo.getChildren().addAll(linea, punta);
        return flechaGrupo;
    }

    private void dibujarGrafo() {
        dibujarGrafo(null);
    }

    private void dibujarGrafo(List<Parada> rutaOptima) {
        mapaPane.getChildren().clear();
        Map<Parada, List<Ruta>> adyacencia = redTransporte.getAdyacencia();

        for (Parada origen : adyacencia.keySet()) {
            for (Ruta ruta : adyacencia.get(origen)) {
                Parada destino = ruta.getDestino();

                Point2D posOrigen = coordenadasMapa.get(origen);
                Point2D posDestino = coordenadasMapa.get(destino);

                if (posOrigen != null && posDestino != null) {
                    boolean esParteDeRuta = false;

                    if (rutaOptima != null && rutaOptima.size() > 1) {
                        for (int i = 0; i < rutaOptima.size() - 1; i++) {
                            if (rutaOptima.get(i).equals(origen) && rutaOptima.get(i + 1).equals(destino)) {
                                esParteDeRuta = true;
                                break;
                            }
                        }
                    }

                    Color colorFlecha = esParteDeRuta ? Color.LIMEGREEN : Color.LIGHTGRAY;
                    double grosor = esParteDeRuta ? 4.0 : 2.0;

                    Group flecha = crearFlecha(posOrigen.getX(), posOrigen.getY(), posDestino.getX(), posDestino.getY(), colorFlecha, grosor);
                    mapaPane.getChildren().add(flecha);
                }
            }
        }

        for (Parada p : adyacencia.keySet()) {
            Point2D pos = coordenadasMapa.get(p);

            if (pos != null) {
                boolean esNodoDeRuta = rutaOptima != null && rutaOptima.contains(p);
                Color colorBase = esNodoDeRuta ? Color.LIMEGREEN : Color.DODGERBLUE;

                Circle nodo = new Circle(pos.getX(), pos.getY(), 15, colorBase);
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
                nodo.setOnMouseExited(e -> nodo.setFill(colorBase));

                Label etiqueta = new Label(p.getNombre());
                etiqueta.setLayoutX(pos.getX() - 20);
                etiqueta.setLayoutY(pos.getY() - 35);
                etiqueta.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");

                mapaPane.getChildren().addAll(nodo, etiqueta);
            }
        }
    }

    private void cargarDatosDePrueba() {
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

        coordenadasMapa.put(p1, new Point2D(400, 300));
        coordenadasMapa.put(p2, new Point2D(400, 100));
        coordenadasMapa.put(p3, new Point2D(400, 500));
        coordenadasMapa.put(p4, new Point2D(700, 300));
        coordenadasMapa.put(p5, new Point2D(100, 300));
        coordenadasMapa.put(p6, new Point2D(650, 120));
        coordenadasMapa.put(p7, new Point2D(650, 480));
        coordenadasMapa.put(p8, new Point2D(150, 120));


        redTransporte.agregarRuta(p1, new Ruta(p2, 10, 20.0, 4.0)); // P1 -> P2
        redTransporte.agregarRuta(p1, new Ruta(p3, 12, 20.0, 5.0)); // P1 -> P3

        redTransporte.agregarRuta(p3, new Ruta(p1, 18, 20.0, 5.0));

        redTransporte.agregarRuta(p2, new Ruta(p6, 12, 15.0, 5.0));
        redTransporte.agregarRuta(p6, new Ruta(p4, 8, 15.0, 3.0));
        redTransporte.agregarRuta(p4, new Ruta(p7, 11, 20.0, 4.5));
        redTransporte.agregarRuta(p7, new Ruta(p3, 10, 15.0, 4.0));

        redTransporte.agregarRuta(p1, new Ruta(p5, 15, 25.0, 6.0));
        redTransporte.agregarRuta(p5, new Ruta(p8, 8, 10.0, 3.0));
        redTransporte.agregarRuta(p8, new Ruta(p2, 14, 20.0, 5.5)); // P8 conecta de vuelta al Norte

        redTransporte.agregarRuta(p1, new Ruta(p6, 5, 100.0, 2.0));

        redTransporte.agregarRuta(p4, new Ruta(p1, 25, 30.0, 8.0));

        comboOrigen.getItems().addAll(todasLasParadas);
        comboDestino.getItems().addAll(todasLasParadas);
    }

    @FXML
    private void calcularRuta(ActionEvent event) {
        Parada origen = comboOrigen.getValue();
        Parada destino = comboDestino.getValue();
        Criterio criterio = comboCriterio.getValue();

        if (origen == null || destino == null || criterio == null) {
            lblResultadoRuta.setText("Por favor, seleccione Origen, Destino y Criterio.");
            return;
        }

        if (origen.equals(destino)) {
            lblResultadoRuta.setText("El origen y destino son iguales.");
            dibujarGrafo(); // Redibujar normal
            return;
        }

        ResultadoRuta resultado = redTransporte.calcularRuta(origen, destino, criterio);

        if (resultado != null && !resultado.getCamino().isEmpty()) {
            lblResultadoRuta.setText("Ruta: " + resultado.getCamino().toString());
            lblTiempo.setText("Tiempo: " + resultado.getTiempoTotal() + " min");
            lblDistancia.setText("Distancia: " + resultado.getDistanciaTotal() + " km");
            lblCosto.setText("Costo: $" + resultado.getCostoTotal());

            dibujarGrafo(resultado.getCamino());
        } else {
            lblResultadoRuta.setText("No hay ruta disponible entre estas paradas.");
            lblTiempo.setText("Tiempo: 0 min");
            lblDistancia.setText("Distancia: 0 km");
            lblCosto.setText("Costo: $0.00");

            dibujarGrafo();
        }
    }

    //abror ventana
    @FXML
    private void abrirGestor(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/GestorView.fxml"));
            Parent root = loader.load();

            GestorController gestorController = loader.getController();

            gestorController.inicializarDatos(redTransporte, coordenadasMapa, this);

            Stage stage = new Stage();
            stage.setTitle("Gestor de Paradas y Rutas");
            stage.setScene(new Scene(root));

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void actualizarVistaCompleta() {
        dibujarGrafo();
        // Aquí también deberías re-poblar los ComboBox de la ventana principal
        comboOrigen.getItems().setAll(redTransporte.getAdyacencia().keySet());
        comboDestino.getItems().setAll(redTransporte.getAdyacencia().keySet());
    }
}