package tg.univ.lome.epl.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Map;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.AnchorPane;
import tg.univ.lome.epl.dao.DashboardDAO;
import tg.univ.lome.epl.dao.VenteDAO;
import tg.univ.lome.epl.model.Utilisateur;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import java.util.List;
import java.util.ArrayList;
import tg.univ.lome.epl.util.SessionManager;

/**
 * FXML Controller class for Dashboard
 */
public class DashboardController implements Initializable {

    @FXML
    private Label lblUtilisateurConnecte;

    @FXML
    private Label lblVentesDuJour;

    @FXML
    private Label lblAlertesActives;

    @FXML
    private Label lblProduitsRupture;

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnVente;

    @FXML
    private Button btnInventaire;

    @FXML
    private Button btnCatalogue;

    @FXML
    private Button btnLots;

    @FXML
    private Button btnAlertes;

    @FXML
    private Button btnRapports;

    @FXML
    private Button btnDeconnexion;

    @FXML
    private Button btnActualiserDashboard;
    @FXML
    private Button btnSupprimerVente;

    @FXML
    private AnchorPane paneContenu;

    @FXML
    private TableView<Activite> tableActiviteRecente;
    @FXML
    private TableColumn<Activite, String> colActDate;
    @FXML
    private TableColumn<Activite, String> colActMedicament;
    @FXML
    private TableColumn<Activite, Integer> colActQte;
    @FXML
    private TableColumn<Activite, Double> colActMontant;
    @FXML
    private TableColumn<Activite, String> colActVendeur;

    private DashboardDAO dashboardDAO;
    private java.util.List<Node> vueDefaut;

    private static DashboardController instance;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this; // hook statique

        // nregistrer la vue par défaut du dashboard afin de l'utiliser pls tard
        if (!paneContenu.getChildren().isEmpty()) {
            vueDefaut = new java.util.ArrayList<>(paneContenu.getChildren());
        }

        // Afficher l'utilisateur connecté
        Utilisateur user = SessionManager.getUtilisateurConnecte();
        if (user != null) {
            lblUtilisateurConnecte.setText("Connecté(e) en tant que : " + user.getPrenom() + " " + user.getNom());
        } else {
            lblUtilisateurConnecte.setText("Connecté(e) en tant que : Invité");
        }

        try {
            this.dashboardDAO = new DashboardDAO();
            chargerMetriques();
            chargerActivitesRecentes();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Associer les écouteurs d'événements pour la navigation
        btnDashboard.setOnAction(e -> afficherDashboardHome());
        btnVente.setOnAction(e -> chargerVue("/fxml/Ventes.fxml", btnVente));
        btnInventaire.setOnAction(e -> chargerVue("/fxml/Inventaire.fxml", btnInventaire));
        btnCatalogue.setOnAction(e -> chargerVue("/fxml/Catalogue.fxml", btnCatalogue));
        btnLots.setOnAction(e -> chargerVue("/fxml/Lots.fxml", btnLots));
        btnAlertes.setOnAction(e -> chargerVue("/fxml/Alertes.fxml", btnAlertes));
        btnRapports.setOnAction(e -> chargerVue("/fxml/Rapports.fxml", btnRapports));
        btnDeconnexion.setOnAction(e -> handleDeconnexion());

        if (btnActualiserDashboard != null)
            btnActualiserDashboard.setOnAction(e -> {
                chargerMetriques();
                chargerActivitesRecentes();
            });
        if (btnSupprimerVente != null)
            btnSupprimerVente.setOnAction(e -> handleSupprimerVentes());
    }

