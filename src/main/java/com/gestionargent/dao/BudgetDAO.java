package com.gestionargent.dao;

import com.gestionargent.model.Budget;
import com.gestionargent.model.Categorie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Accès aux données pour la table budget.
 */
public class BudgetDAO {

    private final CategorieDAO categorieDAO = new CategorieDAO();

    public List<Budget> trouverParMois(int mois, int annee) {
        String sql = "SELECT * FROM budget WHERE mois = ? AND annee = ? ORDER BY id";
        List<Budget> resultat = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, mois);
            ps.setInt(2, annee);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultat.add(mapper(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des budgets", e);
        }
        return resultat;
    }

    public Budget ajouter(Budget b) {
        String sql = "INSERT INTO budget (categorie_id, mois, annee, montant_limite) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getCategorie().getId());
            ps.setInt(2, b.getMois());
            ps.setInt(3, b.getAnnee());
            ps.setDouble(4, b.getMontantLimite());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) b.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Erreur lors de l'ajout du budget (un budget existe peut-être déjà pour ce mois)", e);
        }
        return b;
    }

    public void modifier(Budget b) {
        String sql = "UPDATE budget SET montant_limite = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, b.getMontantLimite());
            ps.setInt(2, b.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification du budget", e);
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM budget WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du budget", e);
        }
    }

    private Budget mapper(ResultSet rs) throws SQLException {
        Categorie cat = categorieDAO.trouverParId(rs.getInt("categorie_id"));
        return new Budget(
            rs.getInt("id"),
            cat,
            rs.getInt("mois"),
            rs.getInt("annee"),
            rs.getDouble("montant_limite")
        );
    }
}
