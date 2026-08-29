package com.gestionargent.service;

import com.gestionargent.dao.DatabaseManager;
import com.gestionargent.util.MotDePasseUtil;

import java.sql.*;

/**
 * Gère la création et la vérification du mot de passe/PIN de l'application.
 * Un seul utilisateur local (id = 1).
 */
public class AuthService {

    public boolean motDePasseDejaDefini() {
        String sql = "SELECT COUNT(*) AS n FROM utilisateur WHERE id = 1";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt("n") > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification du mot de passe", e);
        }
    }

    public void definirMotDePasse(String motDePasse) {
        String sel = MotDePasseUtil.genererSel();
        String hash = MotDePasseUtil.hacher(motDePasse, sel);
        String sql = "INSERT INTO utilisateur (id, mot_de_passe_hash, sel) VALUES (1, ?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, sel);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création du mot de passe", e);
        }
    }

    public boolean verifierMotDePasse(String motDePasseSaisi) {
        String sql = "SELECT mot_de_passe_hash, sel FROM utilisateur WHERE id = 1";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (!rs.next()) return false;
            String hash = rs.getString("mot_de_passe_hash");
            String sel = rs.getString("sel");
            return MotDePasseUtil.verifier(motDePasseSaisi, sel, hash);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification du mot de passe", e);
        }
    }

    public void changerMotDePasse(String nouveauMotDePasse) {
        String sel = MotDePasseUtil.genererSel();
        String hash = MotDePasseUtil.hacher(nouveauMotDePasse, sel);
        String sql = "UPDATE utilisateur SET mot_de_passe_hash = ?, sel = ? WHERE id = 1";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, sel);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du changement de mot de passe", e);
        }
    }
}
