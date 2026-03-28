package com.univ.service;

import com.univ.model.*;
import com.univ.dao.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class PlanningService {
    private ReservationDAO reservationDAO = new ReservationDAO();
    private SalleDAO salleDAO = new SalleDAO();
    private ConflitDetector conflitDetector = new ConflitDetector();

    public Reservation planifierCours(Cours cours, LocalDate date, LocalTime debut, LocalTime fin, Salle salle)
            throws Exception {
        Reservation r = new Reservation(salle, date, debut, fin);
        r.setCours(cours);
        r.setType(Reservation.TypeReservation.COURS);

        List<String> conflits = conflitDetector.verifierReservation(r);
        if (!conflits.isEmpty()) {
            throw new Exception("Conflits détectés : " + String.join(", ", conflits));
        }

        reservationDAO.create(r);
        return r;
    }

    public List<Salle> rechercherSallesDisponibles(LocalDate date, LocalTime debut, LocalTime fin, Integer capaciteMin) {
        return reservationDAO.findSallesDisponibles(date, debut, fin, capaciteMin);
    }

    public List<Reservation> getReservationsParPeriode(LocalDate debut, LocalDate fin) {
        return reservationDAO.findByPeriode(debut, fin);
    }
}