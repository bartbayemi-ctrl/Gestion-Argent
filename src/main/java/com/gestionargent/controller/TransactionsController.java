package com.gestionargent.controller;

import com.gestionargent.model.Transaction;
import com.gestionargent.model.TypeTransaction;
import com.gestionargent.service.ExportService;
import com.gestionargent.service.TransactionService;
import com.gestionargent.util.AlerteUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class TransactionsController {

    @FXML private ComboBox<String> filtreMois;
    @FXML private ComboBox<Integer> filtreAnnee;
    @FXML private TableView<Transaction> tableTransactions;
    @FXML private TableColumn<Transaction, String> colonneDate;
    @FXML private TableColumn<Transaction, String> colonneDescription;
    @FXML private TableColumn<Transaction, String> colonneCategorie;
    @FXML private TableColumn<Transaction, String> colonneType;
    @FXML private TableColumn<Transaction, String> colonneMontant;
    @FXML private Label labelTotal;

    private final TransactionService transactionService = new TransactionService();
    private final ExportService exportService = new ExportService();
    private final DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        configurerColonnes();
        configurerFiltres();
        rafraichir();
    }

    private void configurerColonnes() {
        colonneDate.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getDate().format(formatDate)));
        colonneDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colonneCategorie.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getCategorie().getNom()));
        colonneType.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getType().getLibelle()));
        colonneMontant.setCellValueFactory(data -> {
            Transaction t = data.getValue();
            String signe = t.getType() == TypeTransaction.DEPENSE ? "-" : "+";
            return new javafx.beans.property.SimpleStringProperty(
                signe + String.format(Locale.FRANCE, "%,.2f €", t.getMontant()));
        });
        colonneMontant.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll("montant-negatif", "montant-positif");
                } else {
                    setText(item);
                    getStyleClass().removeAll("montant-negatif", "montant-positif");
                    getStyleClass().add(item.startsWith("-") ? "montant-negatif" : "montant-positif");
                }
            }
        });
    }

    private void configurerFiltres() {
        filtreMois.setItems(FXCollections.observableArrayList(
            "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
            "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"));
        LocalDate maintenant = LocalDate.now();
        filtreMois.getSelectionModel().select(maintenant.getMonthValue() - 1);

        ObservableList<Integer> annees = FXCollections.observableArrayList();
        for (int a = maintenant.getYear() - 5; a <= maintenant.getYear() + 1; a++) {
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

        List<Transaction> transactions = transactionService.listerParMois(mois, annee);
        tableTransactions.setItems(FXCollections.observableArrayList(transactions));

        double total = transactions.stream().mapToDouble(Transaction::getMontantSigne).sum();
        labelTotal.setText(String.format(Locale.FRANCE, "Total : %,.2f €", total));
    }

    @FXML
    private void ouvrirDialogueAjout() {
        ouvrirDialogueTransaction(null);
    }

    @FXML
    private void modifierTransactionSelectionnee() {
        Transaction selection = tableTransactions.getSelectionModel().getSelectedItem();
        if (selection == null) {
            AlerteUtil.avertissement("Aucune sélection", "Veuillez sélectionner une transaction à modifier.");
            return;
        }
        ouvrirDialogueTransaction(selection);
    }

    @FXML
    private void supprimerTransactionSelectionnee() {
        Transaction selection = tableTransactions.getSelectionModel().getSelectedItem();
        if (selection == null) {
            AlerteUtil.avertissement("Aucune sélection", "Veuillez sélectionner une transaction à supprimer.");
            return;
        }
        boolean confirme = AlerteUtil.confirmer("Confirmer la suppression",
            "Voulez-vous vraiment supprimer cette transaction ?");
        if (confirme) {
            transactionService.supprimer(selection.getId());
            rafraichir();
        }
    }

    private void ouvrirDialogueTransaction(Transaction transactionAModifier) {
        try {
            javafx.fxml.FXMLLoader loader = com.gestionargent.App.creerLoader("transaction_dialog.fxml");
            javafx.scene.Parent racine = loader.load();
            TransactionDialogController controleur = loader.getController();
            controleur.initialiser(transactionAModifier);

            javafx.stage.Stage dialogue = new javafx.stage.Stage();
            dialogue.setTitle(transactionAModifier == null ? "Nouvelle transaction" : "Modifier la transaction");
            dialogue.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            javafx.scene.Scene scene = new javafx.scene.Scene(racine);
            scene.getStylesheets().add(getClass().getResource("/com/gestionargent/css/style.css").toExternalForm());
            dialogue.setScene(scene);
            dialogue.setResizable(false);
            dialogue.showAndWait();

            if (controleur.aEteValide()) {
                rafraichir();
            }
        } catch (IOException e) {
            AlerteUtil.erreur("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    @FXML
    private void exporterPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter en PDF");
        chooser.setInitialFileName("transactions.pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf"));
        File fichier = chooser.showSaveDialog(tableTransactions.getScene().getWindow());
        if (fichier == null) return;

        try {
            exportService.exporterPdf(tableTransactions.getItems(), "Relevé de transactions", fichier);
            AlerteUtil.information("Export réussi", "Le fichier PDF a été généré avec succès.");
        } catch (IOException e) {
            AlerteUtil.erreur("Erreur d'export", "Impossible de générer le PDF : " + e.getMessage());
        }
    }

    @FXML
    private void exporterExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter en Excel");
        chooser.setInitialFileName("transactions.xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier Excel", "*.xlsx"));
        File fichier = chooser.showSaveDialog(tableTransactions.getScene().getWindow());
        if (fichier == null) return;

        try {
            exportService.exporterExcel(tableTransactions.getItems(), fichier);
            AlerteUtil.information("Export réussi", "Le fichier Excel a été généré avec succès.");
        } catch (IOException e) {
            AlerteUtil.erreur("Erreur d'export", "Impossible de générer le fichier Excel : " + e.getMessage());
        }
    }
}
