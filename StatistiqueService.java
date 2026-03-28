package com.univ.service;

import com.univ.model.Reservation;
import com.univ.model.Salle;
import com.univ.dao.ReservationDAO;
import com.univ.dao.SalleDAO;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatistiqueService {
    private ReservationDAO reservationDAO = new ReservationDAO();
    private SalleDAO salleDAO = new SalleDAO();

    public Map<String, Object> genererStatistiques(LocalDate debut, LocalDate fin) {
        Map<String, Object> stats = new HashMap<>();
        List<Reservation> reservations = reservationDAO.findByPeriode(debut, fin);
        List<Salle> toutesSalles = salleDAO.findAll();

        // Taux d'occupation global
        double tauxOccupation = calculerTauxOccupation(reservations, toutesSalles, debut, fin);
        stats.put("tauxOccupationGlobal", tauxOccupation);

        // Occupation par salle
        Map<Salle, Long> occupationParSalle = reservations.stream()
                .filter(r -> r.getStatut() != Reservation.Statut.ANNULE)
                .collect(Collectors.groupingBy(Reservation::getSalle, Collectors.counting()));
        stats.put("occupationParSalle", occupationParSalle);

        // Répartition par type
        Map<Reservation.TypeReservation, Long> parType = reservations.stream()
                .filter(r -> r.getStatut() != Reservation.Statut.ANNULE)
                .collect(Collectors.groupingBy(Reservation::getType, Collectors.counting()));
        stats.put("parType", parType);

        return stats;
    }

    private double calculerTauxOccupation(List<Reservation> reservations, List<Salle> salles, LocalDate debut, LocalDate fin) {
        long nbJours = fin.toEpochDay() - debut.toEpochDay() + 1;
        long nbSalles = salles.size();
        // On considère 8 créneaux par jour (par exemple 8h-12h et 14h-18h, 4 créneaux de 2h)
        long creneauxPossibles = nbJours * nbSalles * 8;
        long creneauxOccupes = reservations.stream()
                .filter(r -> r.getStatut() != Reservation.Statut.ANNULE)
                .count();
        if (creneauxPossibles == 0) return 0;
        return (double) creneauxOccupes / creneauxPossibles * 100;
    }
}