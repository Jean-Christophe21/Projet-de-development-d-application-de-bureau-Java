/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.dao;

import tg.univ.lome.epl.model.Categorie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author USER
 */
public class CategorieDAO implements IDao<Categorie, Integer> {

    private final Connection conn;

    public CategorieDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    //  CRUD de base 
    @Override
    public int insert(Categorie c) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Categories(libelle) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getLibelle());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    @Override
    public boolean update(Categorie c) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Categories SET libelle = ? WHERE id_categorie = ?")) {
            ps.setString(1, c.getLibelle());
            ps.setInt(2, c.getIdCategorie());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM Categories WHERE id_categorie = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<Categorie> findById(Integer id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM Categories WHERE id_categorie = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
        }
    }

    @Override
    public List<Categorie> findAll() throws SQLException {
        List<Categorie> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(
                "SELECT * FROM Categories ORDER BY libelle")) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    
    /**
     * Toutes les catégories avec le nombre de médicaments. Image 7 — "Medicine
     * Groups (02)" avec colonne "No of Medicines".
     */
    public List<Object[]> findAllWithCount() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = ""
                + "SELECT c.id_categorie,"
                + " c.libelle,"
                + "  COUNT(mc.id_medicament) AS nb_medicaments"
                + " FROM Categories c"
                + " LEFT JOIN MedicamentCategories mc ON mc.id_categorie = c.id_categorie"
                + " GROUP BY c.id_categorie"
                + " ORDER BY c.libelle";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("id_categorie"),
                    rs.getString("libelle"),
                    rs.getInt("nb_medicaments")
                });
            }
        }
        return list;
    }

    /**
     * Nombre total de groupes — Dashboard "Medicine Groups".
     */
    public int countAll() throws SQLException {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Categories")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Retourne les catégories d'un médicament donné (relation N-N).
     */
    public List<Categorie> findByMedicament(int idMedicament) throws SQLException {
        List<Categorie> list = new ArrayList<>();
        String sql = ""
                + "SELECT c.*"
                + " FROM Categories c"
                + " JOIN MedicamentCategories mc ON mc.id_categorie = c.id_categorie"
                + "WHERE mc.id_medicament = ?"
                + "ORDER BY c.libelle";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedicament);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Ajoute un médicament dans une catégorie (table de jointure).
     */
    public boolean addMedicamentToCategorie(int idMedicament, int idCategorie)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO MedicamentCategories(id_medicament, id_categorie) VALUES (?,?)")) {
            ps.setInt(1, idMedicament);
            ps.setInt(2, idCategorie);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retire un médicament d'une catégorie — bouton "Remove from Group" image
     * 8.
     */
    public boolean removeMedicamentFromCategorie(int idMedicament, int idCategorie)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM MedicamentCategories WHERE id_medicament = ? AND id_categorie = ?")) {
            ps.setInt(1, idMedicament);
            ps.setInt(2, idCategorie);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Mapping 
    private Categorie mapRow(ResultSet rs) throws SQLException {
        return new Categorie(rs.getInt("id_categorie"), rs.getString("libelle"));
    }
}
