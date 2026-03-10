package controladores;
import Modelos.Grafo;
import Modelos.Parada;
import Modelos.Ruta;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.geometry.Point2D;
import javafx.event.ActionEvent;
import java.util.List;
import java.util.Map;

public class GestorController {
    private Grafo redTransporte;
    private Map<Parada, Point2D> coordenadasMapa;
    private MainController mainController;
    @FXML private ComboBox<Parada> comboEditarParada;
    @FXML private TextField txtIdParada;
    @FXML private TextField txtNombreParada;
    @FXML private TextField txtUbicacionParada;
    @FXML private TextField txtXParada;
    @FXML private TextField txtYParada;
    @FXML private Button btnAgregarParada, btnModificarParada, btnEliminarParada;
    @FXML private Label lblMensajeParada;
    @FXML private ComboBox<Parada> comboNuevoOrigen;
    @FXML private ComboBox<Parada> comboNuevoDestino;
    @FXML private TextField txtTiempoRuta;
    @FXML private TextField txtCostoRuta;
    @FXML private TextField txtDistanciaRuta;
    @FXML private Button btnAgregarRuta, btnModificarRuta, btnEliminarRuta;
    @FXML private Label lblMensajeRuta;

    public void inicializarDatos(Grafo grafo, Map<Parada, Point2D> coordenadas, MainController main) {
        this.redTransporte = grafo;
        this.coordenadasMapa = coordenadas;
        this.mainController = main;

        actualizarCombos();
    }

    private void actualizarCombos() {
        // Guarda la selecionada
        Parada pSeleccionada = comboEditarParada.getValue();
        Parada origenAct = comboNuevoOrigen.getValue();
        Parada destinoAct = comboNuevoDestino.getValue();

        comboEditarParada.getItems().clear();
        comboEditarParada.getItems().add(null);
        comboEditarParada.getItems().addAll(redTransporte.getAdyacencia().keySet());
        comboNuevoOrigen.getItems().setAll(redTransporte.getAdyacencia().keySet());
        comboNuevoDestino.getItems().setAll(redTransporte.getAdyacencia().keySet());

        if (pSeleccionada != null && redTransporte.getAdyacencia().containsKey(pSeleccionada)) {
            comboEditarParada.setValue(pSeleccionada);
        }
        if (origenAct != null && redTransporte.getAdyacencia().containsKey(origenAct)) {
            comboNuevoOrigen.setValue(origenAct);
        }
        if (destinoAct != null && redTransporte.getAdyacencia().containsKey(destinoAct)) {
            comboNuevoDestino.setValue(destinoAct);
        }
    }

    @FXML
    private void seleccionarParadaParaEditar(ActionEvent event) {
        Parada p = comboEditarParada.getValue();
        lblMensajeParada.setText("");

        if (p == null) {
            // Se agrega
            limpiarCamposParada();
            txtIdParada.setDisable(false);
            btnAgregarParada.setDisable(false);
            btnModificarParada.setDisable(true);
            btnEliminarParada.setDisable(true);
        } else {
            // modifica
            txtIdParada.setText(p.getId());
            txtIdParada.setDisable(true); // El ID no se puede cambiar
            txtNombreParada.setText(p.getNombre());
            txtUbicacionParada.setText(p.getUbicacion());

            Point2D pos = coordenadasMapa.get(p);
            if(pos != null) {
                txtXParada.setText(String.valueOf(pos.getX()));
                txtYParada.setText(String.valueOf(pos.getY()));
            }

            btnAgregarParada.setDisable(true);
            btnModificarParada.setDisable(false);
            btnEliminarParada.setDisable(false);
        }
    }

    @FXML
    private void agregarParada(ActionEvent event) {
        try {
            String id = txtIdParada.getText();
            String nombre = txtNombreParada.getText();
            String ubicacion = txtUbicacionParada.getText();

            if (id.isEmpty() || nombre.isEmpty()) {
                mostrarMensaje(lblMensajeParada, "ID y nombre son obligatorios.", true);
                return;
            }

            // confirmar id
            for (Parada p : redTransporte.getAdyacencia().keySet()) {
                if (p.getId().equals(id)) {
                    mostrarMensaje(lblMensajeParada, "Error: El Id ya existe.", true);
                    return;
                }
            }

            double x = Double.parseDouble(txtXParada.getText());
            double y = Double.parseDouble(txtYParada.getText());

            Parada nueva = new Parada(id, nombre, ubicacion);
            redTransporte.agregarParada(nueva);
            coordenadasMapa.put(nueva, new Point2D(x, y));

            finalizarAccion("Parada agregada con éxito.", lblMensajeParada);
            limpiarCamposParada();

        } catch (NumberFormatException e) {
            mostrarMensaje(lblMensajeParada, "Las coordenadas deben ser numéricas.", true);
        }
    }

