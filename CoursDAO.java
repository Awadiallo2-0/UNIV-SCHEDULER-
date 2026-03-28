package com.univ.dao;

import com.univ.model.Cours;
import com.univ.model.Utilisateur;
import com.univ.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursDAO {

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    public boolean create(Cours cours) {
        String sql = "INSERT INTO cours (nom, matiere, enseignant_id, classe, groupe) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, cours.getNom());
            stmt.setString(2, cours.getMatiere());
            stmt.setInt(3, cours.getEnseignant() != null ? cours.getEnseignant().getId() : null);
            stmt.setString(4, cours.getClasse());
            stmt.setString(5, cours.getGroupe());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) cours.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Cours findById(int id) {
        String sql = "SELECT * FROM cours WHERE id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Cours> findAll() {
        List<Cours> list = new ArrayList<>();
        String sql = "SELECT * FROM cours";
        try (Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean update(Cours cours) {
        String sql = "UPDATE cours SET nom = ?, matiere = ?, enseignant_id = ?, classe = ?, groupe = ? WHERE id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setString(1, cours.getNom());
            stmt.setString(2, cours.getMatiere());
            stmt.setInt(3, cours.getEnseignant() != null ? cours.getEnseignant().getId() : null);
            stmt.setString(4, cours.getClasse());
            stmt.setString(5, cours.getGroupe());
            stmt.setInt(6, cours.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM cours WHERE id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Cours map(ResultSet rs) throws SQLException {
        Cours c = new Cours();
        c.setId(rs.getInt("id"));
        c.setNom(rs.getString("nom"));
        c.setMatiere(rs.getString("matiere"));
        int enseignantId = rs.getInt("enseignant_id");
        if (!rs.wasNull()) {
            c.setEnseignant(utilisateurDAO.findById(enseignantId));
        }
        c.setClasse(rs.getString("classe"));
        c.setGroupe(rs.getString("groupe"));
        return c;
    }
}