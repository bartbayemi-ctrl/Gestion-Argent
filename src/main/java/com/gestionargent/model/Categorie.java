package com.gestionargent.model;

/**
 * Représente une catégorie de transaction (ex: Courses, Loyer, Salaire...).
 */
public class Categorie {

    private int id;
    private String nom;
    private TypeTransaction type;
    private boolean parDefaut;
    private String couleur; // code couleur hexadécimal pour les graphiques

    public Categorie() {
    }

    public Categorie(int id, String nom, TypeTransaction type, boolean parDefaut, String couleur) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.parDefaut = parDefaut;
        this.couleur = couleur;
    }

    public Categorie(String nom, TypeTransaction type, String couleur) {
        this(0, nom, type, false, couleur);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public TypeTransaction getType() {
        return type;
    }

    public void setType(TypeTransaction type) {
        this.type = type;
    }

    public boolean isParDefaut() {
        return parDefaut;
    }

    public void setParDefaut(boolean parDefaut) {
        this.parDefaut = parDefaut;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    @Override
    public String toString() {
        return nom; // pour affichage direct dans les ComboBox
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Categorie)) return false;
        return id == ((Categorie) o).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
