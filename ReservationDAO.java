package com.univ.dao;

import com.univ.model.Reservation;
import com.univ.model.Salle;
import com.univ.model.Cours;
import com.univ.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    private SalleDAO salleDAO = new SalleDAO();
    private CoursDAO coursDAO = new CoursDAO();

    public boolean create(Reservation reservation) {
        String sql = "INSERT INTO reservation (cours_id, salle_id, date, heure_debut, heure_fin, statut, type_reservation) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, reservation.getCours() != null ? reservation.getCours().getId() : null);
            stmt.setInt(2, reservation.getSalle().getId());
            stmt.setDate(3, java.sql.Date.valueOf(reservation.getDate()));
            stmt.setTime(4, java.sql.Time.valueOf(reservation.getHeureDebut()));
            stmt.setTime(5, java.sql.Time.valueOf(reservation.getHeureFin()));
            stmt.setString(6, reservation.getStatut().name());
            stmt.setString(7, reservation.getType().name());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) reservation.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Reservation findById(int id) {
        String sql = "SELECT * FROM reservation WHERE id = ?";
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

    public List<Reservation> findAll() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation";
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

    public List<Reservation> findBySalleAndDate(int salleId, LocalDate date) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation WHERE salle_id = ? AND date = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, salleId);
            stmt.setDate(2, java.sql.Date.valueOf(date));
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

    public List<Reservation> findByEnseignantAndDate(int enseignantId, LocalDate date) {
        // Jointure avec cours
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.* FROM reservation r JOIN cours c ON r.cours_id = c.id WHERE c.enseignant_id = ? AND r.date = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, enseignantId);
            stmt.setDate(2, java.sql.Date.valueOf(date));
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

    public List<Reservation> findByPeriode(LocalDate debut, LocalDate fin) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation WHERE date BETWEEN ? AND ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(debut));
            stmt.setDate(2, java.sql.Date.valueOf(fin));
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

    public List<Salle> findSallesDisponibles(LocalDate date, LocalTime heureDebut, LocalTime heureFin, Integer capaciteMin) {
        SalleDAO salleDAO = new SalleDAO();
        java.sql.Date sqlDate = java.sql.Date.valueOf(date);
        java.sql.Time sqlDebut = java.sql.Time.valueOf(heureDebut);
        java.sql.Time sqlFin = java.sql.Time.valueOf(heureFin);
        return salleDAO.findSallesDisponibles(sqlDate, sqlDebut, sqlFin, capaciteMin);
    }

    public boolean update(Reservation reservation) {
        String sql = "UPDATE reservation SET cours_id = ?, salle_id = ?, date = ?, heure_debut = ?, heure_fin = ?, statut = ?, type_reservation = ? WHERE id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setObject(1, reservation.getCours() != null ? reservation.getCours().getId() : null);
            stmt.setInt(2, reservation.getSalle().getId());
            stmt.setDate(3, java.sql.Date.valueOf(reservation.getDate()));
            stmt.setTime(4, java.sql.Time.valueOf(reservation.getHeureDebut()));
            stmt.setTime(5, java.sql.Time.valueOf(reservation.getHeureFin()));
            stmt.setString(6, reservation.getStatut().name());
            stmt.setString(7, reservation.getType().name());
            stmt.setInt(8, reservation.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Reservation map(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        int coursId = rs.getInt("cours_id");
        if (!rs.wasNull()) {
            r.setCours(coursDAO.findById(coursId));
        }
        int salleId = rs.getInt("salle_id");
        r.setSalle(salleDAO.findById(salleId));
        r.setDate(rs.getDate("date").toLocalDate());
        r.setHeureDebut(rs.getTime("heure_debut").toLocalTime());
        r.setHeureFin(rs.getTime("heure_fin").toLocalTime());
        r.setStatut(Reservation.Statut.valueOf(rs.getString("statut")));
        r.setType(Reservation.TypeReservation.valueOf(rs.getString("type_reservation")));
        return r;
    }
    public List<Reservation> findByDateAndHeure(LocalDate date, LocalTime heureDebut) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation WHERE date = ? AND heure_debut = ?";
        try (PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(date));
            stmt.setTime(2, java.sql.Time.valueOf(heureDebut));
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
}
