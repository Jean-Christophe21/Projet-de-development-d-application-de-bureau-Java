package tg.univ.lome.epl.controller;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tg.univ.lome.epl.model.LigneVente;
import tg.univ.lome.epl.model.Vente;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class RecuController implements Initializable {

    @FXML
    private Label lblNumeroTicket;
    @FXML
    private Label lblDate;
    @FXML
    private Label lblCaissier;
    @FXML
    private TableView<LigneVente> tableArticlesRecu;
    @FXML
    private Label txtMontantTotal;
    @FXML
    private Button btnImprimer;
    @FXML
    private Button btnFermerRecu;

    private Vente venteActuelle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        TableColumn<LigneVente, String> colArticle = (TableColumn<LigneVente, String>) tableArticlesRecu.getColumns()
                .get(0);
        TableColumn<LigneVente, Integer> colQte = (TableColumn<LigneVente, Integer>) tableArticlesRecu.getColumns()
                .get(1);
        TableColumn<LigneVente, Double> colSousTotal = (TableColumn<LigneVente, Double>) tableArticlesRecu.getColumns()
                .get(2);

        colArticle.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getNomMedicament() != null ? cd.getValue().getNomMedicament() : "—"));
        colQte.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getQuantite()));
        colSousTotal.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getSousTotal()));

        btnFermerRecu.setOnAction(e -> fermerFenetre());
        btnImprimer.setOnAction(e -> imprimerPDF());
    }

    public void setVente(Vente vente) {
        this.venteActuelle = vente;

        String code = vente.getCodeVente() != null ? vente.getCodeVente() : String.valueOf(vente.getIdVente());
        lblNumeroTicket.setText("Ticket N° : " + code);

        String dateStr = vente.getDateVente();
        if (dateStr == null || dateStr.isEmpty()) {
            dateStr = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        lblDate.setText("Date : " + dateStr);

        String caissier = vente.getNomUtilisateur() != null ? vente.getNomUtilisateur() : "Admin";
        lblCaissier.setText("Caissier : " + caissier);
        
        txtMontantTotal.setText(String.format("%,.2f FCFA", vente.getMontantTotal()));

        javafx.collections.ObservableList<LigneVente> items = FXCollections.observableArrayList(vente.getLignes());
        tableArticlesRecu.setItems(items);
        tableArticlesRecu.refresh();
    }

    private void imprimerPDF() {
        if (venteActuelle == null)
            return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le reçu");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Recu_" + venteActuelle.getIdVente() + ".pdf");
        File file = fileChooser.showSaveDialog(btnImprimer.getScene().getWindow());

        if (file != null) {
            try {
               
                Rectangle ticketSize = new Rectangle(226, 800);
                Document document = new Document(ticketSize, 10, 10, 10, 10);
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                Font boldFont = new Font(Font.HELVETICA, 12, Font.BOLD);
                Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

                Paragraph header = new Paragraph("PHARMACIE", boldFont);
                header.setAlignment(Element.ALIGN_CENTER);
                document.add(header);

                Paragraph subHeader = new Paragraph("Reçu de Vente\n\n", normalFont);
                subHeader.setAlignment(Element.ALIGN_CENTER);
                document.add(subHeader);

                document.add(new Paragraph("Ticket N° : " + venteActuelle.getIdVente(), normalFont));
                document.add(new Paragraph("Date : " + venteActuelle.getDateVente(), normalFont));
                document.add(new Paragraph("Caissier : "
                        + (venteActuelle.getNomUtilisateur() != null ? venteActuelle.getNomUtilisateur() : "Admin"),
                        normalFont));
                document.add(new Paragraph("----------------------------------------", normalFont));

                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.setWidths(new float[] { 2f, 1f, 1.5f });

                table.addCell(new PdfPCell(new Phrase("Art.", normalFont)));
                table.addCell(new PdfPCell(new Phrase("Qté", normalFont)));
                table.addCell(new PdfPCell(new Phrase("Total", normalFont)));

                for (LigneVente lv : venteActuelle.getLignes()) {
                    table.addCell(new PdfPCell(new Phrase(lv.getNomMedicament(), normalFont)));
                    table.addCell(new PdfPCell(new Phrase(String.valueOf(lv.getQuantite()), normalFont)));
                    table.addCell(new PdfPCell(new Phrase(String.valueOf(lv.getSousTotal()), normalFont)));
                }
                document.add(table);

                document.add(new Paragraph("----------------------------------------", normalFont));
                Paragraph total = new Paragraph(
                        "TOTAL : " + String.format("%,.2f FCFA", venteActuelle.getMontantTotal()), boldFont);
                total.setAlignment(Element.ALIGN_RIGHT);
                document.add(total);

                Paragraph recu = new Paragraph("Reçu : " + String.format("%,.2f FCFA", venteActuelle.getMontantRecu()),
                        normalFont);
                recu.setAlignment(Element.ALIGN_RIGHT);
                document.add(recu);

                Paragraph monnaie = new Paragraph(
                        "Monnaie : " + String.format("%,.2f FCFA", venteActuelle.getMonnaieRendue()), normalFont);
                monnaie.setAlignment(Element.ALIGN_RIGHT);
                document.add(monnaie);

                document.add(new Paragraph("\nMerci de votre visite !", normalFont));

                document.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Reçu généré avec succès.");
                alert.showAndWait();
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la génération du PDF.");
                alert.showAndWait();
            }
        }
    }

    private void fermerFenetre() {
        Stage stage = (Stage) btnFermerRecu.getScene().getWindow();
        stage.close();
    }
}
