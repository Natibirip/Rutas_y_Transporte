package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Verifica si la URL es nula antes de cargar
            var fxmlUrl = getClass().getResource("/vistas/MainView.fxml");
            if (fxmlUrl == null) {
                System.err.println("CRÍTICO: No se encontró el archivo FXML en la ruta especificada.");
                System.exit(1);
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            primaryStage.setTitle("Sistema de Gestión de Rutas de Transporte");
            primaryStage.setScene(new Scene(root, 1000, 700));
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace(); // Esto nos dirá exactamente qué falló
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}