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

/**
 * Contrôleur de la vue Catalogue des Médicaments.
 * Gère la liste des produits et le panneau de détails / formulaire
 */
public class CatalogueController implements Initializable {

    //  Composants FXML
    @FXML private TextField        txtRechercheCatalogue;
    @FXML private Button           btnNouveauProduit;
    @FXML private TableView<Medicament> tableCatalogue;

    // Formulaire de détail (panneau droit)
    @FXML private TextField  txtNomProduit;
    @FXML private TextArea   txtDescriptionProduit;
    @FXML private TextField  txtSeuilAlerteProduit;
    @FXML private ComboBox<Categorie> cbCategorieProduit;
    @FXML private Button btnEnregistrerProduit;
    @FXML private Button btnModifierProduit;
    @FXML private Button btnSupprimerProduit;

    // DAOs
    private MedicamentDAO medicamentDAO;
    private CategorieDAO  categorieDAO;

    //  État interne 
    private final ObservableList<Medicament> catalogueList = FXCollections.observableArrayList();

    private Medicament medicamentSelectionne = null;

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
        chargerTousProduits();
        configurerEcouteurs();
        viderFormulaire();
    }

    // Configuration des colonnes de la table 
    @SuppressWarnings("unchecked")
    private void configurerColonnes() {
        TableColumn<Medicament, String>  colNom    = (TableColumn<Medicament, String>)  tableCatalogue.getColumns().get(0);
        TableColumn<Medicament, Integer> colSeuil  = (TableColumn<Medicament, Integer>) tableCatalogue.getColumns().get(1);
        TableColumn<Medicament, String>  colCat    = (TableColumn<Medicament, String>)  tableCatalogue.getColumns().get(2);
        TableColumn<Medicament, Integer> colStock  = (TableColumn<Medicament, Integer>) tableCatalogue.getColumns().get(3);

        colNom.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getNomCommercial()));

        colSeuil.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getSeuilAlerte()).asObject());

        colCat.setCellValueFactory(cd -> {
            List<Categorie> cats = cd.getValue().getCategories();
            String lib = cats.isEmpty() ? "—"
                    : cats.stream().map(Categorie::getLibelle)
                          .reduce((a, b) -> a + ", " + b).orElse("—");
            return new SimpleStringProperty(lib);
        });

        colStock.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getStockTotal()).asObject());

        tableCatalogue.setItems(catalogueList);
    }

    //  Chargement du ComboBox catégories
    private void chargerCategories() {
        try {
            List<Categorie> categories = categorieDAO.findAll();
            cbCategorieProduit.setItems(FXCollections.observableArrayList(categories));
            cbCategorieProduit.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Categorie c, boolean empty) {
                    super.updateItem(c, empty);
                    setText(empty || c == null ? "" : c.getLibelle());
                }
            });
            cbCategorieProduit.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Categorie c, boolean empty) {
                    super.updateItem(c, empty);
                    setText(empty || c == null ? "—" : c.getLibelle());
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Chargement initial de tous les produits 
    private void chargerTousProduits() {
        try {
            catalogueList.setAll(medicamentDAO.findAll());
        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le catalogue.");
        }
    }

    private void configurerEcouteurs() {
        // Recherche en temps réel
        txtRechercheCatalogue.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                chargerTousProduits();
            } else {
                try {
                    catalogueList.setAll(medicamentDAO.search(newVal.trim()));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        tableCatalogue.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        remplirFormulaire(newVal);
                    }
                });

        btnNouveauProduit.setOnAction(e -> {
            tableCatalogue.getSelectionModel().clearSelection();
            viderFormulaire();
        });

        btnEnregistrerProduit.setOnAction(e -> enregistrerProduit());

        btnModifierProduit.setOnAction(e -> modifierProduit());

        btnSupprimerProduit.setOnAction(e -> supprimerProduit());
    }

    private void remplirFormulaire(Medicament m) {
        medicamentSelectionne = m;
        txtNomProduit.setText(m.getNomCommercial());
        txtDescriptionProduit.setText(m.getDescription() != null ? m.getDescription() : "");
        txtSeuilAlerteProduit.setText(String.valueOf(m.getSeuilAlerte()));

        // Sélectionner la 1ère catégorie du médicament dans le ComboBox
        if (!m.getCategories().isEmpty()) {
            int idCat = m.getCategories().get(0).getIdCategorie();
            cbCategorieProduit.getItems().stream()
                    .filter(c -> c != null && c.getIdCategorie() == idCat)
                    .findFirst()
                    .ifPresent(cbCategorieProduit::setValue);
        } else {
            cbCategorieProduit.setValue(null);
        }
    }

    private void viderFormulaire() {
        medicamentSelectionne = null;
        txtNomProduit.clear();
        txtDescriptionProduit.clear();
        txtSeuilAlerteProduit.clear();
        cbCategorieProduit.setValue(null);
    }

 
    private void enregistrerProduit() {
        if (!validerFormulaire()) return;

        Medicament m = new Medicament();
        m.setNomCommercial(txtNomProduit.getText().trim());
        m.setDescription(txtDescriptionProduit.getText().trim());
        m.setSeuilAlerte(Integer.parseInt(txtSeuilAlerteProduit.getText().trim()));

        try {
            int newId = medicamentDAO.insert(m);
            // Association à la catégorie choisie
            Categorie cat = cbCategorieProduit.getValue();
            if (cat != null && newId > 0) {
                categorieDAO.addMedicamentToCategorie(newId, cat.getIdCategorie());
            }
            afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Produit ajouté avec succès.");
            chargerTousProduits();
            viderFormulaire();
        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'enregistrer le produit : " + e.getMessage());
        }
    }

    private void modifierProduit() {
        if (medicamentSelectionne == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner un produit dans la liste avant de modifier.");
            return;
        }
        if (!validerFormulaire()) return;

        medicamentSelectionne.setNomCommercial(txtNomProduit.getText().trim());
        medicamentSelectionne.setDescription(txtDescriptionProduit.getText().trim());
        medicamentSelectionne.setSeuilAlerte(Integer.parseInt(txtSeuilAlerteProduit.getText().trim()));

        try {
            medicamentDAO.update(medicamentSelectionne);
            afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Produit modifié avec succès.");
            chargerTousProduits();
            viderFormulaire();
        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier le produit : " + e.getMessage());
        }
    }

    private void supprimerProduit() {
        if (medicamentSelectionne == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner un produit dans la liste avant de supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer le produit « " + medicamentSelectionne.getNomCommercial() + " » ?");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    medicamentDAO.delete(medicamentSelectionne.getIdMedicament());
                    afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Produit supprimé avec succès.");
                    chargerTousProduits();
                    viderFormulaire();
                } catch (SQLException e) {
                    e.printStackTrace();
                    afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                            "Impossible de supprimer le produit (il est peut-être lié à des lots ou des ventes).");
                }
            }
        });
    }

    private boolean validerFormulaire() {
        if (txtNomProduit.getText().isBlank()) {
            afficherAlerte(Alert.AlertType.WARNING, "Champ manquant", "Le nom du produit est obligatoire.");
            return false;
        }
        try {
            int seuil = Integer.parseInt(txtSeuilAlerteProduit.getText().trim());
            if (seuil < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            afficherAlerte(Alert.AlertType.WARNING, "Valeur invalide",
                    "Le seuil d'alerte doit être un nombre entier positif.");
            return false;
        }
        return true;
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
