/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.dao;

import tg.univ.lome.epl.model.DemandeStock;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author USER
 */

public class DemandeStockDAO implements IDao<DemandeStock, Integer> {

    private final Connection conn;

    public DemandeStockDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    private static final String SQL_SELECT
            = "SELECT ds.*, "
            + "m.nom_commercial AS nom_medicament, "
            + "f.nom AS nom_fournisseur, "
            + "u.nom || ' ' || u.prenom AS nom_utilisateur "
            + "FROM DemandesStock ds "
            + "JOIN Medicaments m ON m.id_medicament = ds.id_medicament "
            + "LEFT JOIN Fournisseurs f ON f.id_fournisseur = ds.id_fournisseur "
            + "LEFT JOIN Utilisateurs u ON u.id_utilisateur = ds.id_utilisateur";

    @Override
    public int insert(DemandeStock d) throws SQLException {
        String sql
                = "INSERT INTO DemandesStock("
                + "statut, quantite_demandee, notes, date_demande, "
                + "id_medicament, id_fournisseur, id_utilisateur) "
                + "VALUES ('EN_ATTENTE', ?, ?, datetime('now','localtime'), ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, d.getQuantiteDemandee());
            ps.setString(2, d.getNotes());
            ps.setInt(3, d.getIdMedicament());

            if (d.getIdFournisseur() != null) {
                ps.setInt(4, d.getIdFournisseur());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            if (d.getIdUtilisateur() != null) {
                ps.setInt(5, d.getIdUtilisateur());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    @Override
    public boolean update(DemandeStock d) throws SQLException {
        String sql
                = "UPDATE DemandesStock "
                + "SET statut = ?, quantite_demandee = ?, notes = ?, id_fournisseur = ? "
                + "WHERE id_demande = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, d.getStatut());
            ps.setInt(2, d.getQuantiteDemandee());
            ps.setString(3, d.getNotes());

            if (d.getIdFournisseur() != null) {
                ps.setInt(4, d.getIdFournisseur());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.setInt(5, d.getIdDemande());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM DemandesStock WHERE id_demande = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<DemandeStock> findById(Integer id) throws SQLException {
        String sql = SQL_SELECT + " WHERE ds.id_demande = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
        }
    }

    @Override
    public List<DemandeStock> findAll() throws SQLException {
        List<DemandeStock> list = new ArrayList<>();

        String sql = SQL_SELECT + " ORDER BY ds.date_demande DESC";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    // ── Métier 
    public List<DemandeStock> findEnAttente() throws SQLException {
        List<DemandeStock> list = new ArrayList<>();

        String sql = SQL_SELECT
                + " WHERE ds.statut = 'EN_ATTENTE' "
                + "ORDER BY ds.date_demande";

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    public List<DemandeStock> findByMedicament(int idMedicament) throws SQLException {
        List<DemandeStock> list = new ArrayList<>();

        String sql = SQL_SELECT
                + " WHERE ds.id_medicament = ? "
                + "ORDER BY ds.date_demande DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedicament);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    public boolean changerStatut(int idDemande, String nouveauStatut) throws SQLException {
        String sql
                = "UPDATE DemandesStock "
                + "SET statut = ?, date_traitement = datetime('now','localtime') "
                + "WHERE id_demande = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nouveauStatut);
            ps.setInt(2, idDemande);
            return ps.executeUpdate() > 0;
        }
    }

    public int countEnAttente() throws SQLException {
        String sql = "SELECT COUNT(*) FROM DemandesStock WHERE statut = 'EN_ATTENTE'";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ── Mapping ─────────────────────────────────────────────
    private DemandeStock mapRow(ResultSet rs) throws SQLException {
        DemandeStock d = new DemandeStock();

        d.setIdDemande(rs.getInt("id_demande"));
        d.setStatut(rs.getString("statut"));
        d.setQuantiteDemandee(rs.getInt("quantite_demandee"));
        d.setNotes(rs.getString("notes"));
        d.setDateDemande(rs.getString("date_demande"));
        d.setDateTraitement(rs.getString("date_traitement"));
        d.setIdMedicament(rs.getInt("id_medicament"));

        int idF = rs.getInt("id_fournisseur");
        d.setIdFournisseur(rs.wasNull() ? null : idF);

        int idU = rs.getInt("id_utilisateur");
        d.setIdUtilisateur(rs.wasNull() ? null : idU);

        d.setNomMedicament(rs.getString("nom_medicament"));

        try {
            d.setNomFournisseur(rs.getString("nom_fournisseur"));
        } catch (Exception ignored) {
        }
        try {
            d.setNomUtilisateur(rs.getString("nom_utilisateur"));
        } catch (Exception ignored) {
        }

        return d;
    }
}
