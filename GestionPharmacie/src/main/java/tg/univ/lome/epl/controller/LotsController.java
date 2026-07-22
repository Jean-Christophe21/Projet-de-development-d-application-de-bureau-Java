package tg.univ.lome.epl.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tg.univ.lome.epl.dao.LotDAO;
import tg.univ.lome.epl.dao.MedicamentDAO;
import tg.univ.lome.epl.model.Lot;
import tg.univ.lome.epl.model.Medicament;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur de la vue Gestion des Lots d'approvisionnement.
 * Permet d'ajouter, modifier ou supprimer des lots de médicaments.
 */
public class LotsController implements Initializable {

    // Composants FXML
    @FXML private TableView<Lot> tableLots;
    @FXML private Button         btnNouveauLot;

    // Formulaire de détail
    @FXML private ComboBox<Medicament> cbMedicamentLot;
    @FXML private TextField txtNumeroLot;
    @FXML private TextField txtQuantiteInitialeLot;
    @FXML private TextField txtPrixUnitaireLot;
    @FXML private DatePicker dpDatePeremption;

    @FXML private Button btnEnregistrerLot;
    @FXML private Button btnModifierLot;
    @FXML private Button btnSupprimerLot;

    //  DAO
    private LotDAO lotDAO;
    private MedicamentDAO medicamentDAO;

