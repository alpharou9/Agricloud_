package esprit.shahed;

import esprit.shahed.database.DatabaseConnection;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing System...");

        // 1. Check Database Connection
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn != null) {
            System.out.println("Success! Database is connected.");
        } else {
            System.err.println("Critical Error: Could not connect to the database.");
            // Optional: System.exit(1); // Stop if DB is down
        }

        // 2. Launch UI directly
        MainApp.launch(MainApp.class, args);
    }
}