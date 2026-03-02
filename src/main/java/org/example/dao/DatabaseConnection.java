package org.example.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages a single shared JDBC connection. All DAOs obtain their connection
 * here; no business logic lives in this class.
 */
public class DatabaseConnection {

    private static final String URL  = "jdbc:mysql://localhost:3306/agricloud";
    private static final String USER = "root";
    private static final String PASS = "";

    private static Connection connection;

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            System.out.println("[DB] Opening new connection to " + URL);
            connection = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("[DB] Connection established.");
        }
        return connection;
    }
}
