/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author USER
 */

public class DashboardDAO {

    private final Connection conn;

    public DashboardDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public Map<String, Object> getDashboardMetrics() throws SQLException {

        Map<String, Object> metrics = new HashMap<>();

        // ── INVENTAIRE ───────────────────────────────
        String sqlInventaire
                = "SELECT "
                + "(SELECT COUNT(DISTINCT id_medicament) FROM Lots "
                + " WHERE quantite_restante > 0 "
                + " AND date_peremption >= date('now')) AS medicaments_disponibles, "
                + "(SELECT COUNT(*) FROM Medicaments m "
                + " WHERE (SELECT COALESCE(SUM(l.quantite_restante),0) "
                + " FROM Lots l WHERE l.id_medicament = m.id_medicament) <= m.seuil_alerte) "
                + " AS medicaments_en_rupture, "
                + "(SELECT COUNT(*) FROM Categories) AS nb_groupes, "
                + "(SELECT COUNT(*) FROM Medicaments) AS nb_medicaments_total, "
                + "(SELECT COUNT(*) FROM Fournisseurs) AS nb_fournisseurs, "
                + "(SELECT COUNT(*) FROM Utilisateurs) AS nb_utilisateurs, "
                + "(SELECT COUNT(*) FROM Clients) AS nb_clients";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlInventaire)) {

            if (rs.next()) {
                metrics.put("medicaments_disponibles", rs.getInt("medicaments_disponibles"));
                metrics.put("medicaments_en_rupture", rs.getInt("medicaments_en_rupture"));
                metrics.put("nb_groupes", rs.getInt("nb_groupes"));
                metrics.put("nb_medicaments_total", rs.getInt("nb_medicaments_total"));
                metrics.put("nb_fournisseurs", rs.getInt("nb_fournisseurs"));
                metrics.put("nb_utilisateurs", rs.getInt("nb_utilisateurs"));
                metrics.put("nb_clients", rs.getInt("nb_clients"));
            }
        }

        // ── VENTES DU MOIS ───────────────────────────
        String sqlVentes
                = "SELECT "
                + "COALESCE(SUM(v.montant_total),0) AS revenu_mois, "
                + "COUNT(v.id_vente) AS nb_factures, "
                + "COALESCE(SUM(lv.quantite),0) AS qty_vendues "
                + "FROM Ventes v "
                + "LEFT JOIN LignesVentes lv ON lv.id_vente = v.id_vente "
                + "WHERE strftime('%Y-%m', v.date_vente) = strftime('%Y-%m','now')";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlVentes)) {

            if (rs.next()) {
                metrics.put("revenu_mois_courant", rs.getDouble("revenu_mois"));
                metrics.put("nb_factures_mois", rs.getInt("nb_factures"));
                metrics.put("qty_vendues_mois", rs.getInt("qty_vendues"));
            }
        }

        // ── TOP PRODUIT ─────────────────────────────
        String sqlTop
                = "SELECT m.nom_commercial, SUM(lv.quantite) AS total "
                + "FROM LignesVentes lv "
                + "JOIN Lots l ON l.id_lot = lv.id_lot "
                + "JOIN Medicaments m ON m.id_medicament = l.id_medicament "
                + "GROUP BY m.id_medicament "
                + "ORDER BY total DESC LIMIT 1";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlTop)) {

            metrics.put("medicament_top_vente",
                    rs.next() ? rs.getString("nom_commercial") : "—");
        }

        // ── STATUT INVENTAIRE ───────────────────────
        int enRupture = (int) metrics.getOrDefault("medicaments_en_rupture", 0);
        int expirant = countLotsExpirantDans(90);

        String statut;
        if (enRupture == 0 && expirant == 0) {
            statut = "Good";
        } else if (enRupture <= 2 || expirant <= 3) {
            statut = "Attention";
        } else {
            statut = "Critique";
        }

        metrics.put("statut_inventaire", statut);
        metrics.put("nb_lots_expirant_bientot", expirant);

        return metrics;
    }

    public List<Object[]> getRevenuParMois(int nbMois) throws SQLException {

        List<Object[]> list = new ArrayList<>();

        String sql
                = "SELECT strftime('%Y-%m', date_vente) AS mois, "
                + "SUM(montant_total) AS revenu "
                + "FROM Ventes "
                + "WHERE date_vente >= date('now', '-' || ? || ' months') "
                + "GROUP BY mois ORDER BY mois";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, nbMois);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("mois"),
                    rs.getDouble("revenu")
                });
            }
        }

        return list;
    }

    private int countLotsExpirantDans(int jours) throws SQLException {

        String sql
                = "SELECT COUNT(*) FROM Lots "
                + "WHERE date_peremption BETWEEN date('now') "
                + "AND date('now','+' || ? || ' days') "
                + "AND quantite_restante > 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, jours);

            ResultSet rs = ps.executeQuery();

            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
