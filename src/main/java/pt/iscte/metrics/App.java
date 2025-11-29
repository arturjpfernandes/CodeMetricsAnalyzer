package pt.iscte.metrics;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Carrega o FXML que criámos
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MainView.fxml"));

        // Cria a cena (janela)
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);

        stage.setTitle("Code Metrics Analyzer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}