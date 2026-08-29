package com.gestionargent.controller;

import com.gestionargent.model.Categorie;
import com.gestionargent.model.Transaction;
import com.gestionargent.model.TypeTransaction;
import com.gestionargent.service.CategorieService;
import com.gestionargent.service.TransactionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

public class TransactionDialogController {

    @FXML private ToggleButton boutonDepense;
    @FXML private ToggleButton boutonRevenu;
    @FXML private TextField champDescription;
    @FXML private TextField champMontant;
    @FXML private DatePicker champDate;
    @FXML private ComboBox<Categorie> champCategorie;
    @FXML private Label labelErreur;

    private final TransactionService transactionService = new TransactionService();
    private final CategorieService categorieService = new CategorieService();
    private final ToggleGroup groupeType = new ToggleGroup();

    private Transaction transactionEnEdition;
    private boolean valide = false;

    @FXML
    public void initialize() {
        boutonDepense.setToggleGroup(groupeType);
        boutonRevenu.setToggleGroup(groupeType);
        boutonDepense.setSelected(true);

        groupeType.selectedToggleProperty().addListener((obs, ancien, nouveau) -> rafraichirCategories());
        champDate.setValue(LocalDate.now());
        rafraichirCategories();
    }

    /**
     * Prépare le formulaire : vide pour un ajout, pré-rempli pour une modification.
     */
    public void initialiser(Transaction transaction) {
        this.transactionEnEdition = transaction;
        if (transaction != null) {
            if (transaction.getType() == TypeTransaction.REVENU) {
                boutonRevenu.setSelected(true);
            } else {
                boutonDepense.setSelected(true);
            }
            rafraichirCategories();
            champDescription.setText(transaction.getDescription());
            champMontant.setText(String.valueOf(transaction.getMontant()));
            champDate.setValue(transaction.getDate());
            champCategorie.getSelectionModel().select(transaction.getCategorie());
        }
    }

    private void rafraichirCategories() {
        TypeTransaction type = boutonRevenu.isSelected() ? TypeTransaction.REVENU : TypeTransaction.DEPENSE;
        champCategorie.setItems(FXCollections.observableArrayList(categorieService.listerParType(type)));
        if (!champCategorie.getItems().isEmpty()) {
            champCategorie.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void enregistrer() {
        labelErreur.setText("");
        try {
            double montant = Double.parseDouble(champMontant.getText().trim().replace(",", "."));
            TypeTransaction type = boutonRevenu.isSelected() ? TypeTransaction.REVENU : TypeTransaction.DEPENSE;
            Categorie categorie = champCategorie.getSelectionModel().getSelectedItem();

            Transaction t = transactionEnEdition != null ? transactionEnEdition : new Transaction();
            t.setDescription(champDescription.getText().trim());
            t.setMontant(montant);
            t.setDate(champDate.getValue());
            t.setCategorie(categorie);
            t.setType(type);

            if (transactionEnEdition != null) {
                transactionService.modifier(t);
            } else {
                transactionService.ajouter(t);
            }

            valide = true;
            fermer();
        } catch (NumberFormatException e) {
            labelErreur.setText("Le montant doit être un nombre valide (ex : 25.50).");
        } catch (IllegalArgumentException e) {
            labelErreur.setText(e.getMessage());
        }
    }

    @FXML
    private void annuler() {
        valide = false;
        fermer();
    }

    private void fermer() {
        ((javafx.stage.Stage) champDescription.getScene().getWindow()).close();
    }

    public boolean aEteValide() {
        return valide;
    }
}
