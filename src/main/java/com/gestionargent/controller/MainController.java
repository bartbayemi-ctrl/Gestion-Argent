package com.gestionargent.controller;

import com.gestionargent.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainController {

    @FXML private StackPane zoneContenu;
    @FXML private VBox barreNavigation;
    @FXML private ToggleButton boutonDashboard;
    @FXML private ToggleButton boutonTransactions;
    @FXML private ToggleButton boutonBudgets;
    @FXML private ToggleButton boutonCategories;
    @FXML private Label labelSolde;

    private final ToggleGroup groupeNavigation = new ToggleGroup();

    @FXML
    public void initialize() {
        boutonDashboard.setToggleGroup(groupeNavigation);
        boutonTransactions.setToggleGroup(groupeNavigation);
        boutonBudgets.setToggleGroup(groupeNavigation);
        boutonCategories.setToggleGroup(groupeNavigation);

        boutonDashboard.setSelected(true);
        afficherDashboard();
    }

    @FXML
    private void afficherDashboard() {
        chargerVue("dashboard.fxml");
    }

    @FXML
    private void afficherTransactions() {
        chargerVue("transactions.fxml");
    }

    @FXML
    private void afficherBudgets() {
        chargerVue("budgets.fxml");
    }

    @FXML
    private void afficherCategories() {
        chargerVue("categories.fxml");
    }

    private void chargerVue(String fxml) {
        try {
            FXMLLoader loader = App.creerLoader(fxml);
            Parent vue = loader.load();
            zoneContenu.getChildren().setAll(vue);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger la vue " + fxml, e);
        }
    }
}