    @FXML
    private void modificarParada(ActionEvent event) {
        try {
            Parada p = comboEditarParada.getValue();
            if (p == null) return;

            p.setNombre(txtNombreParada.getText());
            p.setUbicacion(txtUbicacionParada.getText());

            double x = Double.parseDouble(txtXParada.getText());
            double y = Double.parseDouble(txtYParada.getText());
            coordenadasMapa.put(p, new Point2D(x, y));

            finalizarAccion("Parada modificada con éxito.", lblMensajeParada);
            comboEditarParada.setValue(null); // Volver a modo "Agregar"

        } catch (NumberFormatException e) {
            mostrarMensaje(lblMensajeParada, "Las coordenadas deben ser numéricas.", true);
        }
    }

    @FXML
    private void eliminarParada(ActionEvent event) {
        Parada p = comboEditarParada.getValue();
        if (p == null) return;

        redTransporte.eliminarParada(p);
        coordenadasMapa.remove(p);

        finalizarAccion("Parada eliminada con éxito.", lblMensajeParada);
        comboEditarParada.setValue(null);
    }

    private void limpiarCamposParada() {
        txtIdParada.clear(); txtNombreParada.clear(); txtUbicacionParada.clear();
        txtXParada.clear(); txtYParada.clear();
    }

    @FXML
    private void verificarRutaExistente(ActionEvent event) {
        Parada origen = comboNuevoOrigen.getValue();
        Parada destino = comboNuevoDestino.getValue();
        lblMensajeRuta.setText("");

        if (origen == null || destino == null || origen.equals(destino)) {
            limpiarCamposRuta();
            btnAgregarRuta.setDisable(true);
            btnModificarRuta.setDisable(true);
            btnEliminarRuta.setDisable(true);
            return;
        }

        Ruta rutaExistente = buscarRuta(origen, destino);

        if (rutaExistente != null) {
            // se moddifica
            txtTiempoRuta.setText(String.valueOf(rutaExistente.getTiempo()));
            txtCostoRuta.setText(String.valueOf(rutaExistente.getCosto()));
            txtDistanciaRuta.setText(String.valueOf(rutaExistente.getDistancia()));

            btnAgregarRuta.setDisable(true);
            btnModificarRuta.setDisable(false);
            btnEliminarRuta.setDisable(false);
        } else {
            // se agrega
            limpiarCamposRuta();
            btnAgregarRuta.setDisable(false);
            btnModificarRuta.setDisable(true);
            btnEliminarRuta.setDisable(true);
        }
    }

    @FXML
    private void agregarRuta(ActionEvent event) {
        procesarRuta(false);
    }

    @FXML
    private void modificarRuta(ActionEvent event) {
        procesarRuta(true);
    }

    private void procesarRuta(boolean esModificacion) {
        try {
            Parada origen = comboNuevoOrigen.getValue();
            Parada destino = comboNuevoDestino.getValue();

            double tiempo = Double.parseDouble(txtTiempoRuta.getText());
            double costo = Double.parseDouble(txtCostoRuta.getText());
            double distancia = Double.parseDouble(txtDistanciaRuta.getText());

            if (esModificacion) {
                redTransporte.eliminarRuta(origen, destino);
            }

            redTransporte.agregarRuta(origen, new Ruta(destino, tiempo, costo, distancia));

            String msg = esModificacion ? "Ruta modificada con éxito." : "Ruta agregada con éxito.";
            finalizarAccion(msg, lblMensajeRuta);
            verificarRutaExistente(null);

        } catch (NumberFormatException e) {
            mostrarMensaje(lblMensajeRuta, "Los valores deben ser numéricos.", true);
        }
    }

    @FXML
    private void eliminarRuta(ActionEvent event) {
        Parada origen = comboNuevoOrigen.getValue();
        Parada destino = comboNuevoDestino.getValue();

        redTransporte.eliminarRuta(origen, destino);

        finalizarAccion("Ruta eliminada con éxito.", lblMensajeRuta);
        verificarRutaExistente(null);
    }

    private Ruta buscarRuta(Parada origen, Parada destino) {
        List<Ruta> rutas = redTransporte.getAdyacencia().get(origen);
        if (rutas != null) {
            for (Ruta r : rutas) {
                if (r.getDestino().equals(destino)) return r;
            }
        }
        return null;
    }

    private void limpiarCamposRuta() {
        txtTiempoRuta.clear(); txtCostoRuta.clear(); txtDistanciaRuta.clear();
    }

    private void finalizarAccion(String mensaje, Label label) {
        mostrarMensaje(label, mensaje, false);
        mainController.actualizarVistaCompleta();
        actualizarCombos();
    }

    private void mostrarMensaje(Label label, String msg, boolean esError) {
        label.setText(msg);
        label.setStyle(esError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
    }
}