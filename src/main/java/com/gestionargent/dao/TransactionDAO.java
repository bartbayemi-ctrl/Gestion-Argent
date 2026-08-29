package com.gestionargent.dao;

import com.gestionargent.model.Categorie;
import com.gestionargent.model.Transaction;
import com.gestionargent.model.TypeTransaction;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Accès aux données pour la table transaction_arg.
 */
public class TransactionDAO {

    private final CategorieDAO categorieDAO = new CategorieDAO();

    public List<Transaction> trouverToutes() {
        String sql = "SELECT * FROM transaction_arg ORDER BY date DESC, id DESC";
        return executerRequete(sql, ps -> {});
    }

    public List<Transaction> trouverParMois(int mois, int annee) {
        String sql = "SELECT * FROM transaction_arg WHERE strftime('%m', date) = ? " +
                     "AND strftime('%Y', date) = ? ORDER BY date DESC, id DESC";
        String moisStr = String.format("%02d", mois);
        String anneeStr = String.valueOf(annee);
        return executerRequete(sql, ps -> {
            ps.setString(1, moisStr);
            ps.setString(2, anneeStr);
        });
    }

    public List<Transaction> trouverRecentes(int limite) {
        String sql = "SELECT * FROM transaction_arg ORDER BY date DESC, id DESC LIMIT ?";
        return executerRequete(sql, ps -> ps.setInt(1, limite));
    }

    public Transaction ajouter(Transaction t) {
        String sql = "INSERT INTO transaction_arg (description, montant, date, categorie_id, type) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            remplirParametres(ps, t);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) t.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de la transaction", e);
        }
        return t;
    }

    public void modifier(Transaction t) {
        String sql = "UPDATE transaction_arg SET description = ?, montant = ?, date = ?, " +
                     "categorie_id = ?, type = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            remplirParametres(ps, t);
            ps.setInt(6, t.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification de la transaction", e);
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM transaction_arg WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la transaction", e);
        }
    }

    public double calculerSolde() {
        String sql = "SELECT " +
                "COALESCE(SUM(CASE WHEN type = 'REVENU' THEN montant ELSE -montant END), 0) AS solde " +
                "FROM transaction_arg";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("solde");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du calcul du solde", e);
        }
        return 0;
    }

    /**
     * Somme des transactions d'un type donné pour un mois/année donnés.
     */
    public double sommeParTypeEtMois(TypeTransaction type, int mois, int annee) {
        String sql = "SELECT COALESCE(SUM(montant), 0) AS total FROM transaction_arg " +
                     "WHERE type = ? AND strftime('%m', date) = ? AND strftime('%Y', date) = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, type.name());
            ps.setString(2, String.format("%02d", mois));
            ps.setString(3, String.valueOf(annee));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du calcul de la somme", e);
        }
        return 0;
    }

    /**
     * Somme des dépenses par catégorie pour un mois/année donnés
     * (nom de catégorie -> montant total).
     */
    public List<Object[]> depensesParCategorie(int mois, int annee) {
        String sql = "SELECT c.nom AS nom, c.couleur AS couleur, SUM(t.montant) AS total " +
                "FROM transaction_arg t JOIN categorie c ON t.categorie_id = c.id " +
                "WHERE t.type = 'DEPENSE' AND strftime('%m', t.date) = ? AND strftime('%Y', t.date) = ? " +
                "GROUP BY c.id ORDER BY total DESC";
        List<Object[]> resultat = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, String.format("%02d", mois));
            ps.setString(2, String.valueOf(annee));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultat.add(new Object[]{rs.getString("nom"), rs.getString("couleur"), rs.getDouble("total")});
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du calcul des dépenses par catégorie", e);
        }
        return resultat;
    }

    public double sommeDepensesParCategorieEtMois(int categorieId, int mois, int annee) {
        String sql = "SELECT COALESCE(SUM(montant), 0) AS total FROM transaction_arg " +
                "WHERE type = 'DEPENSE' AND categorie_id = ? AND strftime('%m', date) = ? " +
                "AND strftime('%Y', date) = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, categorieId);
            ps.setString(2, String.format("%02d", mois));
            ps.setString(3, String.valueOf(annee));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du calcul des dépenses de la catégorie", e);
        }
        return 0;
    }

    // ---- utilitaires internes ----

    private interface ParamSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    private List<Transaction> executerRequete(String sql, ParamSetter setter) {
        List<Transaction> resultat = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultat.add(mapper(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des transactions", e);
        }
        return resultat;
    }

    private void remplirParametres(PreparedStatement ps, Transaction t) throws SQLException {
        ps.setString(1, t.getDescription());
        ps.setDouble(2, t.getMontant());
        ps.setString(3, t.getDate().toString());
        ps.setInt(4, t.getCategorie().getId());
        ps.setString(5, t.getType().name());
    }

    private Transaction mapper(ResultSet rs) throws SQLException {
        Categorie cat = categorieDAO.trouverParId(rs.getInt("categorie_id"));
        return new Transaction(
            rs.getInt("id"),
            rs.getString("description"),
            rs.getDouble("montant"),
            LocalDate.parse(rs.getString("date")),
            cat,
            TypeTransaction.valueOf(rs.getString("type"))
        );
    }
}
