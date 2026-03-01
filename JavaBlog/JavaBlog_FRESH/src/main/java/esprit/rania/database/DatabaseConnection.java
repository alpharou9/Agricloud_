package esprit.rania.database;

import esprit.rania.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            // Always check if connection is valid before returning
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(
                        DatabaseConfig.DB_URL,
                        DatabaseConfig.DB_USER,
                        DatabaseConfig.DB_PASS
                );
                System.out.println("Database connected successfully!");
                System.out.println("Connected to: " + DatabaseConfig.DB_URL);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            System.err.println("Make sure MySQL Connector is in your dependencies.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Make sure:");
            System.err.println("1. MySQL server is running");
            System.err.println("2. Database 'javablog' exists");
            System.err.println("3. Username and password are correct");
            System.err.println("4. Run the database_schema.sql file");
            e.printStackTrace();
            connection = null; // Ensure connection is null on failure
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null; // Set to null after closing
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
