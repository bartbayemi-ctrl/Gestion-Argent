package com.gestionargent.controller;

import com.gestionargent.model.Budget;
import com.gestionargent.model.Transaction;
import com.gestionargent.model.TypeTransaction;
import com.gestionargent.service.BudgetService;
import com.gestionargent.service.TransactionService;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    @FXML private Label labelPeriode;
    @FXML private Label labelSolde;
    @FXML private Label labelRevenus;
    @FXML private Label labelDepenses;
    @FXML private VBox zoneAlertes;
    @FXML private PieChart graphiqueRepartition;
    @FXML private VBox listeTransactionsRecentes;

    private final TransactionService transactionService = new TransactionService();
    private final BudgetService budgetService = new BudgetService();

    @FXML
    public void initialize() {
        LocalDate maintenant = LocalDate.now();
        int mois = maintenant.getMonthValue();
        int annee = maintenant.getYear();

        String nomMois = maintenant.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        labelPeriode.setText("Tableau de bord — "
            + nomMois.substring(0, 1).toUpperCase() + nomMois.substring(1) + " " + annee);

        afficherSoldeEtTotaux(mois, annee);
        afficherAlertesBudget(mois, annee);
        afficherGraphiqueRepartition(mois, annee);
        afficherTransactionsRecentes();
    }

    private void afficherSoldeEtTotaux(int mois, int annee) {
        double solde = transactionService.calculerSolde();
        double revenus = transactionService.totalRevenusDuMois(mois, annee);
        double depenses = transactionService.totalDepensesDuMois(mois, annee);

        labelSolde.setText(formaterMontant(solde));
        labelRevenus.setText(formaterMontant(revenus));
        labelDepenses.setText(formaterMontant(depenses));
    }

    private void afficherAlertesBudget(int mois, int annee) {
        zoneAlertes.getChildren().clear();
        List<Budget> depasses = budgetService.listerDepasses(mois, annee);
        for (Budget b : depasses) {
            double depassement = b.getMontantDepense() - b.getMontantLimite();
            Label alerte = new Label(String.format(Locale.FRANCE,
                "⚠ Budget \"%s\" dépassé de %.2f €",
                b.getCategorie().getNom(), depassement));
            alerte.getStyleClass().add("alerte-budget");
            zoneAlertes.getChildren().add(alerte);
        }
        zoneAlertes.setVisible(!depasses.isEmpty());
        zoneAlertes.setManaged(!depasses.isEmpty());
    }

    private void afficherGraphiqueRepartition(int mois, int annee) {
        List<Object[]> repartition = transactionService.depensesParCategorie(mois, annee);
        graphiqueRepartition.getData().clear();

        if (repartition.isEmpty()) {
            graphiqueRepartition.setVisible(false);
            return;
        }
        graphiqueRepartition.setVisible(true);

        for (Object[] ligne : repartition) {
            String nom = (String) ligne[0];
            double total = (double) ligne[2];
            PieChart.Data part = new PieChart.Data(nom, total);
            graphiqueRepartition.getData().add(part);
        }

        // Applique la couleur de chaque catégorie après le rendu du graphique
        for (int i = 0; i < repartition.size(); i++) {
            String couleur = (String) repartition.get(i)[1];
            PieChart.Data part = graphiqueRepartition.getData().get(i);
            if (part.getNode() != null) {
                part.getNode().setStyle("-fx-pie-color: " + couleur + ";");
            } else {
                part.nodeProperty().addListener((obs, ancien, nouveau) -> {
                    if (nouveau != null) nouveau.setStyle("-fx-pie-color: " + couleur + ";");
                });
            }
        }
    }

    private void afficherTransactionsRecentes() {
        listeTransactionsRecentes.getChildren().clear();
        List<Transaction> recentes = transactionService.listerRecentes(6);
        DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd MMM", Locale.FRENCH);

        if (recentes.isEmpty()) {
            Label vide = new Label("Aucune transaction pour le moment.");
            vide.getStyleClass().add("texte-secondaire");
            listeTransactionsRecentes.getChildren().add(vide);
            return;
        }

        for (Transaction t : recentes) {
            javafx.scene.layout.HBox ligne = new javafx.scene.layout.HBox(10);
            ligne.getStyleClass().add("ligne-transaction");

            Circle pastille = new Circle(5);
            try {
                pastille.setFill(Color.web(t.getCategorie().getCouleur()));
            } catch (Exception e) {
                pastille.setFill(Color.GRAY);
            }

            VBox details = new VBox(2);
            Label desc = new Label(t.getDescription());
            desc.getStyleClass().add("desc-transaction");
            Label meta = new Label(t.getCategorie().getNom() + " · " + t.getDate().format(formatDate));
            meta.getStyleClass().add("texte-secondaire");
            details.getChildren().addAll(desc, meta);

            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            Label montant = new Label(formaterMontantSigne(t));
            montant.getStyleClass().add(
                t.getType() == TypeTransaction.DEPENSE ? "montant-negatif" : "montant-positif");

            ligne.getChildren().addAll(pastille, details, spacer, montant);
            listeTransactionsRecentes.getChildren().add(ligne);
        }
    }

    private String formaterMontant(double montant) {
        return String.format(Locale.FRANCE, "%,.2f €", montant);
    }

    private String formaterMontantSigne(Transaction t) {
        String signe = t.getType() == TypeTransaction.DEPENSE ? "-" : "+";
        return signe + String.format(Locale.FRANCE, "%,.2f €", t.getMontant());
    }
}
