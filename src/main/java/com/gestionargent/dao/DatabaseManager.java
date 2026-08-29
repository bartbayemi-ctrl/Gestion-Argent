package com.gestionargent.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gère la connexion unique à la base SQLite (fichier .db local) et la
 * création du schéma au premier lancement.
 */
public final class DatabaseManager {

    private static final String DOSSIER_APP = System.getProperty("user.home")
            + File.separator + ".gestion-argent";
    private static final String CHEMIN_DB = DOSSIER_APP + File.separator + "gestion-argent.db";
    private static final String URL = "jdbc:sqlite:" + CHEMIN_DB;

    private static Connection connexion;

    private DatabaseManager() {
    }

    public static synchronized Connection getConnection() {
        try {
            if (connexion == null || connexion.isClosed()) {
                new File(DOSSIER_APP).mkdirs();
                Class.forName("org.sqlite.JDBC");
                connexion = DriverManager.getConnection(URL);
                try (Statement st = connexion.createStatement()) {
                    st.execute("PRAGMA foreign_keys = ON");
                }
            }
            return connexion;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Impossible de se connecter à la base de données", e);
        }
    }

    /**
     * Crée les tables si elles n'existent pas encore et insère les
     * catégories par défaut lors du tout premier lancement.
     */
    public static void initialiser() {
        Connection c = getConnection();
        try (Statement st = c.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS utilisateur (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    mot_de_passe_hash TEXT NOT NULL,
                    sel TEXT NOT NULL
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS categorie (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nom TEXT NOT NULL,
                    type TEXT NOT NULL CHECK (type IN ('REVENU','DEPENSE')),
                    par_defaut INTEGER NOT NULL DEFAULT 0,
                    couleur TEXT NOT NULL DEFAULT '#378ADD'
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS transaction_arg (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    description TEXT NOT NULL,
                    montant REAL NOT NULL,
                    date TEXT NOT NULL,
                    categorie_id INTEGER NOT NULL,
                    type TEXT NOT NULL CHECK (type IN ('REVENU','DEPENSE')),
                    FOREIGN KEY (categorie_id) REFERENCES categorie(id) ON DELETE RESTRICT
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS budget (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    categorie_id INTEGER NOT NULL,
                    mois INTEGER NOT NULL,
                    annee INTEGER NOT NULL,
                    montant_limite REAL NOT NULL,
                    FOREIGN KEY (categorie_id) REFERENCES categorie(id) ON DELETE CASCADE,
                    UNIQUE(categorie_id, mois, annee)
                )
            """);

            insererCategoriesParDefaut(c);

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'initialisation de la base", e);
        }
    }

    private static void insererCategoriesParDefaut(Connection c) throws SQLException {
        try (Statement check = c.createStatement()) {
            var rs = check.executeQuery("SELECT COUNT(*) AS n FROM categorie");
            rs.next();
            if (rs.getInt("n") > 0) {
                return; // déjà initialisé
            }
        }

        String sql = "INSERT INTO categorie (nom, type, par_defaut, couleur) VALUES (?, ?, 1, ?)";
        try (var ps = c.prepareStatement(sql)) {
            Object[][] categoriesDefaut = {
                {"Salaire", "REVENU", "#1D9E75"},
                {"Freelance", "REVENU", "#5DCAA5"},
                {"Autres revenus", "REVENU", "#97C459"},
                {"Logement", "DEPENSE", "#378ADD"},
                {"Courses", "DEPENSE", "#1D9E75"},
                {"Transport", "DEPENSE", "#BA7517"},
                {"Loisirs", "DEPENSE", "#D85A30"},
                {"Santé", "DEPENSE", "#D4537E"},
                {"Restaurants", "DEPENSE", "#7F77DD"},
                {"Abonnements", "DEPENSE", "#888780"},
                {"Autres dépenses", "DEPENSE", "#E24B4A"}
            };
            for (Object[] cat : categoriesDefaut) {
                ps.setString(1, (String) cat[0]);
                ps.setString(2, (String) cat[1]);
                ps.setString(3, (String) cat[2]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public static String getCheminBase() {
        return CHEMIN_DB;
    }
}
