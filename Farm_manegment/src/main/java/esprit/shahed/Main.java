package esprit.shahed;

import esprit.shahed.database.DatabaseConnection;
import java.sql.Connection; // Essential import

public class Main {
    public static void main(String[] args) {
        // Fix: Use the Singleton instance
        Connection conn = DatabaseConnection.getInstance().getConnection();

        if (conn != null) {
            System.out.println("Success! Database is connected.");
        }

        MainApp.main(args); // Launch UI
    }
}