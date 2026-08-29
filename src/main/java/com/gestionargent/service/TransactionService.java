package com.gestionargent.service;

import com.gestionargent.dao.TransactionDAO;
import com.gestionargent.model.Transaction;
import com.gestionargent.model.TypeTransaction;

import java.util.List;

/**
 * Logique métier autour des transactions (validation, calculs).
 */
public class TransactionService {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    public List<Transaction> listerToutes() {
        return transactionDAO.trouverToutes();
    }

    public List<Transaction> listerParMois(int mois, int annee) {
        return transactionDAO.trouverParMois(mois, annee);
    }

    public List<Transaction> listerRecentes(int limite) {
        return transactionDAO.trouverRecentes(limite);
    }

    public Transaction ajouter(Transaction t) {
        valider(t);
        return transactionDAO.ajouter(t);
    }

    public void modifier(Transaction t) {
        valider(t);
        transactionDAO.modifier(t);
    }

    public void supprimer(int id) {
        transactionDAO.supprimer(id);
    }

    public double calculerSolde() {
        return transactionDAO.calculerSolde();
    }

    public double totalRevenusDuMois(int mois, int annee) {
        return transactionDAO.sommeParTypeEtMois(TypeTransaction.REVENU, mois, annee);
    }

    public double totalDepensesDuMois(int mois, int annee) {
        return transactionDAO.sommeParTypeEtMois(TypeTransaction.DEPENSE, mois, annee);
    }

    public List<Object[]> depensesParCategorie(int mois, int annee) {
        return transactionDAO.depensesParCategorie(mois, annee);
    }

    private void valider(Transaction t) {
        if (t.getDescription() == null || t.getDescription().isBlank()) {
            throw new IllegalArgumentException("La description est obligatoire.");
        }
        if (t.getMontant() <= 0) {
            throw new IllegalArgumentException("Le montant doit être supérieur à zéro.");
        }
        if (t.getDate() == null) {
            throw new IllegalArgumentException("La date est obligatoire.");
        }
        if (t.getCategorie() == null) {
            throw new IllegalArgumentException("La catégorie est obligatoire.");
        }
        if (t.getType() == null) {
            throw new IllegalArgumentException("Le type (revenu/dépense) est obligatoire.");
        }
    }
}
