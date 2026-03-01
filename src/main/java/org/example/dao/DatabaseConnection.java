package org.example.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/agricloud";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection;

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            System.out.println("[DB] Opening NEW database connection to: " + URL);
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Connection opened successfully. AutoCommit=" + connection.getAutoCommit());
        } else {
            System.out.println("[DB] Reusing existing connection. isClosed=" + connection.isClosed());
        }
        return connection;
    }
}
