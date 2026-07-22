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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import tg.univ.lome.epl.dao.DashboardDAO;
import tg.univ.lome.epl.model.Utilisateur;
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
    private AnchorPane paneContenu;

    private DashboardDAO dashboardDAO;
    private Node vueDefaut; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // nregistrer la vue par défaut du dashboard afin de l'utiliser pls tard
        if (!paneContenu.getChildren().isEmpty()) {
            vueDefaut = paneContenu.getChildren().get(0);
        }

        // 2. Afficher l'utilisateur connecté
        Utilisateur user = SessionManager.getUtilisateurConnecte();
        if (user != null) {
            lblUtilisateurConnecte.setText("Connecté(e) en tant que : " + user.getPrenom() + " " + user.getNom());
        } else {
            lblUtilisateurConnecte.setText("Connecté(e) en tant que : Invité");
        }

        // 3. Charger les métriques du dashboard
        try {
            this.dashboardDAO = new DashboardDAO();
            chargerMetriques();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 4. Associer les écouteurs d'événements pour la navigation
        btnDashboard.setOnAction(e -> afficherDashboardHome());
        btnVente.setOnAction(e -> chargerVue("/fxml/Ventes.fxml", btnVente));
        btnInventaire.setOnAction(e -> chargerVue("/fxml/Inventaire.fxml", btnInventaire));
        btnCatalogue.setOnAction(e -> chargerVue("/fxml/Catalogue.fxml", btnCatalogue));
        btnLots.setOnAction(e -> chargerVue("/fxml/Lots.fxml", btnLots));
        btnAlertes.setOnAction(e -> chargerVue("/fxml/Alertes.fxml", btnAlertes));
        btnRapports.setOnAction(e -> chargerVue("/fxml/Rapports.fxml", btnRapports));
        btnDeconnexion.setOnAction(e -> handleDeconnexion());
    }

    // fonction pour charger les mésures
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

        // mettre à jour les métriques
        chargerMetriques();
    }

    private void restaurerBoutonsSidebar() {
        Button[] buttons = {btnDashboard, btnVente, btnInventaire, btnCatalogue, btnLots, btnAlertes, btnRapports};
        for (Button btn : buttons) {
            btn.getStyleClass().removeAll("nav-button", "nav-button-active");
            btn.getStyleClass().add("nav-button");
        }
    }

    private void handleDeconnexion() {
        try {
            SessionManager.clearSession();

            // Retourner à l'écran de connexion
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
}
