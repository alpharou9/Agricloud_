package esprit.farouk;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load login screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();

        // Create scene with CSS
        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Configure stage
        primaryStage.setTitle("AgriCloud - Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();

        System.out.println("========================================");
        System.out.println("  AgriCloud User Management System");
        System.out.println("========================================");
        System.out.println("Application started successfully!");
        System.out.println("Login with:");
        System.out.println("  Admin: admin@admin.com / farouk");
        System.out.println("  Farmer: farmer@farmer.com / farouk");
        System.out.println("  Customer: customer@customer.com / farouk");
        System.out.println("========================================\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
