package tn.esprit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class mydb {

    private final String URL = "jdbc:mysql://127.0.0.1:3306/event";
    private final String USERNAME = "root";
    private final String PWD = "";

    public static mydb instance;

    private Connection conx;

    private mydb(){
        try {
            conx = DriverManager.getConnection(URL,USERNAME,PWD);
            System.out.println("Connected to DB!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static mydb getInstance(){
        if (instance == null){
            instance = new mydb();
        }
        return instance;
    }


    public Connection getConx() {
        return conx;
    }
}