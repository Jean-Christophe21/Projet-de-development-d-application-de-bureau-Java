/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.dao;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 *
 * @author amevo
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:pharmacie.db";

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

    public synchronized Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(URL);
                // Initialise les tables au cas où le fichier .db vient d'être (re)créé
                initDb(conn);
            }
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base SQLite : " + e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }

    public synchronized void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
        }
    }

    /**
     * Exécute le script d'initialisation SQL pour garantir la résilience de
     * l'application. Si le fichier .db est supprimé, SQLite le recrée à vide et
     * cette méthode y applique les tables.
     */
    private void initDb(Connection connection) {
        // Option 1 : Charger le script depuis un fichier dans src/main/resources/sql/schema.sql
        String scriptSql = chargerScriptSql("/sql/schema.sql");

        // Option 2 (Fallback) : DDL d'urgence si le fichier de ressources est introuvable
        if (scriptSql == null || scriptSql.isBlank()) {
            scriptSql = "    CREATE TABLE IF NOT EXISTS Utilisateurs (\n"
                    + "        id_utilisateur INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                    + "        nom TEXT NOT NULL,\n"
                    + "        prenom TEXT NOT NULL,\n"
                    + "        identifiant TEXT UNIQUE NOT NULL,\n"
                    + "        mot_de_passe TEXT NOT NULL,\n"
                    + "        role TEXT NOT NULL\n" + "    );\n" + "\n"
                    + "    -- Utilisateur admin par d\u00e9faut si la table est vide\n"
                    + "    INSERT OR IGNORE INTO Utilisateurs (id_utilisateur, nom, prenom, identifiant, mot_de_passe, role)\n"
                    + "    VALUES (1, 'Admin', 'System', 'admin', 'admin123', 'ADMIN');\n";
        }

        try (Statement stmt = connection.createStatement()) {
            // SQLite supporte l'exécution de plusieurs requêtes séparées par des points-virgules via executeUpdate
            for (String sqlQuery : scriptSql.split(";")) {
                if (!sqlQuery.trim().isEmpty()) {
                    stmt.executeUpdate(sqlQuery.trim());
                }
            }
            System.out.println("Base de données initialisée avec succès.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'initialisation du schéma SQL : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lit un fichier SQL placé dans le dossier resources de l'application.
     */
    private String chargerScriptSql(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            System.err.println("Impossible de lire le fichier de script SQL (" + resourcePath + ") : " + e.getMessage());
            return null;
        }
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