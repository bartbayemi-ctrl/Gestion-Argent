package com.gestionargent.controller;

import com.gestionargent.model.Categorie;
import com.gestionargent.model.TypeTransaction;
import com.gestionargent.service.CategorieService;
import com.gestionargent.util.AlerteUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;

public class CategoriesController {

    @FXML private VBox listeDepenses;
    @FXML private VBox listeRevenus;
    @FXML private TextField champNom;
    @FXML private ComboBox<TypeTransaction> champType;
    @FXML private Label labelErreur;

    private final CategorieService categorieService = new CategorieService();

    private static final String[] PALETTE = {
        "#378ADD", "#1D9E75", "#D85A30", "#BA7517", "#7F77DD",
        "#D4537E", "#5DCAA5", "#97C459", "#E24B4A", "#888780"
    };
    private int indexCouleur = 0;

    @FXML
    public void initialize() {
        champType.getItems().setAll(TypeTransaction.values());
        champType.getSelectionModel().select(TypeTransaction.DEPENSE);
        rafraichir();
    }

    private void rafraichir() {
        listeDepenses.getChildren().clear();
        listeRevenus.getChildren().clear();

        List<Categorie> toutes = categorieService.listerToutes();
        for (Categorie c : toutes) {
            HBox ligne = creerLigneCategorie(c);
            if (c.getType() == TypeTransaction.DEPENSE) {
                listeDepenses.getChildren().add(ligne);
            } else {
                listeRevenus.getChildren().add(ligne);
            }
        }
    }

    private HBox creerLigneCategorie(Categorie c) {
        HBox ligne = new HBox(10);
        ligne.getStyleClass().add("ligne-categorie");
        ligne.setPadding(new Insets(8, 12, 8, 12));

        Circle pastille = new Circle(6);
        try {
            pastille.setFill(Color.web(c.getCouleur()));
        } catch (Exception e) {
            pastille.setFill(Color.GRAY);
        }

        Label nom = new Label(c.getNom());
        if (c.isParDefaut()) {
            nom.getStyleClass().add("texte-secondaire");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ligne.getChildren().addAll(pastille, nom, spacer);

        if (!c.isParDefaut()) {
            Button supprimer = new Button("✕");
            supprimer.getStyleClass().add("bouton-icone");
            supprimer.setOnAction(e -> {
                boolean confirme = AlerteUtil.confirmer("Confirmer",
                    "Supprimer la catégorie \"" + c.getNom() + "\" ?");
                if (confirme) {
                    try {
                        categorieService.supprimer(c);
                        rafraichir();
                    } catch (RuntimeException ex) {
                        AlerteUtil.erreur("Impossible de supprimer",
                            "Cette catégorie est utilisée par des transactions existantes.");
                    }
                }
            });
            ligne.getChildren().add(supprimer);
        } else {
            Label badge = new Label("Par défaut");
            badge.getStyleClass().add("badge-defaut");
            ligne.getChildren().add(badge);
        }

        return ligne;
    }

    @FXML
    private void ajouterCategorie() {
        labelErreur.setText("");
        String nom = champNom.getText();
        TypeTransaction type = champType.getSelectionModel().getSelectedItem();

        try {
            String couleur = PALETTE[indexCouleur % PALETTE.length];
            indexCouleur++;
            categorieService.ajouter(nom, type, couleur);
            champNom.clear();
            rafraichir();
        } catch (IllegalArgumentException e) {
            labelErreur.setText(e.getMessage());
        }
    }
}
