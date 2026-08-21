package tg.univ.lome.epl.controller;

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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur de la vue Alertes.
 */
public class AlertesController implements Initializable {

    @FXML private Label lblTotalRupture;
    @FXML private Label lblTotalCritique;
    @FXML private Label lblTotalPeremption;

    @FXML private TableView<Medicament> tableAlertesRupture;
    @FXML private TableView<Lot> tableAlertesPeremption;

    private MedicamentDAO medicamentDAO;
    private LotDAO lotDAO;

    private final ObservableList<Medicament> ruptureList = FXCollections.observableArrayList();
    private final ObservableList<Lot> peremptionList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            this.medicamentDAO = new MedicamentDAO();
            this.lotDAO = new LotDAO();
        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur BDD", "Impossible de charger la base de données.");
            return;
        }

        configurerColonnesRupture();
        configurerColonnesPeremption();

        chargerDonnees();
    }

    @SuppressWarnings("unchecked")
    private void configurerColonnesRupture() {
        TableColumn<Medicament, String>  colMed      = (TableColumn<Medicament, String>)  tableAlertesRupture.getColumns().get(0);
        TableColumn<Medicament, Integer> colStock    = (TableColumn<Medicament, Integer>) tableAlertesRupture.getColumns().get(1);
        TableColumn<Medicament, Integer> colSeuil    = (TableColumn<Medicament, Integer>) tableAlertesRupture.getColumns().get(2);
        TableColumn<Medicament, String>  colUrgence  = (TableColumn<Medicament, String>)  tableAlertesRupture.getColumns().get(3);

        colMed.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNomCommercial()));
        colStock.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getStockTotal()).asObject());
        colSeuil.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getSeuilAlerte()).asObject());

        colUrgence.setCellValueFactory(cd -> {
            int stock = cd.getValue().getStockTotal();
            return new SimpleStringProperty(stock == 0 ? "Rupture totale" : "Stock critique");
        });

        colUrgence.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Rupture totale".equals(item)) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Rouge
                    } else {
                        setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;"); // Orange
                    }
                }
            }
        });

        tableAlertesRupture.setItems(ruptureList);
    }

    @SuppressWarnings("unchecked")
    private void configurerColonnesPeremption() {
        TableColumn<Lot, String>  colMed     = (TableColumn<Lot, String>)  tableAlertesPeremption.getColumns().get(0);
        TableColumn<Lot, String>  colNum     = (TableColumn<Lot, String>)  tableAlertesPeremption.getColumns().get(1);
        TableColumn<Lot, Integer> colQte     = (TableColumn<Lot, Integer>) tableAlertesPeremption.getColumns().get(2);
        TableColumn<Lot, String>  colDate    = (TableColumn<Lot, String>)  tableAlertesPeremption.getColumns().get(3);
        TableColumn<Lot, String> colJours    = (TableColumn<Lot, String>) tableAlertesPeremption.getColumns().get(4);

        colMed.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNomMedicament()));
        colNum.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNumeroLot()));
        colQte.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getQuantiteRestante()).asObject());
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDatePeremption()));

        colJours.setCellValueFactory(cd -> {
            String dateStr = cd.getValue().getDatePeremption();
            if (dateStr == null) return new SimpleStringProperty("Inconnue");
            
            LocalDate peremption = LocalDate.parse(dateStr);
            long days = ChronoUnit.DAYS.between(LocalDate.now(), peremption);
            
            if (days < 0) return new SimpleStringProperty("Expiré (" + Math.abs(days) + " j)");
            if (days == 0) return new SimpleStringProperty("Expire aujourd'hui");
            return new SimpleStringProperty(days + " jours");
        });

        colJours.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("Expiré") || item.contains("aujourd'hui")) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Rouge
                    } else {
                        setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;"); // Orange
                    }
                }
            }
        });

        tableAlertesPeremption.setItems(peremptionList);
    }

    private void chargerDonnees() {
        try {
            List<Medicament> alertesRupture = medicamentDAO.findEnRupture();
            ruptureList.setAll(alertesRupture);

            int countRupture = 0;
            int countCritique = 0;
            for (Medicament m : alertesRupture) {
                if (m.getStockTotal() == 0) countRupture++;
                else countCritique++;
            }
            lblTotalRupture.setText(String.valueOf(countRupture));
            lblTotalCritique.setText(String.valueOf(countCritique));

            List<Lot> expires = lotDAO.findLotsExpires();
            List<Lot> bientotExpires = lotDAO.findLotsExpirantDans(90); // Seuil de 3 mois pour les alertes
            
            peremptionList.setAll(expires);
            peremptionList.addAll(bientotExpires);
            
            lblTotalPeremption.setText(String.valueOf(peremptionList.size()));

        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les données d'alerte.");
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
