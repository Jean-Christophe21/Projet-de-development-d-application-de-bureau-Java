/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author amevo
 */
public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:bibliotheque.db";

    // implémentation du singleton pour l'accès à la base de données
    private static DatabaseConnection instance;
    private static Connection conn;

    private DatabaseConnection() {
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL);
        }
        return conn;
    }

    public synchronized void closeConnection() {
        try {
            if (conn != null) {
                if (!conn.isClosed()) {
                    conn.close();
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQLException : " + e.getMessage());
        }
    }

//-------------------------------------------------------------------------------------------------------------------------------    
// bien repenser cette partie et la implémenter de telle sorte que lors de la 
//supppression de la base de donnée, qu'elle soit créé au démarrage de l'application
// le script de création dois aussi être exécuté en ce moment.
// l'objectif à terme est d'avoir une app résiliente.
//    private void init(Connection conn) {
//        try (Statement stmt = conn.createStatement()) {
//            stmt.execute(createTableSQL);
//            System.out.println("Exécution avec succès");
//
//        } catch (SQLException e) {
//            System.out.println("Exception: " + e.getMessage());
//        }
//    }
}
