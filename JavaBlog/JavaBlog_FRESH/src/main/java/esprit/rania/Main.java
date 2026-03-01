package esprit.rania;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the main FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();

            // Set up the scene
            Scene scene = new Scene(root, 1200, 800);
            
            // Add stylesheet
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            primaryStage.setTitle("AgriCloud - Blog Management System");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading application: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        // Close database connection when application closes
        esprit.rania.database.DatabaseConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
