package tg.univ.lome.epl.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tg.univ.lome.epl.dao.LotDAO;
import tg.univ.lome.epl.dao.VenteDAO;
import tg.univ.lome.epl.model.LigneVente;
import tg.univ.lome.epl.model.Lot;
import tg.univ.lome.epl.model.Vente;
import tg.univ.lome.epl.util.SessionManager;

/**
 * FXML Controller class for Ventes/Caisse
 */
public class VentesController implements Initializable {

    @FXML
    private TextField txtRechercheMedicament;

    @FXML
    private Button btnRechercher;

    @FXML
    private TableView<Lot> tableResultats;

    @FXML
    private Button btnAjouterPanier;

    @FXML
    private TableView<LigneVente> tablePanier;

    @FXML
    private Label lblTotal;

    @FXML
    private Button btnValiderVente;

    private LotDAO lotDAO;
    private VenteDAO venteDAO;

    private final ObservableList<Lot> resultatsList = FXCollections.observableArrayList();
    private final ObservableList<LigneVente> panierList = FXCollections.observableArrayList();

    private double totalVente = 0.0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            this.lotDAO = new LotDAO();
            this.venteDAO = new VenteDAO();
        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur BDD", "Impossible d'initialiser la connexion avec la base de données.");
        }

        TableColumn<Lot, String> colResMedicament = (TableColumn<Lot, String>) tableResultats.getColumns().get(0);
        TableColumn<Lot, String> colResLot = (TableColumn<Lot, String>) tableResultats.getColumns().get(1);
        TableColumn<Lot, Double> colResPrix = (TableColumn<Lot, Double>) tableResultats.getColumns().get(2);
        TableColumn<Lot, Integer> colResQte = (TableColumn<Lot, Integer>) tableResultats.getColumns().get(3);
        TableColumn<Lot, String> colResPerem = (TableColumn<Lot, String>) tableResultats.getColumns().get(4);

        colResMedicament.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNomMedicament()));
        colResLot.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumeroLot()));
        colResPrix.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrixUnitaire()));
        colResQte.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getQuantiteRestante()));
        colResPerem.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDatePeremption()));

        tableResultats.setItems(resultatsList);

        TableColumn<LigneVente, String> colPanMedicament = (TableColumn<LigneVente, String>) tablePanier.getColumns().get(0);
        TableColumn<LigneVente, String> colPanLot = (TableColumn<LigneVente, String>) tablePanier.getColumns().get(1);
        TableColumn<LigneVente, Integer> colPanQte = (TableColumn<LigneVente, Integer>) tablePanier.getColumns().get(2);
        TableColumn<LigneVente, Double> colPanPrix = (TableColumn<LigneVente, Double>) tablePanier.getColumns().get(3);
        TableColumn<LigneVente, Double> colPanTotal = (TableColumn<LigneVente, Double>) tablePanier.getColumns().get(4);

        colPanMedicament.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNomMedicament()));
        colPanLot.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumeroLot()));
        colPanQte.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getQuantite()));
        colPanPrix.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrixUnitaireApplique()));
        colPanTotal.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getSousTotal()));

        tablePanier.setItems(panierList);

        btnRechercher.setOnAction(e -> handleRecherche());
        txtRechercheMedicament.setOnAction(e -> handleRecherche());
        btnAjouterPanier.setOnAction(e -> handleAjouterPanier());
        btnValiderVente.setOnAction(e -> handleValiderVente());

        chargerTousLesLotsActifs();
    }

    private void chargerTousLesLotsActifs() {
        try {
            resultatsList.setAll(lotDAO.searchActiveLots(""));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleRecherche() {
        String keyword = txtRechercheMedicament.getText() != null ? txtRechercheMedicament.getText().trim() : "";
        try {
            resultatsList.setAll(lotDAO.searchActiveLots(keyword));
        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur Recherche", "Erreur lors de la recherche des médicaments.");
        }
    }

    private void handleAjouterPanier() {
        Lot selectedLot = tableResultats.getSelectionModel().getSelectedItem();
        if (selectedLot == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner un médicament dans le tableau des résultats.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Quantité de vente");
        dialog.setHeaderText("Ajouter au panier : " + selectedLot.getNomMedicament());
        dialog.setContentText("Saisissez la quantité :");

        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent()) {
            return;
        }

        int qte;
        try {
            qte = Integer.parseInt(result.get());
            if (qte <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Saisie invalide", "Veuillez saisir un entier strictement supérieur à 0.");
            return;
        }

        int qteDansPanier = 0;
        for (LigneVente lv : panierList) {
            if (lv.getIdLot() == selectedLot.getIdLot()) {
                qteDansPanier += lv.getQuantite();
            }
        }

        if (qte + qteDansPanier > selectedLot.getQuantiteRestante()) {
            afficherAlerte(Alert.AlertType.ERROR, "Stock insuffisant",
                    "Le lot sélectionné ne contient que " + (selectedLot.getQuantiteRestante() - qteDansPanier) + " unités restantes.");
            return;
        }

        boolean produitExisteDeja = false;
        for (LigneVente lv : panierList) {
            if (lv.getIdLot() == selectedLot.getIdLot()) {
                lv.setQuantite(lv.getQuantite() + qte);
                produitExisteDeja = true;
                break;
            }
        }

        if (!produitExisteDeja) {
            LigneVente ligne = new LigneVente();
            ligne.setIdLot(selectedLot.getIdLot());
            ligne.setNomMedicament(selectedLot.getNomMedicament());
            ligne.setNumeroLot(selectedLot.getNumeroLot());
            ligne.setQuantite(qte);
            ligne.setPrixUnitaireApplique(selectedLot.getPrixUnitaire());
            panierList.add(ligne);
        }

        tablePanier.refresh();
        calculerTotal();
    }

    private void calculerTotal() {
        totalVente = 0.0;
        for (LigneVente lv : panierList) {
            totalVente += lv.getSousTotal();
        }
        lblTotal.setText(String.format("%,.2f FCFA", totalVente));
    }

    private void handleValiderVente() {
        if (panierList.isEmpty()) {
            afficherAlerte(Alert.AlertType.WARNING, "Panier vide", "Veuillez ajouter des médicaments au panier avant de valider.");
            return;
        }

        Vente vente = new Vente();
        vente.setMontantTotal(totalVente);
        vente.setMontantRecu(totalVente); // Payé au comptant par défaut

        if (SessionManager.getUtilisateurConnecte() != null) {
            vente.setIdUtilisateur(SessionManager.getUtilisateurConnecte().getIdUtilisateur());
        }

        vente.setLignes(panierList);

        try {
            venteDAO.insert(vente);
            afficherAlerte(Alert.AlertType.INFORMATION, "Vente validée", "La vente a été enregistrée avec succès !");
            panierList.clear();
            calculerTotal();
            handleRecherche(); 
        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur de validation", "Erreur lors de l'enregistrement de la vente : " + e.getMessage());
        }
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
