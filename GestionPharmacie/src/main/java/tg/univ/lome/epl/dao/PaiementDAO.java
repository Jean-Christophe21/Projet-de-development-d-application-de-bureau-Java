/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.dao;

import tg.univ.lome.epl.model.Paiement;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author USER
 */



public class PaiementDAO implements IDao<Paiement, Integer> {

    private final Connection conn;

    public PaiementDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public int insert(Paiement p) throws SQLException {
        String sql =
                "INSERT INTO Paiements(mode_paiement, montant, date_paiement, id_vente) " +
                "VALUES (?, ?, datetime('now','localtime'), ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getModePaiement());
            ps.setDouble(2, p.getMontant());
            ps.setInt(3, p.getIdVente());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    @Override
    public boolean update(Paiement p) throws SQLException {
        String sql =
                "UPDATE Paiements SET mode_paiement = ?, montant = ? " +
                "WHERE id_paiement = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getModePaiement());
            ps.setDouble(2, p.getMontant());
            ps.setInt(3, p.getIdPaiement());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM Paiements WHERE id_paiement = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<Paiement> findById(Integer id) throws SQLException {
        String sql =
                "SELECT p.*, v.code_vente " +
                "FROM Paiements p " +
                "JOIN Ventes v ON v.id_vente = p.id_vente " +
                "WHERE p.id_paiement = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
        }
    }

    @Override
    public List<Paiement> findAll() throws SQLException {
        List<Paiement> list = new ArrayList<>();

        String sql =
                "SELECT p.*, v.code_vente " +
                "FROM Paiements p " +
                "JOIN Ventes v ON v.id_vente = p.id_vente " +
                "ORDER BY p.date_paiement DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));
        }

        return list;
    }

    // ── Métier

    public List<Paiement> findByPeriode(String dateDebut, String dateFin)
            throws SQLException {

        List<Paiement> list = new ArrayList<>();

        String sql =
                "SELECT p.*, v.code_vente " +
                "FROM Paiements p " +
                "JOIN Ventes v ON v.id_vente = p.id_vente " +
                "WHERE p.date_paiement BETWEEN ? AND ? " +
                "ORDER BY p.date_paiement DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dateDebut + " 00:00:00");
            ps.setString(2, dateFin + " 23:59:59");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(mapRow(rs));
        }

        return list;
    }

    public List<Object[]> getTotalParMode(String dateDebut, String dateFin)
            throws SQLException {

        List<Object[]> list = new ArrayList<>();

        String sql =
                "SELECT mode_paiement, COUNT(*) AS nb_transactions, SUM(montant) AS total " +
                "FROM Paiements " +
                "WHERE date_paiement BETWEEN ? AND ? " +
                "GROUP BY mode_paiement " +
                "ORDER BY total DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dateDebut + " 00:00:00");
            ps.setString(2, dateFin + " 23:59:59");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("mode_paiement"),
                        rs.getInt("nb_transactions"),
                        rs.getDouble("total")
                });
            }
        }

        return list;
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Paiements";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Paiement mapRow(ResultSet rs) throws SQLException {
        Paiement p = new Paiement();

        p.setIdPaiement(rs.getInt("id_paiement"));
        p.setModePaiement(rs.getString("mode_paiement"));
        p.setMontant(rs.getDouble("montant"));
        p.setDatePaiement(rs.getString("date_paiement"));
        p.setIdVente(rs.getInt("id_vente"));

        try {
            p.setCodeVente(rs.getString("code_vente"));
        } catch (SQLException ignored) {}

        return p;
    }
}
