package com.gestionargent.model;

/**
 * Type d'une transaction ou d'une catégorie : revenu ou dépense.
 */
public enum TypeTransaction {
    REVENU("Revenu"),
    DEPENSE("Dépense");

    private final String libelle;

    TypeTransaction(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
