package com.gestionargent.model;

/**
 * Représente un budget mensuel alloué à une catégorie de dépense.
 */
public class Budget {

    private int id;
    private Categorie categorie;
    private int mois;   // 1-12
    private int annee;
    private double montantLimite;
    private double montantDepense; // calculé, non stocké en base

    public Budget() {
    }

    public Budget(int id, Categorie categorie, int mois, int annee, double montantLimite) {
        this.id = id;
        this.categorie = categorie;
        this.mois = mois;
        this.annee = annee;
        this.montantLimite = montantLimite;
    }

    public Budget(Categorie categorie, int mois, int annee, double montantLimite) {
        this(0, categorie, mois, annee, montantLimite);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public int getMois() {
        return mois;
    }

    public void setMois(int mois) {
        this.mois = mois;
    }

    public int getAnnee() {
        return annee;
    }

    public void setAnnee(int annee) {
        this.annee = annee;
    }

    public double getMontantLimite() {
        return montantLimite;
    }

    public void setMontantLimite(double montantLimite) {
        this.montantLimite = montantLimite;
    }

    public double getMontantDepense() {
        return montantDepense;
    }

    public void setMontantDepense(double montantDepense) {
        this.montantDepense = montantDepense;
    }

    public double getPourcentageUtilise() {
        if (montantLimite <= 0) return 0;
        return (montantDepense / montantLimite) * 100.0;
    }

    public boolean isDepasse() {
        return montantDepense > montantLimite;
    }
}
