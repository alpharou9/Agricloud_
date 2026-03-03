package esprit.shahed;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        URL fxmlLocation = getClass().getResource("/fxml/FarmView.fxml");
        if (fxmlLocation == null) {
            throw new RuntimeException("FXML File not found! Check your /resources/fxml/ folder.");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Scene scene = new Scene(fxmlLoader.load(), 1000, 650); // Adjusted size for better view

        stage.setTitle("Shahed Farm & Fields Management System");
        stage.setScene(scene);
        stage.show();
    }
}