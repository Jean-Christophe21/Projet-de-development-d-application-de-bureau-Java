/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.dao;

import tg.univ.lome.epl.model.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author USER
 */

public class ClientDAO implements IDao<Client, Integer> {

    private final Connection conn;

    public ClientDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    //  CRUD de base 
    @Override
    public int insert(Client c) throws SQLException {
        String sql = ""
                + "INSERT INTO Clients(nom, prenom, telephone, email)"
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getPrenom());
            ps.setString(3, c.getTelephone());
            ps.setString(4, c.getEmail());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    @Override
    public boolean update(Client c) throws SQLException {
        String sql = ""
                + "UPDATE Clients"
                + "SET nom = ?, prenom = ?, telephone = ?, email = ?"
                + "WHERE id_client = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getPrenom());
            ps.setString(3, c.getTelephone());
            ps.setString(4, c.getEmail());
            ps.setInt(5, c.getIdClient());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM Clients WHERE id_client = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<Client> findById(Integer id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM Clients WHERE id_client = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
        }
    }

    @Override
    public List<Client> findAll() throws SQLException {
        List<Client> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM Clients ORDER BY nom")) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    //  Requêtes métier 
    /**
     * Nombre total de clients — Dashboard "Total no of Customers".
     */
    public int countAll() throws SQLException {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Clients")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Recherche un client par nom ou téléphone.
     */
    public List<Client> search(String motCle) throws SQLException {
        List<Client> list = new ArrayList<>();
        String sql = ""
                + "SELECT * FROM Clients"
                + " WHERE LOWER(nom)  LIKE LOWER(?)"
                + "   OR LOWER(prenom) LIKE LOWER(?)"
                + "   OR telephone LIKE ?"
                + " ORDER BY nom";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + motCle + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Retourne le médicament le plus acheté par un client donné. Dashboard
     * "Frequently bought item". Jointure : Clients → Ventes → LignesVentes →
     * Lots → Medicaments
     */
    public Optional<String> findFrequentlyBoughtItem(int idClient) throws SQLException {
        String sql = ""
                + "SELECT m.nom_commercial, SUM(lv.quantite) AS total_qty"
                + " FROM LignesVentes lv"
                + "  JOIN Ventes v  ON v.id_vente      = lv.id_vente"
                + "  JOIN Lots   l  ON l.id_lot         = lv.id_lot"
                + "  JOIN Medicaments m ON m.id_medicament = l.id_medicament"
                + " WHERE v.id_client = ?"
                + " GROUP BY m.id_medicament"
                + "ORDER BY total_qty DESC"
                + "LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idClient);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(rs.getString("nom_commercial")) : Optional.empty();
        }
    }

    /**
     * Médicament le plus acheté tous clients confondus — Dashboard global.
     */
    public Optional<String> findGlobalFrequentlyBoughtItem() throws SQLException {
        String sql = ""
                + "SELECT m.nom_commercial, SUM(lv.quantite) AS total_qty"
                + " FROM LignesVentes lv"
                + "  JOIN Lots l        ON l.id_lot         = lv.id_lot"
                + "  JOIN Medicaments m ON m.id_medicament   = l.id_medicament"
                + " GROUP BY m.id_medicament"
                + " ORDER BY total_qty DESC"
                + " LIMIT 1";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? Optional.of(rs.getString("nom_commercial")) : Optional.empty();
        }
    }

    //  Mapping
    private Client mapRow(ResultSet rs) throws SQLException {
        return new Client(
                rs.getInt("id_client"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("telephone"),
                rs.getString("email")
        );
    }
}
