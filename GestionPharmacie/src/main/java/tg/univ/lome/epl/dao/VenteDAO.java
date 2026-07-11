/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.dao;

import tg.univ.lome.epl.model.LigneVente;
import tg.univ.lome.epl.model.Vente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author USER
 */

public class VenteDAO implements IDao<Vente, Integer> {

    private final Connection conn;
    private final LotDAO lotDAO;

    public VenteDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
        this.lotDAO = new LotDAO();
    }

    @Override
    public int insert(Vente vente) throws SQLException {
        conn.setAutoCommit(false);

        try {
            String sqlVente
                    = "INSERT INTO Ventes(date_vente, montant_total, montant_recu, "
                    + "id_utilisateur, id_client) "
                    + "VALUES (datetime('now','localtime'), 0, ?, ?, ?)";

            int idVente;

            try (PreparedStatement ps = conn.prepareStatement(
                    sqlVente, Statement.RETURN_GENERATED_KEYS)) {

                if (vente.getMontantRecu() != null) {
                    ps.setDouble(1, vente.getMontantRecu());
                } else {
                    ps.setNull(1, Types.REAL);
                }

                if (vente.getIdUtilisateur() != null) {
                    ps.setInt(2, vente.getIdUtilisateur());
                } else {
                    ps.setNull(2, Types.INTEGER);
                }

                if (vente.getIdClient() != null) {
                    ps.setInt(3, vente.getIdClient());
                } else {
                    ps.setNull(3, Types.INTEGER);
                }

                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                idVente = rs.next() ? rs.getInt(1) : -1;
            }

            if (idVente == -1) {
                throw new SQLException("Échec création vente");
            }

            double total = 0.0;

            String sqlLigne
                    = "INSERT INTO LignesVentes(quantite, prix_unitaire_applique, id_vente, id_lot) "
                    + "VALUES (?, ?, ?, ?)";

            for (LigneVente ligne : vente.getLignes()) {

                boolean ok = lotDAO.decrementerStock(
                        ligne.getIdLot(),
                        ligne.getQuantite()
                );

                if (!ok) {
                    conn.rollback();
                    throw new SQLException("Stock insuffisant lot id=" + ligne.getIdLot());
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlLigne)) {
                    ps.setInt(1, ligne.getQuantite());
                    ps.setDouble(2, ligne.getPrixUnitaireApplique());
                    ps.setInt(3, idVente);
                    ps.setInt(4, ligne.getIdLot());
                    ps.executeUpdate();
                }

                total += ligne.getSousTotal();
            }

            try (PreparedStatement ps
                    = conn.prepareStatement("UPDATE Ventes SET montant_total = ? WHERE id_vente = ?")) {
                ps.setDouble(1, total);
                ps.setInt(2, idVente);
                ps.executeUpdate();
            }

            conn.commit();

            vente.setIdVente(idVente);
            vente.setMontantTotal(total);

            return idVente;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    @Override
    public boolean update(Vente v) throws SQLException {
        String sql
                = "UPDATE Ventes SET montant_recu = ?, id_client = ? "
                + "WHERE id_vente = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            if (v.getMontantRecu() != null) {
                ps.setDouble(1, v.getMontantRecu());
            } else {
                ps.setNull(1, Types.REAL);
            }

            if (v.getIdClient() != null) {
                ps.setInt(2, v.getIdClient());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setInt(3, v.getIdVente());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        try (PreparedStatement ps
                = conn.prepareStatement("DELETE FROM Ventes WHERE id_vente = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<Vente> findById(Integer id) throws SQLException {
        String sql
                = "SELECT v.*, "
                + "u.nom || ' ' || u.prenom AS nom_utilisateur, "
                + "c.nom || ' ' || COALESCE(c.prenom,'') AS nom_client "
                + "FROM Ventes v "
                + "LEFT JOIN Utilisateurs u ON u.id_utilisateur = v.id_utilisateur "
                + "LEFT JOIN Clients c ON c.id_client = v.id_client "
                + "WHERE v.id_vente = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            Vente vente = mapRow(rs);
            vente.setLignes(findLignesByVente(id));

            return Optional.of(vente);
        }
    }

    @Override
    public List<Vente> findAll() throws SQLException {
        List<Vente> list = new ArrayList<>();

        String sql
                = "SELECT v.*, "
                + "u.nom || ' ' || u.prenom AS nom_utilisateur, "
                + "c.nom || ' ' || COALESCE(c.prenom,'') AS nom_client "
                + "FROM Ventes v "
                + "LEFT JOIN Utilisateurs u ON u.id_utilisateur = v.id_utilisateur "
                + "LEFT JOIN Clients c ON c.id_client = v.id_client "
                + "ORDER BY v.date_vente DESC";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    
    public List<LigneVente> findLignesByVente(int idVente) throws SQLException {
        List<LigneVente> list = new ArrayList<>();

        String sql
                = "SELECT lv.*, m.nom_commercial AS nom_medicament, l.numero_lot "
                + "FROM LignesVentes lv "
                + "JOIN Lots l ON l.id_lot = lv.id_lot "
                + "JOIN Medicaments m ON m.id_medicament = l.id_medicament "
                + "WHERE lv.id_vente = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVente);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LigneVente lv = new LigneVente();
                lv.setIdLigne(rs.getInt("id_ligne"));
                lv.setQuantite(rs.getInt("quantite"));
                lv.setPrixUnitaireApplique(rs.getDouble("prix_unitaire_applique"));
                lv.setIdVente(rs.getInt("id_vente"));
                lv.setIdLot(rs.getInt("id_lot"));
                lv.setNomMedicament(rs.getString("nom_medicament"));
                lv.setNumeroLot(rs.getString("numero_lot"));
                list.add(lv);
            }
        }

        return list;
    }

    private Vente mapRow(ResultSet rs) throws SQLException {
        Vente v = new Vente();
        v.setIdVente(rs.getInt("id_vente"));
        v.setCodeVente(rs.getString("code_vente"));
        v.setDateVente(rs.getString("date_vente"));
        v.setMontantTotal(rs.getDouble("montant_total"));

        double mr = rs.getDouble("montant_recu");
        v.setMontantRecu(rs.wasNull() ? null : mr);

        double mr2 = rs.getDouble("monnaie_rendue");
        v.setMonnaieRendue(rs.wasNull() ? null : mr2);

        int idU = rs.getInt("id_utilisateur");
        v.setIdUtilisateur(rs.wasNull() ? null : idU);

        int idC = rs.getInt("id_client");
        v.setIdClient(rs.wasNull() ? null : idC);

        try {
            v.setNomUtilisateur(rs.getString("nom_utilisateur"));
        } catch (Exception ignored) {
        }
        try {
            v.setNomClient(rs.getString("nom_client"));
        } catch (Exception ignored) {
        }

        return v;
    }
}