    private void chargerMetriques() {
        try {
            Map<String, Object> metrics = dashboardDAO.getDashboardMetrics();

            // Ventes du jour
            double ventesJour = (double) metrics.getOrDefault("ventes_du_jour", 0.0);
            lblVentesDuJour.setText(String.format("%,.0f", ventesJour));

            // Produits en rupture
            int enRupture = (int) metrics.getOrDefault("medicaments_en_rupture", 0);
            lblProduitsRupture.setText(String.valueOf(enRupture));

            // Alertes actives
            int expirant = (int) metrics.getOrDefault("nb_lots_expirant_bientot", 0);
            int alertesActives = enRupture + expirant;
            lblAlertesActives.setText(String.valueOf(alertesActives));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void chargerVue(String fxmlPath, Button activeButton) {
        try {
            Node node = FXMLLoader.load(getClass().getResource(fxmlPath));

            AnchorPane.setTopAnchor(node, 0.0);
            AnchorPane.setBottomAnchor(node, 0.0);
            AnchorPane.setLeftAnchor(node, 0.0);
            AnchorPane.setRightAnchor(node, 0.0);

            paneContenu.getChildren().setAll(node);

            restaurerBoutonsSidebar();
            activeButton.getStyleClass().remove("nav-button");
            activeButton.getStyleClass().add("nav-button-active");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void afficherDashboardHome() {
        if (vueDefaut != null) {
            paneContenu.getChildren().setAll(vueDefaut);
        }
        restaurerBoutonsSidebar();
        btnDashboard.getStyleClass().remove("nav-button");
        btnDashboard.getStyleClass().add("nav-button-active");

        chargerMetriques();
        chargerActivitesRecentes();
    }

    private void restaurerBoutonsSidebar() {
        Button[] buttons = { btnDashboard, btnVente, btnInventaire, btnCatalogue, btnLots, btnAlertes, btnRapports };
        for (Button btn : buttons) {
            btn.getStyleClass().removeAll("nav-button", "nav-button-active");
            btn.getStyleClass().add("nav-button");
        }
    }

    private void handleDeconnexion() {
        try {
            SessionManager.clearSession();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Node root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) btnDeconnexion.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene((javafx.scene.Parent) root));
            stage.setTitle("Pharmacie - Connexion");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    public static void rafraichir() {
        if (instance != null) {
            instance.chargerMetriques();
            instance.chargerActivitesRecentes();
        }
    }

    private void handleSupprimerVentes() {
        List<Activite> selection = new ArrayList<>(tableActiviteRecente.getSelectionModel().getSelectedItems());
        if (selection.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner au moins une ligne.");
            a.setHeaderText(null);
            a.showAndWait();
            return;
        }

        java.util.Set<Integer> idVentes = new java.util.LinkedHashSet<>();
        for (Activite act : selection) {
            idVentes.add(act.getIdVente());
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer " + idVentes.size() + " vente(s) ? Cette action est irréversible.",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    VenteDAO vd = new VenteDAO();
                    for (int id : idVentes)
                        vd.delete(id);
                    chargerMetriques();
                    chargerActivitesRecentes();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Alert err = new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression.");
                    err.setHeaderText(null);
                    err.showAndWait();
                }
            }
        });
    }

    private void chargerActivitesRecentes() {
        if (tableActiviteRecente == null)
            return;

        tableActiviteRecente.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        colActDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
        colActMedicament.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMedicament()));
        colActQte.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQte()));
        colActMontant.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getMontant()));
        colActVendeur.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getVendeur()));

        try {
            List<Object[]> acts = dashboardDAO.getActivitesRecentes();
            ObservableList<Activite> oList = FXCollections.observableArrayList();
            for (Object[] row : acts) {
                int idV = ((Number) row[0]).intValue();
                String date = row[1] != null ? row[1].toString() : "";
                String med = row[2] != null ? row[2].toString() : "";
                int qte = ((Number) row[3]).intValue();
                double mnt = ((Number) row[4]).doubleValue();
                String vend = row[5] != null ? row[5].toString() : "";
                oList.add(new Activite(idV, date, med, qte, mnt, vend));
            }
            tableActiviteRecente.setItems(oList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class Activite {
        private final int idVente;
        private final String date;
        private final String medicament;
        private final int qte;
        private final double montant;
        private final String vendeur;

        public Activite(int idVente, String d, String m, int q, double mt, String v) {
            this.idVente = idVente;
            this.date = d;
            this.medicament = m;
            this.qte = q;
            this.montant = mt;
            this.vendeur = v;
        }

        public int getIdVente() {
            return idVente;
        }

        public String getDate() {
            return date;
        }

        public String getMedicament() {
            return medicament;
        }

        public int getQte() {
            return qte;
        }

        public double getMontant() {
            return montant;
        }

        public String getVendeur() {
            return vendeur;
        }
    }
}
