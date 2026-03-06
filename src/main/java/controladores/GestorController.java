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
    private MainController mainController; // Referencia para decirle que redibuje el mapa

    // --- Componentes Pestaña Paradas ---
    @FXML private TextField txtIdParada;
    @FXML private TextField txtNombreParada;
    @FXML private TextField txtUbicacionParada;
    @FXML private TextField txtXParada;
    @FXML private TextField txtYParada;
    @FXML private Label lblMensajeParada;

    // --- Componentes Pestaña Rutas ---
    @FXML private ComboBox<Parada> comboNuevoOrigen;
    @FXML private ComboBox<Parada> comboNuevoDestino;
    @FXML private TextField txtTiempoRuta;
    @FXML private TextField txtCostoRuta;
    @FXML private TextField txtDistanciaRuta;
    @FXML private Label lblMensajeRuta;

    // Método crucial para enlazar ambas ventanas
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
        // Aquí pondremos la lógica para crear la parada y validar números
        System.out.println("Botón Agregar Parada presionado");
    }

    @FXML
    private void agregarRuta(ActionEvent event) {
        // Aquí pondremos la lógica para crear la ruta y validar números
        System.out.println("Botón Agregar Ruta presionado");
    }
}