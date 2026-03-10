package controladores;

import Modelos.Grafo;
import Modelos.Parada;
import Modelos.Ruta;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.geometry.Point2D;
import javafx.event.ActionEvent;

import java.util.Map;

public class GestorController {

    private Grafo redTransporte;
    private Map<Parada, Point2D> coordenadasMapa;
    private MainController mainController;


    @FXML
    private TextField txtIdParada;
    @FXML
    private TextField txtNombreParada;
    @FXML
    private TextField txtUbicacionParada;
    @FXML
    private TextField txtXParada;
    @FXML
    private TextField txtYParada;
    @FXML
    private Label lblMensajeParada;


    @FXML
    private ComboBox<Parada> comboNuevoOrigen;
    @FXML
    private ComboBox<Parada> comboNuevoDestino;
    @FXML
    private TextField txtTiempoRuta;
    @FXML
    private TextField txtCostoRuta;
    @FXML
    private TextField txtDistanciaRuta;
    @FXML
    private Label lblMensajeRuta;

    public void inicializarDatos(Grafo grafo, Map<Parada, Point2D> coordenadas, MainController main) {
        this.redTransporte = grafo;
        this.coordenadasMapa = coordenadas;
        this.mainController = main;

        actualizarCombos();
    }

    private void actualizarCombos() {
        comboNuevoOrigen.getItems().setAll(redTransporte.getAdyacencia().keySet());
        comboNuevoDestino.getItems().setAll(redTransporte.getAdyacencia().keySet());
    }


    @FXML
    private void agregarParada(ActionEvent event) {
        try {
            String id = txtIdParada.getText();
            String nombre = txtNombreParada.getText();
            String ubicacion = txtUbicacionParada.getText();

            if (id.isEmpty() || nombre.isEmpty()) {
                lblMensajeParada.setText("Error: ID y Nombre son obligatorios.");
                return;
            }

            double x = Double.parseDouble(txtXParada.getText());
            double y = Double.parseDouble(txtYParada.getText());

            Parada nuevaParada = new Parada(id, nombre, ubicacion);
            redTransporte.agregarParada(nuevaParada);
            coordenadasMapa.put(nuevaParada, new Point2D(x, y));

            // actualiza
            mainController.actualizarVistaCompleta();
            actualizarCombos();

            lblMensajeParada.setText("Parada agregada con éxito.");
            lblMensajeParada.setStyle("-fx-text-fill: green;");

            txtIdParada.clear();
            txtNombreParada.clear();
            txtUbicacionParada.clear();
            txtXParada.clear();
            txtYParada.clear();

        } catch (NumberFormatException e) {
            lblMensajeParada.setText("Error: Las coordenadas X e Y deben ser números.");
            lblMensajeParada.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void agregarRuta(ActionEvent event) {
        try {
            Parada origen = comboNuevoOrigen.getValue();
            Parada destino = comboNuevoDestino.getValue();

            if (origen == null || destino == null || origen.equals(destino)) {
                lblMensajeRuta.setText("Seleccione origen y dstino válidos.");
                return;
            }

            double tiempo = Double.parseDouble(txtTiempoRuta.getText());
            double costo = Double.parseDouble(txtCostoRuta.getText());
            double distancia = Double.parseDouble(txtDistanciaRuta.getText());

            Ruta nuevaRuta = new Ruta(destino, tiempo, costo, distancia);
            redTransporte.agregarRuta(origen, nuevaRuta);

            mainController.actualizarVistaCompleta();

            lblMensajeRuta.setText("Ruta agregada con éxito.");
            lblMensajeRuta.setStyle("-fx-text-fill: green;");

            txtTiempoRuta.clear();
            txtCostoRuta.clear();
            txtDistanciaRuta.clear();

        } catch (NumberFormatException e) {
            lblMensajeRuta.setText("Error: Tiempo, costo y distancia deben ser números.");
            lblMensajeRuta.setStyle("-fx-text-fill: red;");
        }
    }
}