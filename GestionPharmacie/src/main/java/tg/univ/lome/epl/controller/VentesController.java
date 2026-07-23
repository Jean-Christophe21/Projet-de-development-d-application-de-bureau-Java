package tg.univ.lome.epl.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tg.univ.lome.epl.dao.LotDAO;
import tg.univ.lome.epl.dao.VenteDAO;
import tg.univ.lome.epl.model.LigneVente;
import tg.univ.lome.epl.model.Lot;
import tg.univ.lome.epl.model.Vente;
import tg.univ.lome.epl.util.SessionManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

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
    private FilteredList<Lot> filteredData;
    private final ObservableList<LigneVente> panierList = FXCollections.observableArrayList();

    private double totalVente = 0.0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            this.lotDAO = new LotDAO();
            this.venteDAO = new VenteDAO();
        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur BDD",
                    "Impossible d'initialiser la connexion avec la base de données.");
        }

        TableColumn<Lot, String> colResMedicament = (TableColumn<Lot, String>) tableResultats.getColumns().get(0);
        TableColumn<Lot, String> colResLot = (TableColumn<Lot, String>) tableResultats.getColumns().get(1);
        TableColumn<Lot, Double> colResPrix = (TableColumn<Lot, Double>) tableResultats.getColumns().get(2);
        TableColumn<Lot, Integer> colResQte = (TableColumn<Lot, Integer>) tableResultats.getColumns().get(3);
        TableColumn<Lot, String> colResPerem = (TableColumn<Lot, String>) tableResultats.getColumns().get(4);

        colResMedicament
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNomMedicament()));
        colResLot.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumeroLot()));
        colResPrix.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrixUnitaire()));
        colResQte
                .setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getQuantiteRestante()));
        colResPerem.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDatePeremption()));

        filteredData = new FilteredList<>(resultatsList, p -> true);
        tableResultats.setItems(filteredData);

        TableColumn<LigneVente, String> colPanMedicament = (TableColumn<LigneVente, String>) tablePanier.getColumns()
                .get(0);
        TableColumn<LigneVente, String> colPanLot = (TableColumn<LigneVente, String>) tablePanier.getColumns().get(1);
        TableColumn<LigneVente, Integer> colPanQte = (TableColumn<LigneVente, Integer>) tablePanier.getColumns().get(2);
        TableColumn<LigneVente, Double> colPanPrix = (TableColumn<LigneVente, Double>) tablePanier.getColumns().get(3);
        TableColumn<LigneVente, Double> colPanTotal = (TableColumn<LigneVente, Double>) tablePanier.getColumns().get(4);

        colPanMedicament
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNomMedicament()));
        colPanLot.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumeroLot()));
        colPanQte.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getQuantite()));
        colPanPrix.setCellValueFactory(
                cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrixUnitaireApplique()));
        colPanTotal.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getSousTotal()));

        tablePanier.setItems(panierList);

        txtRechercheMedicament.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(lot -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lower = newValue.toLowerCase().trim();
                return (lot.getNomMedicament() != null && lot.getNomMedicament().toLowerCase().contains(lower)) ||
                        (lot.getNumeroLot() != null && lot.getNumeroLot().toLowerCase().contains(lower));
            });
        });
        btnRechercher.setOnAction(e -> {
            // Forcer le focus ou un petit refresh si désiré.
            tableResultats.refresh();
        });
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
    }

    private void handleAjouterPanier() {
        Lot selectedLot = tableResultats.getSelectionModel().getSelectedItem();
        if (selectedLot == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Sélection requise",
                    "Veuillez sélectionner un médicament dans le tableau des résultats.");
            return;
        }

        if (selectedLot.getDatePeremption() != null) {
            java.time.LocalDate peremption = java.time.LocalDate.parse(selectedLot.getDatePeremption());
            if (peremption.isBefore(java.time.LocalDate.now())) {
                afficherAlerte(Alert.AlertType.ERROR, "Produit périmé",
                        "Ce produit est périmé et ne peut pas être vendu.");
                return;
            }
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
            afficherAlerte(Alert.AlertType.ERROR, "Saisie invalide",
                    "Veuillez saisir un entier strictement supérieur à 0.");
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
                    "Le lot sélectionné ne contient que " + (selectedLot.getQuantiteRestante() - qteDansPanier)
                            + " unités restantes.");
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
            afficherAlerte(Alert.AlertType.WARNING, "Panier vide",
                    "Veuillez ajouter des médicaments au panier avant de valider.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(totalVente));
        dialog.setTitle("Encaissement");
        dialog.setHeaderText(String.format("Total à payer : %,.2f FCFA", totalVente));
        dialog.setContentText("Montant reçu du client :");

        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent())
            return;

        double montantRecu = 0;
        try {
            montantRecu = Double.parseDouble(result.get());
        } catch (NumberFormatException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Le montant reçu doit être un nombre valide.");
            return;
        }

        if (montantRecu < totalVente) {
            afficherAlerte(Alert.AlertType.ERROR, "Fonds insuffisants",
                    "Le montant reçu est inférieur au total à payer.");
            return;
        }

        double monnaieRendue = montantRecu - totalVente;

        Vente vente = new Vente();
        vente.setMontantTotal(totalVente);
        vente.setMontantRecu(montantRecu);
        vente.setMonnaieRendue(monnaieRendue);

        if (SessionManager.getUtilisateurConnecte() != null) {
            vente.setIdUtilisateur(SessionManager.getUtilisateurConnecte().getIdUtilisateur());
        }

        vente.setLignes(panierList);

        try {
            int idVente = venteDAO.insert(vente);
            vente.setIdVente(idVente);

            venteDAO.findById(idVente).ifPresent(venteSaved -> {
                vente.setDateVente(venteSaved.getDateVente());
                vente.setCodeVente(venteSaved.getCodeVente());
                if (venteSaved.getLignes() != null && !venteSaved.getLignes().isEmpty()) {
                    vente.setLignes(venteSaved.getLignes());
                }
            });

            if (vente.getLignes() == null || vente.getLignes().isEmpty()) {
                vente.setLignes(new java.util.ArrayList<>(panierList));
            }

            afficherAlerte(Alert.AlertType.INFORMATION, "Vente validée",
                    String.format("Vente enregistrée avec succès !\nMonnaie à rendre : %,.2f FCFA", monnaieRendue));
            panierList.clear();
            calculerTotal();
            handleRecherche();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Recu.fxml"));
                Parent root = loader.load();
                RecuController controller = loader.getController();
                controller.setVente(vente);

                Stage stage = new Stage();
                stage.setTitle("Ticket de Caisse");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.showAndWait();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur de validation",
                    "Erreur lors de l'enregistrement de la vente : " + e.getMessage());
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
