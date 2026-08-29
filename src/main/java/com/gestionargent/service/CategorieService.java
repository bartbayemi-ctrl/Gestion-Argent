package com.gestionargent.service;

import com.gestionargent.dao.CategorieDAO;
import com.gestionargent.model.Categorie;
import com.gestionargent.model.TypeTransaction;

import java.util.List;

/**
 * Logique métier autour des catégories.
 */
public class CategorieService {

    private final CategorieDAO categorieDAO = new CategorieDAO();

    public List<Categorie> listerToutes() {
        return categorieDAO.trouverToutes();
    }

    public List<Categorie> listerParType(TypeTransaction type) {
        return categorieDAO.trouverParType(type);
    }

    public Categorie ajouter(String nom, TypeTransaction type, String couleur) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom de la catégorie est obligatoire.");
        }
        boolean doublon = categorieDAO.trouverToutes().stream()
            .anyMatch(c -> c.getNom().equalsIgnoreCase(nom.trim()) && c.getType() == type);
        if (doublon) {
            throw new IllegalArgumentException("Cette catégorie existe déjà.");
        }
        Categorie c = new Categorie(nom.trim(), type, couleur == null ? "#378ADD" : couleur);
        return categorieDAO.ajouter(c);
    }

    public void modifier(Categorie categorie) {
        if (categorie.getNom() == null || categorie.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom de la catégorie est obligatoire.");
        }
        categorieDAO.modifier(categorie);
    }

    public void supprimer(Categorie categorie) {
        if (categorie.isParDefaut()) {
            throw new IllegalArgumentException("Impossible de supprimer une catégorie par défaut.");
        }
        categorieDAO.supprimer(categorie.getId());
    }
}
