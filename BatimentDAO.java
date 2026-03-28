package com.univ.dao;

import com.univ.model.Batiment;
import com.univ.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BatimentDAO {

    public boolean create(Batiment batiment) {
        String sql = "INSERT INTO batiment (nom, localisation, nombre_etages) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, batiment.getNom());
            stmt.setString(2, batiment.getLocalisation());
            stmt.setInt(3, batiment.getNombreEtages());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) batiment.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Batiment findById(int id) {
        String sql = "SELECT * FROM batiment WHERE id = ?";
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

    public List<Batiment> findAll() {
        List<Batiment> list = new ArrayList<>();
        String sql = "SELECT * FROM batiment";
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

    public boolean update(Batiment batiment) {
        String sql = "UPDATE batiment SET nom = ?, localisation = ?, nombre_etages = ? WHERE id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setString(1, batiment.getNom());
            stmt.setString(2, batiment.getLocalisation());
            stmt.setInt(3, batiment.getNombreEtages());
            stmt.setInt(4, batiment.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM batiment WHERE id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Batiment map(ResultSet rs) throws SQLException {
        Batiment b = new Batiment();
        b.setId(rs.getInt("id"));
        b.setNom(rs.getString("nom"));
        b.setLocalisation(rs.getString("localisation"));
        b.setNombreEtages(rs.getInt("nombre_etages"));
        return b;
    }
}