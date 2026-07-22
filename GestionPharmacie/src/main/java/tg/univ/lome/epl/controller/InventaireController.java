package tg.univ.lome.epl.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tg.univ.lome.epl.dao.CategorieDAO;
import tg.univ.lome.epl.dao.MedicamentDAO;
import tg.univ.lome.epl.model.Categorie;
import tg.univ.lome.epl.model.Medicament;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;


// Contrôleur de la vue Inventaire.
 
public class InventaireController implements Initializable {

    //Composants FXML
    @FXML private TextField txtRechercheInventaire;
    @FXML private ComboBox<Categorie> cbFiltreCategorie;
    @FXML private Button btnFiltrer;
    @FXML private TableView<Medicament> tableInventaire;
    @FXML private Label lblNombreProduits;

    // élément DAO
    private MedicamentDAO medicamentDAO;
    private CategorieDAO categorieDAO;

    // Données du tableView
    private final ObservableList<Medicament> medicamentsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            this.medicamentDAO = new MedicamentDAO();
            this.categorieDAO  = new CategorieDAO();
        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur BDD",
                    "Impossible d'initialiser la connexion avec la base de données.");
            return;
        }

        configurerColonnes();
        chargerCategories();
        chargerTousMedicaments();
        configurerEcouteurs();
    }

    // Configuration des colonnes
    @SuppressWarnings("unchecked")
    private void configurerColonnes() {
        TableColumn<Medicament, String>  colNom      = (TableColumn<Medicament, String>)  tableInventaire.getColumns().get(0);
        TableColumn<Medicament, String>  colCategorie= (TableColumn<Medicament, String>)  tableInventaire.getColumns().get(1);
        TableColumn<Medicament, Integer> colQte      = (TableColumn<Medicament, Integer>) tableInventaire.getColumns().get(2);
        TableColumn<Medicament, Integer> colSeuil    = (TableColumn<Medicament, Integer>) tableInventaire.getColumns().get(3);
        TableColumn<Medicament, String>  colStatut   = (TableColumn<Medicament, String>)  tableInventaire.getColumns().get(4);

        colNom.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getNomCommercial()));

        colCategorie.setCellValueFactory(cd -> {
            List<Categorie> cats = cd.getValue().getCategories();
            String libelles = cats.isEmpty() ? "—"
                    : cats.stream().map(Categorie::getLibelle)
                          .reduce((a, b) -> a + ", " + b).orElse("—");
            return new SimpleStringProperty(libelles);
        });

        colQte.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getStockTotal()).asObject());

        colSeuil.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getSeuilAlerte()).asObject());

        colStatut.setCellValueFactory(cd -> {
            Medicament m = cd.getValue();
            if (m.getStockTotal() == 0)                      return new SimpleStringProperty("Rupture");
            if (m.getStockTotal() <= m.getSeuilAlerte())     return new SimpleStringProperty("Alerte");
            return new SimpleStringProperty("OK");
        });

        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    switch (statut) {
                        case "Rupture" : setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        case "Alerte"  : setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                        default        : setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    }
                }
            }
        });

        tableInventaire.setItems(medicamentsList);
    }

    // Chargement des catégories dans le ComboBox
    private void chargerCategories() {
        try {
            List<Categorie> categories = categorieDAO.findAll();
            ObservableList<Categorie> items = FXCollections.observableArrayList();

            items.add(null);
            items.addAll(categories);
            cbFiltreCategorie.setItems(items);
            cbFiltreCategorie.setValue(null);

            cbFiltreCategorie.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Categorie c, boolean empty) {
                    super.updateItem(c, empty);
                    setText(empty || c == null ? "Toutes" : c.getLibelle());
                }
            });
            cbFiltreCategorie.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Categorie c, boolean empty) {
                    super.updateItem(c, empty);
                    setText(empty || c == null ? "Toutes" : c.getLibelle());
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Chargement initial de tous les médicaments
    private void chargerTousMedicaments() {
        try {
            List<Medicament> meds = medicamentDAO.findAll();
            medicamentsList.setAll(meds);
            mettreAJourCompteur();
        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'inventaire.");
        }
    }

    // Écoute des actions utilisateur 
    private void configurerEcouteurs() {
        // Recherche en temps réel
        txtRechercheInventaire.textProperty().addListener((obs, oldVal, newVal) -> filtrer());

        // Bouton Filtrer
        btnFiltrer.setOnAction(e -> filtrer());

        tableInventaire.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> { /* extension future */ });
    }

    private void filtrer() {
        String motCle   = txtRechercheInventaire.getText().trim();
        Categorie categorie = cbFiltreCategorie.getValue();

        try {
            List<Medicament> resultats;

            if (categorie != null && !motCle.isEmpty()) {
                // Filtre combiné : catégorie + recherche texte
                resultats = medicamentDAO.findByCategorie(categorie.getIdCategorie());
                final String lower = motCle.toLowerCase();
                resultats.removeIf(m -> !m.getNomCommercial().toLowerCase().contains(lower));
            } else if (categorie != null) {
                resultats = medicamentDAO.findByCategorie(categorie.getIdCategorie());
            } else if (!motCle.isEmpty()) {
                resultats = medicamentDAO.search(motCle);
            } else {
                resultats = medicamentDAO.findAll();
            }

            medicamentsList.setAll(resultats);
            mettreAJourCompteur();

        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'effectuer le filtrage.");
        }
    }

    // Mise à jour du label de comptage
    private void mettreAJourCompteur() {
        lblNombreProduits.setText("Nombre de produits : " + medicamentsList.size());
    }

    // Utilitaire
    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
