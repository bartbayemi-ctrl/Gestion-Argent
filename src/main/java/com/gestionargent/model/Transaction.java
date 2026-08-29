package com.gestionargent.model;

import java.time.LocalDate;

/**
 * Représente une transaction financière (dépense ou revenu).
 */
public class Transaction {

    private int id;
    private String description;
    private double montant; // toujours positif, le signe dépend du type
    private LocalDate date;
    private Categorie categorie;
    private TypeTransaction type;

    public Transaction() {
    }

    public Transaction(int id, String description, double montant, LocalDate date,
                        Categorie categorie, TypeTransaction type) {
        this.id = id;
        this.description = description;
        this.montant = montant;
        this.date = date;
        this.categorie = categorie;
        this.type = type;
    }

    public Transaction(String description, double montant, LocalDate date,
                        Categorie categorie, TypeTransaction type) {
        this(0, description, montant, date, categorie, type);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public TypeTransaction getType() {
        return type;
    }

    public void setType(TypeTransaction type) {
        this.type = type;
    }

    /**
     * Montant signé : positif pour un revenu, négatif pour une dépense.
     * Utile pour les calculs de solde.
     */
    public double getMontantSigne() {
        return type == TypeTransaction.DEPENSE ? -montant : montant;
    }
}
