/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.dao;


import tg.univ.lome.epl.model.Utilisateur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author USER
 */



public class UtilisateurDAO implements IDao<Utilisateur, Integer> {

    private final Connection conn;

    public UtilisateurDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    //  CRUD de base 

    @Override
    public int insert(Utilisateur u) throws SQLException {
        String sql = ""
                + "INSERT INTO Utilisateurs(nom, prenom, identifiant, mot_de_passe, role) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getIdentifiant());
            ps.setString(4, u.getMotDePasse());
            ps.setString(5, u.getRole());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    @Override
    public boolean update(Utilisateur u) throws SQLException {
        String sql = " "
                + "UPDATE Utilisateurs"
                + "SET nom = ?, prenom = ?, identifiant = ?, mot_de_passe = ?, role = ?"
                + "WHERE id_utilisateur = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getIdentifiant());
            ps.setString(4, u.getMotDePasse());
            ps.setString(5, u.getRole());
            ps.setInt(6, u.getIdUtilisateur());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM Utilisateurs WHERE id_utilisateur = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<Utilisateur> findById(Integer id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM Utilisateurs WHERE id_utilisateur = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
        }
    }

    @Override
    public List<Utilisateur> findAll() throws SQLException {
        List<Utilisateur> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Utilisateurs ORDER BY nom")) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    //  Requêtes métier

    /**
     * Authentification : retourne l'utilisateur si identifiant + mot de passe corrects.
     * Utilisé à l'écran de login sur la maquette
     */
    public Optional<Utilisateur> authenticate(String identifiant, String motDePasse) {
        String sql = "SELECT * FROM Utilisateurs WHERE LOWER(TRIM(identifiant)) = LOWER(TRIM(?)) AND mot_de_passe = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifiant);
            ps.setString(2, motDePasse);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Retourne tous les utilisateurs d'un rôle donné (ADMIN ou VENDEUR).
     * Utilisé dans la page User Management.
     */
    public List<Utilisateur> findByRole(String role) throws SQLException {
        List<Utilisateur> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM Utilisateurs WHERE role = ? ORDER BY nom")) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /**
     * Vérifie l'unicité d'un identifiant (utile avant INSERT) pour éviter des douvlons dans la base de donnés.
     */
    public boolean existsByIdentifiant(String identifiant) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM Utilisateurs WHERE identifiant = ?")) {
            ps.setString(1, identifiant);
            return ps.executeQuery().next();
        }
    }

    /**
     * Nombre total d'utilisateurs sur le Dashboard "Total no of Users".
     */
    public int countAll() throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Utilisateurs")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    //  Mapping

    private Utilisateur mapRow(ResultSet rs) throws SQLException {
        return new Utilisateur(
            rs.getInt("id_utilisateur"),
            rs.getString("nom"),
            rs.getString("prenom"),
            rs.getString("identifiant"),
            rs.getString("mot_de_passe"),
            rs.getString("role")
        );
    }
}
