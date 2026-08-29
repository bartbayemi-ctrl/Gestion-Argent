package com.gestionargent.controller;

import com.gestionargent.model.Budget;
import com.gestionargent.model.Categorie;
import com.gestionargent.model.TypeTransaction;
import com.gestionargent.service.BudgetService;
import com.gestionargent.service.CategorieService;
import com.gestionargent.util.AlerteUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class BudgetsController {

    @FXML private ComboBox<String> filtreMois;
    @FXML private ComboBox<Integer> filtreAnnee;
    @FXML private VBox listeBudgets;
    @FXML private ComboBox<Categorie> champNouvelleCategorie;
    @FXML private TextField champNouveauMontant;
    @FXML private Label labelErreur;

    private final BudgetService budgetService = new BudgetService();
    private final CategorieService categorieService = new CategorieService();

    @FXML
    public void initialize() {
        configurerFiltres();
        champNouvelleCategorie.setItems(
            FXCollections.observableArrayList(categorieService.listerParType(TypeTransaction.DEPENSE)));
        rafraichir();
    }

    private void configurerFiltres() {
        filtreMois.setItems(FXCollections.observableArrayList(
            "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
            "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"));
        LocalDate maintenant = LocalDate.now();
        filtreMois.getSelectionModel().select(maintenant.getMonthValue() - 1);

        var annees = FXCollections.<Integer>observableArrayList();
        for (int a = maintenant.getYear() - 2; a <= maintenant.getYear() + 1; a++) {
            annees.add(a);
        }
        filtreAnnee.setItems(annees);
        filtreAnnee.getSelectionModel().select(Integer.valueOf(maintenant.getYear()));

        filtreMois.setOnAction(e -> rafraichir());
        filtreAnnee.setOnAction(e -> rafraichir());
    }

    private void rafraichir() {
        int mois = filtreMois.getSelectionModel().getSelectedIndex() + 1;
        Integer annee = filtreAnnee.getSelectionModel().getSelectedItem();
        if (annee == null) return;

        listeBudgets.getChildren().clear();
        List<Budget> budgets = budgetService.listerAvecConsommation(mois, annee);

        if (budgets.isEmpty()) {
            Label vide = new Label("Aucun budget défini pour cette période.");
            vide.getStyleClass().add("texte-secondaire");
            listeBudgets.getChildren().add(vide);
            return;
        }

        for (Budget b : budgets) {
            listeBudgets.getChildren().add(creerCarteBudget(b));
        }
    }

    private VBox creerCarteBudget(Budget b) {
        VBox carte = new VBox(6);
        carte.getStyleClass().add("carte-budget");
        carte.setPadding(new Insets(12));

        HBox entete = new HBox();
        Label nom = new Label(b.getCategorie().getNom());
        nom.getStyleClass().add("nom-budget");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label montants = new Label(String.format(Locale.FRANCE, "%,.2f € / %,.2f €",
            b.getMontantDepense(), b.getMontantLimite()));
        montants.getStyleClass().add(b.isDepasse() ? "montant-negatif" : "texte-secondaire");

        Button supprimer = new Button("✕");
        supprimer.getStyleClass().add("bouton-icone");
        supprimer.setOnAction(e -> {
            boolean confirme = AlerteUtil.confirmer("Confirmer", "Supprimer ce budget ?");
            if (confirme) {
                budgetService.supprimer(b.getId());
                rafraichir();
            }
        });

        entete.getChildren().addAll(nom, spacer, montants, supprimer);

        ProgressBar barre = new ProgressBar(Math.min(b.getPourcentageUtilise() / 100.0, 1.0));
        barre.setMaxWidth(Double.MAX_VALUE);
        barre.getStyleClass().add(b.isDepasse() ? "barre-depassee" : "barre-budget");

        carte.getChildren().addAll(entete, barre);

        if (b.isDepasse()) {
            Label alerte = new Label(String.format(Locale.FRANCE,
                "⚠ Dépassement de %,.2f €", b.getMontantDepense() - b.getMontantLimite()));
            alerte.getStyleClass().add("alerte-budget");
            carte.getChildren().add(alerte);
        }

        return carte;
    }

    @FXML
    private void ajouterBudget() {
        labelErreur.setText("");
        Categorie categorie = champNouvelleCategorie.getSelectionModel().getSelectedItem();
        Integer annee = filtreAnnee.getSelectionModel().getSelectedItem();
        int mois = filtreMois.getSelectionModel().getSelectedIndex() + 1;

        if (categorie == null) {
            labelErreur.setText("Veuillez choisir une catégorie.");
            return;
        }
        try {
            double montant = Double.parseDouble(champNouveauMontant.getText().trim().replace(",", "."));
            budgetService.ajouter(categorie, mois, annee, montant);
            champNouveauMontant.clear();
            rafraichir();
        } catch (NumberFormatException e) {
            labelErreur.setText("Le montant doit être un nombre valide.");
        } catch (IllegalArgumentException e) {
            labelErreur.setText(e.getMessage() != null && e.getMessage().contains("existe")
                ? "Un budget existe déjà pour cette catégorie ce mois-ci."
                : e.getMessage());
        } catch (RuntimeException e) {
            labelErreur.setText("Un budget existe déjà pour cette catégorie ce mois-ci.");
        }
    }
}
