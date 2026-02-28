package tn.esprit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import tn.esprit.utils.mydb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Test DB connection first
        testConnection();

        try {
            // 2. Load FXML with detailed error catching
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventManagement.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 750);
            primaryStage.setTitle("AgriCloud - Event Manager");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("❌ FXML Loading Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Critical Application Error:");
            e.printStackTrace();
        }
    }

    private void testConnection() {
        mydb db = mydb.getInstance();
        Connection con = db.getConx();
        if (con != null) {
            System.out.println("Connection test successful ✅");
        } else {
            System.out.println("Connection failed ❌ - Check your DB credentials in mydb.java");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}