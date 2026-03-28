package com.univ.dao;

import com.univ.model.Salle;
import com.univ.model.Batiment;
import com.univ.model.Equipement;
import com.univ.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalleDAO {

    private BatimentDAO batimentDAO = new BatimentDAO();
    private EquipementDAO equipementDAO = new EquipementDAO();

    public boolean create(Salle salle) {
        String sql = "INSERT INTO salle (numero, capacite, type, batiment_id, etage) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, salle.getNumero());
            stmt.setInt(2, salle.getCapacite());
            stmt.setString(3, salle.getType().name());
            stmt.setInt(4, salle.getBatiment().getId());
            stmt.setInt(5, salle.getEtage());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) salle.setId(rs.getInt(1));
                }
                // Ajouter les équipements
                ajouterEquipements(salle);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void ajouterEquipements(Salle salle) throws SQLException {
        String sql = "INSERT INTO salle_equipement (salle_id, equipement_id, quantite) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            for (var entry : salle.getEquipements().entrySet()) {
                stmt.setInt(1, salle.getId());
                stmt.setInt(2, entry.getKey().getId());
                stmt.setInt(3, entry.getValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public Salle findById(int id) {
        String sql = "SELECT * FROM salle WHERE id = ?";
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

    public List<Salle> findAll() {
        List<Salle> list = new ArrayList<>();
        String sql = "SELECT * FROM salle";
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

    public boolean update(Salle salle) {
        String sql = "UPDATE salle SET numero = ?, capacite = ?, type = ?, batiment_id = ?, etage = ? WHERE id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setString(1, salle.getNumero());
            stmt.setInt(2, salle.getCapacite());
            stmt.setString(3, salle.getType().name());
            stmt.setInt(4, salle.getBatiment().getId());
            stmt.setInt(5, salle.getEtage());
            stmt.setInt(6, salle.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM salle WHERE id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Salle> rechercherParCriteres(Integer capaciteMin, String type, Integer batimentId) {
        List<Salle> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM salle WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (capaciteMin != null) {
            sql.append(" AND capacite >= ?");
            params.add(capaciteMin);
        }
        if (type != null && !type.isEmpty()) {
            sql.append(" AND type = ?");
            params.add(type);
        }
        if (batimentId != null) {
            sql.append(" AND batiment_id = ?");
            params.add(batimentId);
        }

        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Salle> findSallesDisponibles(Date date, Time heureDebut, Time heureFin, Integer capaciteMin) {
        List<Salle> result = new ArrayList<>();
        String sql = """
            SELECT s.* FROM salle s
            WHERE s.capacite >= ?
            AND s.id NOT IN (
                SELECT r.salle_id FROM reservation r
                WHERE r.date = ?
                AND r.statut != 'ANNULE'
                AND (
                    (r.heure_debut < ? AND r.heure_fin > ?) OR
                    (r.heure_debut < ? AND r.heure_fin > ?) OR
                    (r.heure_debut >= ? AND r.heure_fin <= ?)
                )
            )
        """;
        // Simplification : on considère qu'une réservation est conflictuelle si elle chevauche l'intervalle donné.
        // Ici, on test si r commence avant la fin et finit après le début.
        // Version plus simple : NOT (r.heure_fin <= ? OR r.heure_debut >= ?) avec ? = heureDebut et ? = heureFin
        // Pour éviter la complexité, on utilise la condition simplifiée suivante :
        // (r.heure_debut < ? AND r.heure_fin > ?) avec ? = heureFin et ? = heureDebut
        // Mais cela ne couvre pas tous les cas, donc utilisons la version complète :
        // (r.heure_debut < ? AND r.heure_fin > ?) OR (r.heure_debut < ? AND r.heure_fin > ?) OR ...
        // Pour simplifier, nous allons utiliser une seule condition de chevauchement correcte :
        // NOT (r.heure_fin <= ? OR r.heure_debut >= ?) avec ? = heureDebut et ? = heureFin
        // Donc : WHERE NOT (r.heure_fin <= ? AND r.heure_debut >= ?) est faux, mieux vaut utiliser :
        // NOT (r.heure_fin <= ? OR r.heure_debut >= ?) est la négation de "ne chevauche pas"
        // On veut les salles qui n'ont pas de réservation telle que r.heure_fin > ? ET r.heure_debut < ? (chevauchement)
        // Donc sous-requête avec NOT EXISTS serait plus clair, mais on va garder une approche simple :
        // On sélectionne les salles qui n'ont aucune réservation sur cette date avec chevauchement.
        // On utilise donc NOT EXISTS avec une condition de chevauchement.
        String sql2 = """
            SELECT s.* FROM salle s
            WHERE s.capacite >= ?
            AND NOT EXISTS (
                SELECT 1 FROM reservation r
                WHERE r.salle_id = s.id
                AND r.date = ?
                AND r.statut != 'ANNULE'
                AND r.heure_debut < ?  -- début de la réservation existante < fin de la nouvelle
                AND r.heure_fin > ?     -- fin de la réservation existante > début de la nouvelle
            )
        """;
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql2)) {
            stmt.setInt(1, capaciteMin != null ? capaciteMin : 0);
            stmt.setDate(2, date);
            stmt.setTime(3, heureFin);
            stmt.setTime(4, heureDebut);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Salle map(ResultSet rs) throws SQLException {
        Salle s = new Salle();
        s.setId(rs.getInt("id"));
        s.setNumero(rs.getString("numero"));
        s.setCapacite(rs.getInt("capacite"));
        s.setType(Salle.TypeSalle.valueOf(rs.getString("type")));
        s.setEtage(rs.getInt("etage"));
        Batiment b = batimentDAO.findById(rs.getInt("batiment_id"));
        s.setBatiment(b);
        // Charger les équipements
        chargerEquipements(s);
        return s;
    }

    private void chargerEquipements(Salle salle) {
        String sql = "SELECT e.*, se.quantite FROM equipement e " +
                "JOIN salle_equipement se ON e.id = se.equipement_id " +
                "WHERE se.salle_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, salle.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Equipement e = equipementDAO.findById(rs.getInt("id"));
                    if (e != null) {
                        salle.addEquipement(e, rs.getInt("quantite"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}