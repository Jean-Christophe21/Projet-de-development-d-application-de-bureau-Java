/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.dao;

import tg.univ.lome.epl.model.Lot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author USER
 */

public class LotDAO implements IDao<Lot, Integer> {

    private final Connection conn;

    public LotDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public int insert(Lot l) throws SQLException {
        String sql
                = "INSERT INTO Lots("
                + "numero_lot, quantite_initiale, quantite_restante, "
                + "prix_unitaire, date_peremption, id_medicament, id_fournisseur) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, l.getNumeroLot());
            ps.setInt(2, l.getQuantiteInitiale());
            ps.setInt(3, l.getQuantiteRestante());
            ps.setDouble(4, l.getPrixUnitaire());
            ps.setString(5, l.getDatePeremption());
            ps.setInt(6, l.getIdMedicament());

            if (l.getIdFournisseur() != null) {
                ps.setInt(7, l.getIdFournisseur());
            } else {
                ps.setNull(7, Types.INTEGER);
            }

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    @Override
    public boolean update(Lot l) throws SQLException {
        String sql
                = "UPDATE Lots SET "
                + "numero_lot = ?, "
                + "quantite_initiale = ?, "
                + "quantite_restante = ?, "
                + "prix_unitaire = ?, "
                + "date_peremption = ?, "
                + "id_medicament = ?, "
                + "id_fournisseur = ? "
                + "WHERE id_lot = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, l.getNumeroLot());
            ps.setInt(2, l.getQuantiteInitiale());
            ps.setInt(3, l.getQuantiteRestante());
            ps.setDouble(4, l.getPrixUnitaire());
            ps.setString(5, l.getDatePeremption());
            ps.setInt(6, l.getIdMedicament());

            if (l.getIdFournisseur() != null) {
                ps.setInt(7, l.getIdFournisseur());
            } else {
                ps.setNull(7, Types.INTEGER);
            }

            ps.setInt(8, l.getIdLot());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM Lots WHERE id_lot = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<Lot> findById(Integer id) throws SQLException {
        String sql
                = "SELECT l.*, m.nom_commercial AS nom_medicament, "
                + "f.nom AS nom_fournisseur "
                + "FROM Lots l "
                + "JOIN Medicaments m ON m.id_medicament = l.id_medicament "
                + "LEFT JOIN Fournisseurs f ON f.id_fournisseur = l.id_fournisseur "
                + "WHERE l.id_lot = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
        }
    }

    @Override
    public List<Lot> findAll() throws SQLException {
        List<Lot> list = new ArrayList<>();

        String sql
                = "SELECT l.*, m.nom_commercial AS nom_medicament, "
                + "f.nom AS nom_fournisseur "
                + "FROM Lots l "
                + "JOIN Medicaments m ON m.id_medicament = l.id_medicament "
                + "LEFT JOIN Fournisseurs f ON f.id_fournisseur = l.id_fournisseur "
                + "ORDER BY l.date_peremption";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    public List<Lot> findByMedicamentFIFO(int idMedicament) throws SQLException {
        List<Lot> list = new ArrayList<>();

        String sql
                = "SELECT l.*, m.nom_commercial AS nom_medicament, "
                + "f.nom AS nom_fournisseur "
                + "FROM Lots l "
                + "JOIN Medicaments m ON m.id_medicament = l.id_medicament "
                + "LEFT JOIN Fournisseurs f ON f.id_fournisseur = l.id_fournisseur "
                + "WHERE l.id_medicament = ? "
                + "AND l.quantite_restante > 0 "
                + "AND l.date_peremption >= date('now') "
                + "ORDER BY l.date_peremption ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedicament);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    public List<Lot> findLotsExpirantDans(int jours) throws SQLException {
        List<Lot> list = new ArrayList<>();

        String sql
                = "SELECT l.*, m.nom_commercial AS nom_medicament, "
                + "f.nom AS nom_fournisseur "
                + "FROM Lots l "
                + "JOIN Medicaments m ON m.id_medicament = l.id_medicament "
                + "LEFT JOIN Fournisseurs f ON f.id_fournisseur = l.id_fournisseur "
                + "WHERE l.date_peremption BETWEEN date('now') "
                + "AND date('now', '+' || ? || ' days') "
                + "AND l.quantite_restante > 0 "
                + "ORDER BY l.date_peremption ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jours);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    public List<Lot> findLotsExpires() throws SQLException {
        List<Lot> list = new ArrayList<>();

        String sql
                = "SELECT l.*, m.nom_commercial AS nom_medicament, "
                + "f.nom AS nom_fournisseur "
                + "FROM Lots l "
                + "JOIN Medicaments m ON m.id_medicament = l.id_medicament "
                + "LEFT JOIN Fournisseurs f ON f.id_fournisseur = l.id_fournisseur "
                + "WHERE l.date_peremption < date('now') "
                + "AND l.quantite_restante > 0 "
                + "ORDER BY l.date_peremption ASC";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    public boolean decrementerStock(int idLot, int quantite) throws SQLException {
        String sql
                = "UPDATE Lots "
                + "SET quantite_restante = quantite_restante - ? "
                + "WHERE id_lot = ? "
                + "AND quantite_restante >= ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantite);
            ps.setInt(2, idLot);
            ps.setInt(3, quantite);

            return ps.executeUpdate() > 0;
        }
    }

    private Lot mapRow(ResultSet rs) throws SQLException {
        Lot l = new Lot();
        l.setIdLot(rs.getInt("id_lot"));
        l.setNumeroLot(rs.getString("numero_lot"));
        l.setQuantiteInitiale(rs.getInt("quantite_initiale"));
        l.setQuantiteRestante(rs.getInt("quantite_restante"));
        l.setPrixUnitaire(rs.getDouble("prix_unitaire"));
        l.setDatePeremption(rs.getString("date_peremption"));
        l.setIdMedicament(rs.getInt("id_medicament"));

        int idF = rs.getInt("id_fournisseur");
        l.setIdFournisseur(rs.wasNull() ? null : idF);

        l.setNomMedicament(rs.getString("nom_medicament"));
        l.setNomFournisseur(rs.getString("nom_fournisseur"));

        return l;
    }

    public List<Lot> searchActiveLots(String keyword) throws SQLException {
        List<Lot> list = new ArrayList<>();
        String sql = "SELECT l.*, m.nom_commercial AS nom_medicament, "
                + "f.nom AS nom_fournisseur "
                + "FROM Lots l "
                + "JOIN Medicaments m ON m.id_medicament = l.id_medicament "
                + "LEFT JOIN Fournisseurs f ON f.id_fournisseur = l.id_fournisseur "
                + "WHERE (m.nom_commercial LIKE ? OR m.dci LIKE ?) "
                + "AND l.quantite_restante > 0 "
                + "AND l.date_peremption >= date('now') "
                + "ORDER BY l.date_peremption ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }
}