    // État interne
    private final ObservableList<Lot> lotsList = FXCollections.observableArrayList();
    private Lot lotSelectionne = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            this.lotDAO = new LotDAO();
            this.medicamentDAO = new MedicamentDAO();
        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur BDD",
                    "Impossible d'initialiser la connexion avec la base de données.");
            return;
        }

        configurerColonnes();
        chargerMedicaments();
        chargerTousLots();
        configurerEcouteurs();
        viderFormulaire();
    }

    //  Configuration des colonnes
    @SuppressWarnings("unchecked")
    private void configurerColonnes() {
        TableColumn<Lot, String>  colMed      = (TableColumn<Lot, String>)  tableLots.getColumns().get(0);
        TableColumn<Lot, String>  colNum      = (TableColumn<Lot, String>)  tableLots.getColumns().get(1);
        TableColumn<Lot, Integer> colQteInit  = (TableColumn<Lot, Integer>) tableLots.getColumns().get(2);
        TableColumn<Lot, Integer> colQteRest  = (TableColumn<Lot, Integer>) tableLots.getColumns().get(3);
        TableColumn<Lot, Double>  colPrix     = (TableColumn<Lot, Double>)  tableLots.getColumns().get(4);
        TableColumn<Lot, String>  colDate     = (TableColumn<Lot, String>)  tableLots.getColumns().get(5);
        TableColumn<Lot, String>  colStatut   = (TableColumn<Lot, String>)  tableLots.getColumns().get(6);

        colMed.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNomMedicament()));
        colNum.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNumeroLot()));
        colQteInit.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getQuantiteInitiale()).asObject());
        colQteRest.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getQuantiteRestante()).asObject());
        colPrix.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getPrixUnitaire()).asObject());
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDatePeremption()));

        colStatut.setCellValueFactory(cd -> {
            Lot l = cd.getValue();
            if (l.getQuantiteRestante() == 0) {
                return new SimpleStringProperty("Épuisé");
            }
            if (l.getDatePeremption() != null && LocalDate.parse(l.getDatePeremption()).isBefore(LocalDate.now())) {
                return new SimpleStringProperty("Expiré");
            }
            if (l.expirereDans(30)) {
                return new SimpleStringProperty("Expire bientôt");
            }
            return new SimpleStringProperty("OK");
        });


        colStatut.setCellFactory(col -> {
            return new TableCell<>() {
                @Override
                protected void updateItem(String statut, boolean empty) {
                    super.updateItem(statut, empty);
                    if (empty || statut == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(statut);
                        switch (statut) {
                            case "Épuisé"        : setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold;");
                            case "Expiré"        : setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            case "Expire bientôt": setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                            default              : setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        }
                    }
                }
            };
        });

        tableLots.setItems(lotsList);
    }

    // Chargement des données 
    private void chargerMedicaments() {
        try {
            List<Medicament> meds = medicamentDAO.findAll();
            cbMedicamentLot.setItems(FXCollections.observableArrayList(meds));
            cbMedicamentLot.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Medicament m, boolean empty) {
                    super.updateItem(m, empty);
                    setText(empty || m == null ? "" : m.getNomCommercial());
                }
            });
            cbMedicamentLot.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Medicament m, boolean empty) {
                    super.updateItem(m, empty);
                    setText(empty || m == null ? "—" : m.getNomCommercial());
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void chargerTousLots() {
        try {
            lotsList.setAll(lotDAO.findAll());
        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les lots.");
        }
    }

    private void configurerEcouteurs() {
        tableLots.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                remplirFormulaire(newVal);
            }
        });

        btnNouveauLot.setOnAction(e -> {
            tableLots.getSelectionModel().clearSelection();
            viderFormulaire();
        });

        btnEnregistrerLot.setOnAction(e -> enregistrerLot());
        btnModifierLot.setOnAction(e -> modifierLot());
        btnSupprimerLot.setOnAction(e -> supprimerLot());
    }

    private void viderFormulaire() {
        lotSelectionne = null;
        cbMedicamentLot.setValue(null);
        txtNumeroLot.clear();
        txtQuantiteInitialeLot.clear();
        txtPrixUnitaireLot.clear();
        dpDatePeremption.setValue(null);
    }

    private void remplirFormulaire(Lot l) {
        lotSelectionne = l;
        txtNumeroLot.setText(l.getNumeroLot());
        txtQuantiteInitialeLot.setText(String.valueOf(l.getQuantiteInitiale()));
        txtPrixUnitaireLot.setText(String.valueOf(l.getPrixUnitaire()));
        if (l.getDatePeremption() != null) {
            dpDatePeremption.setValue(LocalDate.parse(l.getDatePeremption()));
        } else {
            dpDatePeremption.setValue(null);
        }

        cbMedicamentLot.getItems().stream()
                .filter(m -> m != null && m.getIdMedicament() == l.getIdMedicament())
                .findFirst()
                .ifPresent(cbMedicamentLot::setValue);
    }

    private void enregistrerLot() {
        if (!validerFormulaire()) return;

        Lot l = new Lot();
        l.setIdMedicament(cbMedicamentLot.getValue().getIdMedicament());
        l.setNumeroLot(txtNumeroLot.getText().trim());
        int qte = Integer.parseInt(txtQuantiteInitialeLot.getText().trim());
        l.setQuantiteInitiale(qte);
        l.setQuantiteRestante(qte); // A la création, la qte restante = qte initiale
        l.setPrixUnitaire(Double.parseDouble(txtPrixUnitaireLot.getText().trim()));
        l.setDatePeremption(dpDatePeremption.getValue().toString());

        try {
            lotDAO.insert(l);
            afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Lot ajouté avec succès.");
            chargerTousLots();
            viderFormulaire();
        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'enregistrer le lot.");
        }
    }

    private void modifierLot() {
        if (lotSelectionne == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un lot à modifier.");
            return;
        }
        if (!validerFormulaire()) return;

        lotSelectionne.setIdMedicament(cbMedicamentLot.getValue().getIdMedicament());
        lotSelectionne.setNumeroLot(txtNumeroLot.getText().trim());
        int newInit = Integer.parseInt(txtQuantiteInitialeLot.getText().trim());
        int diff = newInit - lotSelectionne.getQuantiteInitiale();
        lotSelectionne.setQuantiteInitiale(newInit);
        // Ajustement proportionnel de la qté restante (ex: s'il s'est trompé sur l'arrivage)
        lotSelectionne.setQuantiteRestante(Math.max(0, lotSelectionne.getQuantiteRestante() + diff));
        
        lotSelectionne.setPrixUnitaire(Double.parseDouble(txtPrixUnitaireLot.getText().trim()));
        lotSelectionne.setDatePeremption(dpDatePeremption.getValue().toString());

        try {
            lotDAO.update(lotSelectionne);
            afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Lot modifié avec succès.");
            chargerTousLots();
            viderFormulaire();
        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier le lot.");
        }
    }

    private void supprimerLot() {
        if (lotSelectionne == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un lot à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer le lot N° " + lotSelectionne.getNumeroLot() + " ?");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    lotDAO.delete(lotSelectionne.getIdLot());
                    afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Lot supprimé avec succès.");
                    chargerTousLots();
                    viderFormulaire();
                } catch (SQLException e) {
                    e.printStackTrace();
                    afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer ce lot.");
                }
            }
        });
    }

    private boolean validerFormulaire() {
        if (cbMedicamentLot.getValue() == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un médicament.");
            return false;
        }
        if (txtNumeroLot.getText().isBlank()) {
            afficherAlerte(Alert.AlertType.WARNING, "Validation", "Veuillez renseigner le numéro de lot.");
            return false;
        }
        try {
            int q = Integer.parseInt(txtQuantiteInitialeLot.getText().trim());
            if (q <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            afficherAlerte(Alert.AlertType.WARNING, "Validation", "La quantité initiale doit être un nombre entier positif.");
            return false;
        }
        try {
            double p = Double.parseDouble(txtPrixUnitaireLot.getText().trim());
            if (p < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            afficherAlerte(Alert.AlertType.WARNING, "Validation", "Le prix unitaire doit être un nombre positif.");
            return false;
        }
        if (dpDatePeremption.getValue() == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner une date de péremption.");
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
