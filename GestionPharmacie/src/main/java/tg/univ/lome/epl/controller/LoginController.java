package tg.univ.lome.epl.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tg.univ.lome.epl.dao.UtilisateurDAO;
import tg.univ.lome.epl.model.Utilisateur;

import tg.univ.lome.epl.util.SessionManager;

public class LoginController implements Initializable {

    @FXML private TextField txtIdentifiant;
    @FXML private PasswordField txtMotDePasse;
    @FXML private Button btnConnexion;

    private UtilisateurDAO utilisateurDAO;

    public LoginController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            this.utilisateurDAO = new UtilisateurDAO();
        } catch (Exception e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur BDD", "Impossible d'initialiser la connexion avec la base de données.");
        }
    }

    @FXML
    private void handleConnexion(ActionEvent event) {
        if (utilisateurDAO == null) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur BDD", "La connexion à la base de données n'est pas disponible.");
            return;
        }

        String login = txtIdentifiant.getText() != null ? txtIdentifiant.getText().trim() : "";
        String pwd = txtMotDePasse.getText() != null ? txtMotDePasse.getText().trim() : "";

        if (login.isEmpty() || pwd.isEmpty()) {
            afficherAlerte(Alert.AlertType.WARNING, "Champs incomplets", "Veuillez remplir tous les champs.");
            return;
        }

        Optional<Utilisateur> user = utilisateurDAO.authenticate(login, pwd);
        if (user.isPresent()) {
            Utilisateur userConnecte = user.get();
            SessionManager.setUtilisateurConnecte(userConnecte);
            ouvrirTableauDeBord(event, userConnecte);
        } else {
            afficherAlerte(Alert.AlertType.ERROR, "Échec de connexion", "Nom d'utilisateur ou mot de passe incorrect.");
        }
    }

    private void ouvrirTableauDeBord(ActionEvent event, Utilisateur userConnecte) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Pharmacie - Tableau de Bord");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur de chargement", "Impossible d'ouvrir la vue du Tableau de Bord.");
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