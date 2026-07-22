package tg.univ.lome.epl.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import tg.univ.lome.epl.dao.VenteDAO;
import tg.univ.lome.epl.model.LigneVente;
import tg.univ.lome.epl.model.Vente;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Contrôleur de la vue Rapports.
 * Permet de générer des rapports de ventes sur une période donnée,
 */
public class RapportsController implements Initializable {

    // Cartes d'informations 
    @FXML private Label lblTotalChiffreAffaires;
    @FXML private Label lblNombreVentes;
    @FXML private Label lblPanierMoyen;

    // Composants FXML
    @FXML private ComboBox<String> cbTypeRapport;
    @FXML private DatePicker dpDateDebut;
    @FXML private DatePicker dpDateFin;
    @FXML private Button btnGenererRapport;
    @FXML private Button btnExporterRapport;

    @FXML private TableView<LigneRapport> tableRapport;
    @FXML private BarChart<String, Number> chartRapport;

    private VenteDAO venteDAO;
    private final ObservableList<LigneRapport> rapportList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            venteDAO = new VenteDAO();
        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur BDD", "Impossible d'initialiser la base de données.");
            return;
        }

        cbTypeRapport.setItems(FXCollections.observableArrayList("Ventes Globales", "Ventes par Utilisateur"));
        cbTypeRapport.setValue("Ventes Globales");
        
        dpDateDebut.setValue(LocalDate.now().withDayOfMonth(1));
        dpDateFin.setValue(LocalDate.now());

        configurerColonnes();
        
        btnGenererRapport.setOnAction(e -> genererRapport());
        btnExporterRapport.setOnAction(e -> exporterRapport());
        cbTypeRapport.setOnAction(e -> genererRapport());
        
        genererRapport();
    }

    @SuppressWarnings("unchecked")
    private void configurerColonnes() {
        TableColumn<LigneRapport, String>  colDate   = (TableColumn<LigneRapport, String>)  tableRapport.getColumns().get(0);
        TableColumn<LigneRapport, String>  colDesc   = (TableColumn<LigneRapport, String>)  tableRapport.getColumns().get(1);
        TableColumn<LigneRapport, Integer> colQte    = (TableColumn<LigneRapport, Integer>) tableRapport.getColumns().get(2);
        TableColumn<LigneRapport, Double>  colTotal  = (TableColumn<LigneRapport, Double>)  tableRapport.getColumns().get(3);

        colDate.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDate()));
        colDesc.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDescription()));
        colQte.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getQuantite()).asObject());
        colTotal.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getMontant()).asObject());

        tableRapport.setItems(rapportList);
    }

    private void genererRapport() {
        LocalDate debut = dpDateDebut.getValue();
        LocalDate fin = dpDateFin.getValue();

        if (debut == null || fin == null || debut.isAfter(fin)) {
            afficherAlerte(Alert.AlertType.WARNING, "Dates invalides", "Veuillez sélectionner une plage de dates valide.");
            return;
        }

        try {
            List<Vente> toutesLesVentes = venteDAO.findAll();
            
            List<Vente> ventesFiltrees = toutesLesVentes.stream().filter(v -> {
                if (v.getDateVente() == null) return false;
                String dateOnly = v.getDateVente().split(" ")[0];
                try {
                    LocalDate dateVente = LocalDate.parse(dateOnly);
                    return !dateVente.isBefore(debut) && !dateVente.isAfter(fin);
                } catch (Exception e) {
                    return false;
                }
            }).collect(Collectors.toList());

            double totalCA = ventesFiltrees.stream().mapToDouble(Vente::getMontantTotal).sum();
            int nbVentes = ventesFiltrees.size();
            double panierMoyen = nbVentes > 0 ? (totalCA / nbVentes) : 0.0;

            lblTotalChiffreAffaires.setText(String.format("%.0f FCFA", totalCA));
            lblNombreVentes.setText(String.valueOf(nbVentes));
            lblPanierMoyen.setText(String.format("%.0f FCFA", panierMoyen));

            rapportList.clear();
            String typeRapport = cbTypeRapport.getValue();

            if ("Ventes par Utilisateur".equals(typeRapport)) {
                Map<String, List<Vente>> parUtilisateur = ventesFiltrees.stream()
                        .collect(Collectors.groupingBy(v -> v.getNomUtilisateur() != null ? v.getNomUtilisateur() : "Admin"));

                parUtilisateur.forEach((user, liste) -> {
                    double caUser = liste.stream().mapToDouble(Vente::getMontantTotal).sum();
                    int countUser = liste.size();
                    rapportList.add(new LigneRapport("Période sélectionnée", "Agent : " + user, countUser, caUser));
                });
            } else {
                for (Vente v : ventesFiltrees) {
                    String dateStr = v.getDateVente().split(" ")[0];
                    String user = (v.getNomUtilisateur() != null) ? v.getNomUtilisateur() : "Admin";
                    String desc = "Vente #" + v.getIdVente() + " (" + user + ")";
                    
                    int nbArticles = 0;
                    try {
                        List<LigneVente> lignes = venteDAO.findLignesByVente(v.getIdVente());
                        nbArticles = lignes.stream().mapToInt(LigneVente::getQuantite).sum();
                    } catch (Exception ignored) {}

                    rapportList.add(new LigneRapport(dateStr, desc, nbArticles > 0 ? nbArticles : 1, v.getMontantTotal()));
                }
            }

            mettreAJourGraphique(ventesFiltrees, typeRapport);

        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible de générer le rapport.");
        }
    }

    private void mettreAJourGraphique(List<Vente> ventes, String typeRapport) {
        chartRapport.getData().clear();
        chartRapport.setLegendVisible("Ventes par Utilisateur".equals(typeRapport));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Chiffre d'Affaires");

        if ("Ventes par Utilisateur".equals(typeRapport)) {
            Map<String, Double> caParUser = new HashMap<>();
            for (Vente v : ventes) {
                String u = v.getNomUtilisateur() != null ? v.getNomUtilisateur() : "Admin";
                caParUser.put(u, caParUser.getOrDefault(u, 0.0) + v.getMontantTotal());
            }
            caParUser.forEach((u, ca) -> series.getData().add(new XYChart.Data<>(u, ca)));
        } else {
            Map<String, Double> montantsParJour = new TreeMap<>();
            for (Vente v : ventes) {
                String dateStr = v.getDateVente().split(" ")[0];
                montantsParJour.put(dateStr, montantsParJour.getOrDefault(dateStr, 0.0) + v.getMontantTotal());
            }
            for (Map.Entry<String, Double> entry : montantsParJour.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        }

        chartRapport.getData().add(series);
    }

    private void exporterRapport() {
        afficherAlerte(Alert.AlertType.INFORMATION, "Export", "L'exportation (CSV/PDF) n'est pas encore implémentée.");
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // classe pour mapper les données du tableau
    public static class LigneRapport {
        private String date;
        private String description;
        private int quantite;
        private double montant;

        public LigneRapport(String date, String description, int quantite, double montant) {
            this.date = date;
            this.description = description;
            this.quantite = quantite;
            this.montant = montant;
        }

        public String getDate() { return date; }
        public String getDescription() { return description; }
        public int getQuantite() { return quantite; }
        public double getMontant() { return montant; }
    }
}
