/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.dao;

import tg.univ.lome.epl.model.Categorie;
import tg.univ.lome.epl.model.Medicament;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author USER
 */

public class MedicamentDAO implements IDao<Medicament, Integer> {

    private final Connection conn;
    private final CategorieDAO categorieDAO;

    public MedicamentDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
        this.categorieDAO = new CategorieDAO();
    }

    // ── CRUD de base ─────────────────────────────────────────────────────────
    @Override
    public int insert(Medicament m) throws SQLException {
        String sql = "INSERT INTO Medicaments("
                + "     nom_commercial, dci, description,"
                + "     comment_utiliser, effets_secondaires,"
                + "     est_generique, seuil_alerte, id_medicament_reference)"
                + "   VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getNomCommercial());
            ps.setString(2, m.getDci());
            ps.setString(3, m.getDescription());
            ps.setString(4, m.getCommentUtiliser());
            ps.setString(5, m.getEffetsSecondaires());
            ps.setInt(6, m.isEstGenerique() ? 1 : 0);
            ps.setInt(7, m.getSeuilAlerte());
            if (m.getIdMedicamentReference() != null) {
                ps.setInt(8, m.getIdMedicamentReference());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            int newId = rs.next() ? rs.getInt(1) : -1;
            m.setIdMedicament(newId);
            return newId;
        }
    }

    @Override
    public boolean update(Medicament m) throws SQLException {
        String sql = " UPDATE Medicaments"
                + "      SET nom_commercial = ?, dci = ?, description = ?,"
                + "        comment_utiliser = ?, effets_secondaires = ?,"
                + "        est_generique = ?, seuil_alerte = ?,"
                + "        id_medicament_reference = ?"
                + "      WHERE id_medicament = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getNomCommercial());
            ps.setString(2, m.getDci());
            ps.setString(3, m.getDescription());
            ps.setString(4, m.getCommentUtiliser());
            ps.setString(5, m.getEffetsSecondaires());
            ps.setInt(6, m.isEstGenerique() ? 1 : 0);
            ps.setInt(7, m.getSeuilAlerte());
            if (m.getIdMedicamentReference() != null) {
                ps.setInt(8, m.getIdMedicamentReference());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.setInt(9, m.getIdMedicament());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM Medicaments WHERE id_medicament = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * findById enrichi : charge aussi le stock calculé et les catégories. Image
     * 5 — fiche médicament avec Lifetime Supply / Lifetime Sales / Stock Left.
     */
    @Override
    public Optional<Medicament> findById(Integer id) throws SQLException {
        String sql = ""
                + "SELECT m.*,"
                + "  COALESCE(SUM(l.quantite_initiale), 0) AS lifetime_supply,"
                + "  COALESCE(SUM(l.quantite_restante), 0) AS stock_total"
                + " FROM Medicaments m"
                + " LEFT JOIN Lots l ON l.id_medicament = m.id_medicament"
                + " WHERE m.id_medicament = ?"
                + " GROUP BY m.id_medicament";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return Optional.empty();
            }
            Medicament med = mapRow(rs);
            med.setCategories(categorieDAO.findByMedicament(id));
            return Optional.of(med);
        }
    }

    /**
     * findAll avec stock en temps réel. Image 4 — liste des médicaments avec
     * colonne "Stock in Qty".
     */
    @Override
    public List<Medicament> findAll() throws SQLException {
        List<Medicament> list = new ArrayList<>();
        String sql = ""
                + "SELECT m.*,"
                + " COALESCE(SUM(l.quantite_restante), 0) AS stock_total,"
                + " COALESCE(SUM(l.quantite_initiale), 0) AS lifetime_supply"
                + " FROM Medicaments m"
                + " LEFT JOIN Lots l ON l.id_medicament = m.id_medicament"
                + "GROUP BY m.id_medicament"
                + "ORDER BY m.nom_commercial";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── Requêtes métier ──────────────────────────────────────────────────────
    /**
     * Recherche fulltext sur nom commercial, DCI ou description. Barre de
     * recherche image 4.
     */
    public List<Medicament> search(String motCle) throws SQLException {
        List<Medicament> list = new ArrayList<>();
        String sql = ""
                + "SELECT m.*,"
                + "  COALESCE(SUM(l.quantite_restante), 0) AS stock_total,"
                + "  COALESCE(SUM(l.quantite_initiale), 0) AS lifetime_supply"
                + " FROM Medicaments m"
                + " LEFT JOIN Lots l ON l.id_medicament = m.id_medicament"
                + "WHERE LOWER(m.nom_commercial) LIKE LOWER(?)"
                + "OR LOWER(m.dci)            LIKE LOWER(?)"
                + "OR LOWER(m.description)    LIKE LOWER(?)"
                + "GROUP BY m.id_medicament"
                + "ORDER BY m.nom_commercial";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String p = "%" + motCle + "%";
            ps.setString(1, p);
            ps.setString(2, p);
            ps.setString(3, p);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Filtre par catégorie — Select Group image 4.
     */
    public List<Medicament> findByCategorie(int idCategorie) throws SQLException {
        List<Medicament> list = new ArrayList<>();
        String sql = ""
                + "SELECT m.*,"
                + "    COALESCE(SUM(l.quantite_restante), 0) AS stock_total,"
                + "    COALESCE(SUM(l.quantite_initiale), 0) AS lifetime_supply"
                + " FROM Medicaments m"
                + " JOIN MedicamentCategories mc ON mc.id_medicament = m.id_medicament"
                + " LEFT JOIN Lots l ON l.id_medicament = m.id_medicament"
                + " WHERE mc.id_categorie = ?"
                + " GROUP BY m.id_medicament"
                + " ORDER BY m.nom_commercial";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCategorie);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Médicaments en rupture (stock_total = 0 ou ≤ seuil_alerte). Dashboard
     * "Medicine Shortage" + badge rouge.
     */
    public List<Medicament> findEnRupture() throws SQLException {
        List<Medicament> list = new ArrayList<>();
        String sql = ""
                + "SELECT m.*,"
                + "   COALESCE(SUM(l.quantite_restante), 0) AS stock_total,"
                + "   COALESCE(SUM(l.quantite_initiale), 0) AS lifetime_supply"
                + " FROM Medicaments m"
                + " LEFT JOIN Lots l ON l.id_medicament = m.id_medicament"
                + " GROUP BY m.id_medicament"
                + " HAVING stock_total <= m.seuil_alerte"
                + " ORDER BY stock_total ASC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Substituts génériques d'un médicament (même DCI ou même catégorie).
     * Module d'équivalences image 1 spec.
     */
    public List<Medicament> findSubstituts(int idMedicament) throws SQLException {
        List<Medicament> list = new ArrayList<>();
        String sql = ""
                + "SELECT m.*,"
                + "  COALESCE(SUM(l.quantite_restante), 0) AS stock_total,"
                + "  COALESCE(SUM(l.quantite_initiale), 0) AS lifetime_supply"
                + " FROM Medicaments m"
                + " LEFT JOIN Lots l ON l.id_medicament = m.id_medicament"
                + " WHERE m.id_medicament != ?"
                + " AND ("
                + "   m.dci = (SELECT dci FROM Medicaments WHERE id_medicament = ?)"
                + " OR m.id_medicament_reference = ?"
                + " OR ? IN ("
                + "    SELECT id_medicament_reference"
                + "    FROM Medicaments"
                + "      WHERE id_medicament = m.id_medicament"
                + "         )"
                + ")"
                + "  GROUP BY m.id_medicament"
                + "  ORDER BY m.est_generique DESC, m.nom_commercial";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedicament);
            ps.setInt(2, idMedicament);
            ps.setInt(3, idMedicament);
            ps.setInt(4, idMedicament);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Nombre total de médicaments disponibles — Dashboard "Medicines
     * Available". Compte uniquement les médicaments ayant au moins un lot avec
     * stock > 0.
     */
    public int countDisponibles() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT id_medicament) FROM Lots WHERE quantite_restante > 0";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Nombre total de médicaments — Dashboard "Total no of Medicines".
     */
    public int countAll() throws SQLException {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Medicaments")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Top N médicaments les plus vendus — Dashboard Statistiques. Résultat :
     * [nom_commercial, total_vendu]
     */
    public List<Object[]> findTopVendus(int limit) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = ""
                + "SELECT m.nom_commercial, SUM(lv.quantite) AS total_vendu"
                + "  FROM LignesVentes lv"
                + "  JOIN Lots l        ON l.id_lot         = lv.id_lot"
                + "  JOIN Medicaments m ON m.id_medicament   = l.id_medicament"
                + " GROUP BY m.id_medicament"
                + " ORDER BY total_vendu DESC"
                + " LIMIT ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("nom_commercial"),
                    rs.getInt("total_vendu")
                });
            }
        }
        return list;
    }

    // ── Mapping 
    private Medicament mapRow(ResultSet rs) throws SQLException {
        Medicament m = new Medicament();
        m.setIdMedicament(rs.getInt("id_medicament"));
        m.setNomCommercial(rs.getString("nom_commercial"));
        m.setDci(rs.getString("dci"));
        m.setDescription(rs.getString("description"));
        m.setCommentUtiliser(rs.getString("comment_utiliser"));
        m.setEffetsSecondaires(rs.getString("effets_secondaires"));
        m.setEstGenerique(rs.getInt("est_generique") == 1);
        m.setSeuilAlerte(rs.getInt("seuil_alerte"));
        int ref = rs.getInt("id_medicament_reference");
        m.setIdMedicamentReference(rs.wasNull() ? null : ref);

        // Colonnes calculées (présentes seulement dans les requêtes enrichies)
        try {
            int stockTotal = rs.getInt("stock_total");
            int ls = rs.getInt("lifetime_supply");
            m.setStockTotal(stockTotal);
            m.setLifetimeSupply(ls);
            m.setLifetimeSales(ls - stockTotal);
        } catch (SQLException ignored) {
            // Pas de colonne calculée dans cette requête, on laisse les valeurs à 0
        }
        return m;
    }
}
