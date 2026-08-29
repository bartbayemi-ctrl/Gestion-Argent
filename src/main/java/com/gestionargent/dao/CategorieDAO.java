package com.gestionargent.dao;

import com.gestionargent.model.Categorie;
import com.gestionargent.model.TypeTransaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Accès aux données pour la table categorie.
 */
public class CategorieDAO {

    public List<Categorie> trouverToutes() {
        String sql = "SELECT * FROM categorie ORDER BY type, nom";
        List<Categorie> resultat = new ArrayList<>();
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                resultat.add(mapper(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des catégories", e);
        }
        return resultat;
    }

    public List<Categorie> trouverParType(TypeTransaction type) {
        String sql = "SELECT * FROM categorie WHERE type = ? ORDER BY nom";
        List<Categorie> resultat = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultat.add(mapper(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des catégories", e);
        }
        return resultat;
    }

    public Categorie trouverParId(int id) {
        String sql = "SELECT * FROM categorie WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapper(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de la catégorie", e);
        }
        return null;
    }

    public Categorie ajouter(Categorie categorie) {
        String sql = "INSERT INTO categorie (nom, type, par_defaut, couleur) VALUES (?, ?, 0, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, categorie.getNom());
            ps.setString(2, categorie.getType().name());
            ps.setString(3, categorie.getCouleur());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) categorie.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de la catégorie", e);
        }
        return categorie;
    }

    public void modifier(Categorie categorie) {
        String sql = "UPDATE categorie SET nom = ?, couleur = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, categorie.getNom());
            ps.setString(2, categorie.getCouleur());
            ps.setInt(3, categorie.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification de la catégorie", e);
        }
    }

    /**
     * Supprime une catégorie. Impossible si elle est utilisée par des
     * transactions existantes (contrainte FK RESTRICT) ou si elle est une
     * catégorie par défaut.
     */
    public void supprimer(int id) {
        String sql = "DELETE FROM categorie WHERE id = ? AND par_defaut = 0";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                "Impossible de supprimer cette catégorie : elle est utilisée par des transactions.", e);
        }
    }

    private Categorie mapper(ResultSet rs) throws SQLException {
        return new Categorie(
            rs.getInt("id"),
            rs.getString("nom"),
            TypeTransaction.valueOf(rs.getString("type")),
            rs.getInt("par_defaut") == 1,
            rs.getString("couleur")
        );
    }
}
