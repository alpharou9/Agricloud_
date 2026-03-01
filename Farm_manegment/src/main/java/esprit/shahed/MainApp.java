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
        if (fxmlLocation == null) throw new RuntimeException("FXML not found!");

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Scene scene = new Scene(fxmlLoader.load(), 850, 500);

        stage.setTitle("Farm Management Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}