package application.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/nsr_population_tracker";
    private static final String USER = "root";
    private static final String PASS = "alchemistrabbit71";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            System.err.println("Database connection failed:");
            System.err.println("URL: " + URL);
            e.printStackTrace();
            return null;
        }
    }
}
