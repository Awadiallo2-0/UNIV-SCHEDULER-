package com.univ.utils;

import java.sql.*;
import java.io.InputStream;
import java.util.Properties;

/**
 * Classe singleton gérant la connexion à la base de données.
 * Lit les paramètres depuis le fichier config/database.properties.
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config/database.properties")) {
            if (input == null) {
                throw new RuntimeException("Fichier database.properties introuvable dans resources/config");
            }
            Properties props = new Properties();
            props.load(input);

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");
            String driver = props.getProperty("db.driver");

            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion à la base de données établie.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur de connexion à la base de données", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Ferme la connexion (à utiliser avec précaution, généralement à la fin de l'application).
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connexion fermée.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Méthode utilitaire pour fermer un ResultSet et un Statement.
     */
    public static void close(ResultSet rs, Statement stmt) {
        try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
        try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
    }

    /**
     * Méthode utilitaire pour fermer un Statement.
     */
    public static void close(Statement stmt) {
        try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
    }
}