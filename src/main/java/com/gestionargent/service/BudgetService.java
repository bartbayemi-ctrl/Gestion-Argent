package com.gestionargent.service;

import com.gestionargent.dao.BudgetDAO;
import com.gestionargent.dao.TransactionDAO;
import com.gestionargent.model.Budget;
import com.gestionargent.model.Categorie;

import java.util.List;

/**
 * Logique métier autour des budgets mensuels : calcul des montants dépensés
 * et détection des dépassements.
 */
public class BudgetService {

    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    /**
     * Retourne les budgets du mois avec le montant réellement dépensé calculé
     * à partir des transactions.
     */
    public List<Budget> listerAvecConsommation(int mois, int annee) {
        List<Budget> budgets = budgetDAO.trouverParMois(mois, annee);
        for (Budget b : budgets) {
            double depense = transactionDAO.sommeDepensesParCategorieEtMois(
                b.getCategorie().getId(), mois, annee);
            b.setMontantDepense(depense);
        }
        return budgets;
    }

    public List<Budget> listerDepasses(int mois, int annee) {
        return listerAvecConsommation(mois, annee).stream()
            .filter(Budget::isDepasse)
            .toList();
    }

    public Budget ajouter(Categorie categorie, int mois, int annee, double montantLimite) {
        if (montantLimite <= 0) {
            throw new IllegalArgumentException("Le montant du budget doit être supérieur à zéro.");
        }
        Budget b = new Budget(categorie, mois, annee, montantLimite);
        return budgetDAO.ajouter(b);
    }

    public void modifier(Budget budget) {
        if (budget.getMontantLimite() <= 0) {
            throw new IllegalArgumentException("Le montant du budget doit être supérieur à zéro.");
        }
        budgetDAO.modifier(budget);
    }

    public void supprimer(int id) {
        budgetDAO.supprimer(id);
    }
}
