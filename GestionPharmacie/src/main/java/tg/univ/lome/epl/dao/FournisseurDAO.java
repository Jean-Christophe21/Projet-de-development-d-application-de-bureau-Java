/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.dao;

import tg.univ.lome.epl.model.Fournisseur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author USER
 */
public class FournisseurDAO implements IDao<Fournisseur, Integer> {

    private final Connection conn;

    public FournisseurDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    //  CRUD de base 
    @Override
    public int insert(Fournisseur f) throws SQLException {
        String sql = ""
                + "INSERT INTO Fournisseurs(nom, contact, email, adresse)"
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.getNom());
            ps.setString(2, f.getContact());
            ps.setString(3, f.getEmail());
            ps.setString(4, f.getAdresse());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    @Override
    public boolean update(Fournisseur f) throws SQLException {
        String sql = ""
                + "UPDATE Fournisseurs"
                + "SET nom = ?, contact = ?, email = ?, adresse = ?"
                + "WHERE id_fournisseur = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getNom());
            ps.setString(2, f.getContact());
            ps.setString(3, f.getEmail());
            ps.setString(4, f.getAdresse());
            ps.setInt(5, f.getIdFournisseur());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM Fournisseurs WHERE id_fournisseur = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<Fournisseur> findById(Integer id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM Fournisseurs WHERE id_fournisseur = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
        }
    }

    @Override
    public List<Fournisseur> findAll() throws SQLException {
        List<Fournisseur> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM Fournisseurs ORDER BY nom")) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    //  Requêtes métier 
    /**
     * Nombre total de fournisseurs — Dashboard "Total no of Suppliers".
     */
    public int countAll() throws SQLException {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Fournisseurs")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Recherche par nom (insensible à la casse).
     */
    public List<Fournisseur> searchByNom(String motCle) throws SQLException {
        List<Fournisseur> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM Fournisseurs WHERE LOWER(nom) LIKE LOWER(?) ORDER BY nom")) {
            ps.setString(1, "%" + motCle + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Retourne les fournisseurs qui approvisionnent un médicament donné.
     * Jointure Fournisseurs → Lots → Medicaments.
     */
    public List<Fournisseur> findByMedicament(int idMedicament) throws SQLException {
        List<Fournisseur> list = new ArrayList<>();
        String sql = ""
                + "SELECT DISTINCT f.*"
                + "FROM Fournisseurs f"
                + "JOIN Lots l ON l.id_fournisseur = f.id_fournisseur"
                + "WHERE l.id_medicament = ?"
                + "ORDER BY f.nom";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedicament);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── Mapping ──────────────────────────────────────────────────────────────
    private Fournisseur mapRow(ResultSet rs) throws SQLException {
        return new Fournisseur(
                rs.getInt("id_fournisseur"),
                rs.getString("nom"),
                rs.getString("contact"),
                rs.getString("email"),
                rs.getString("adresse")
        );
    }
}
