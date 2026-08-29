package com.gestionargent;

import com.gestionargent.dao.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Point d'entrée de l'application de gestion d'argent.
 */
public class App extends Application {

    private static Stage stagePrincipal;

    @Override
    public void start(Stage stage) throws IOException {
        stagePrincipal = stage;

        // Initialise la base SQLite locale (création des tables si besoin)
        DatabaseManager.initialiser();

        Parent racine = charger("login.fxml");
        Scene scene = new Scene(racine, 420, 480);
        scene.getStylesheets().add(getRessource("/com/gestionargent/css/style.css").toExternalForm());

        stage.setTitle("Gestion d'Argent");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Charge une vue FXML et remplace le contenu de la fenêtre principale.
     * Utilisé pour la navigation entre l'écran de connexion et l'écran principal.
     */
    public static void changerScene(String fxml, double largeur, double hauteur, boolean redimensionnable) throws IOException {
        Parent racine = charger(fxml);
        Scene scene = new Scene(racine, largeur, hauteur);
        scene.getStylesheets().add(getRessource("/com/gestionargent/css/style.css").toExternalForm());
        stagePrincipal.setScene(scene);
        stagePrincipal.setResizable(redimensionnable);
        stagePrincipal.centerOnScreen();
    }

    public static Parent charger(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(getRessource("/com/gestionargent/view/" + fxml));
        return loader.load();
    }

    public static FXMLLoader creerLoader(String fxml) {
        return new FXMLLoader(getRessource("/com/gestionargent/view/" + fxml));
    }

    private static URL getRessource(String chemin) {
        URL url = App.class.getResource(chemin);
        if (url == null) {
            throw new RuntimeException("Ressource introuvable : " + chemin);
        }
        return url;
    }

    public static Stage getStagePrincipal() {
        return stagePrincipal;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
