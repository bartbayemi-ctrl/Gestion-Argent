package com.gestionargent.controller;

import com.gestionargent.App;
import com.gestionargent.service.AuthService;
import com.gestionargent.util.AlerteUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import java.io.IOException;

public class LoginController {

    @FXML private Label labelTitre;
    @FXML private Label labelSousTitre;
    @FXML private PasswordField champMotDePasse;
    @FXML private PasswordField champConfirmation;
    @FXML private Label labelConfirmation;
    @FXML private Button boutonValider;
    @FXML private Label labelErreur;

    private final AuthService authService = new AuthService();
    private boolean modeCreation;

    @FXML
    public void initialize() {
        modeCreation = !authService.motDePasseDejaDefini();
        if (modeCreation) {
            labelTitre.setText("Bienvenue !");
            labelSousTitre.setText("Créez un mot de passe pour protéger vos données");
            boutonValider.setText("Créer le mot de passe");
            champConfirmation.setVisible(true);
            champConfirmation.setManaged(true);
            labelConfirmation.setVisible(true);
            labelConfirmation.setManaged(true);
        } else {
            labelTitre.setText("Gestion d'Argent");
            labelSousTitre.setText("Entrez votre mot de passe pour continuer");
            boutonValider.setText("Se connecter");
            champConfirmation.setVisible(false);
            champConfirmation.setManaged(false);
            labelConfirmation.setVisible(false);
            labelConfirmation.setManaged(false);
        }
        champMotDePasse.setOnAction(e -> valider());
        champConfirmation.setOnAction(e -> valider());
    }

    @FXML
    private void valider() {
        labelErreur.setText("");
        String motDePasse = champMotDePasse.getText();

        if (motDePasse == null || motDePasse.isBlank()) {
            labelErreur.setText("Veuillez saisir un mot de passe.");
            return;
        }

        if (modeCreation) {
            if (motDePasse.length() < 4) {
                labelErreur.setText("Le mot de passe doit contenir au moins 4 caractères.");
                return;
            }
            if (!motDePasse.equals(champConfirmation.getText())) {
                labelErreur.setText("Les mots de passe ne correspondent pas.");
                return;
            }
            authService.definirMotDePasse(motDePasse);
            ouvrirApplication();
        } else {
            if (authService.verifierMotDePasse(motDePasse)) {
                ouvrirApplication();
            } else {
                labelErreur.setText("Mot de passe incorrect.");
                champMotDePasse.clear();
            }
        }
    }

    private void ouvrirApplication() {
        try {
            App.changerScene("main.fxml", 1080, 700, true);
        } catch (IOException e) {
            AlerteUtil.erreur("Erreur", "Impossible de charger l'application : " + e.getMessage());
        }
    }
}
