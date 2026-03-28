package com.univ.service;

import com.univ.model.Reservation;
import com.univ.dao.ReservationDAO;

import java.util.ArrayList;
import java.util.List;

public class ConflitDetector {
    private ReservationDAO reservationDAO = new ReservationDAO();

    public List<String> verifierReservation(Reservation reservation) {
        List<String> conflits = new ArrayList<>();

        // Conflit de salle
        List<Reservation> autres = reservationDAO.findBySalleAndDate(
                reservation.getSalle().getId(), reservation.getDate());
        for (Reservation r : autres) {
            if (r.getId() == reservation.getId()) continue; // même réservation (cas update)
            if (r.getStatut() == Reservation.Statut.ANNULE) continue;
            if (reservation.chevauche(r)) {
                conflits.add("La salle est déjà occupée de " + r.getHeureDebut() + " à " + r.getHeureFin());
                break;
            }
        }

        // Conflit enseignant
        if (reservation.getCours() != null && reservation.getCours().getEnseignant() != null) {
            int enseignantId = reservation.getCours().getEnseignant().getId();
            List<Reservation> reservationsEnseignant = reservationDAO.findByEnseignantAndDate(enseignantId, reservation.getDate());
            for (Reservation r : reservationsEnseignant) {
                if (r.getId() == reservation.getId()) continue;
                if (r.getStatut() == Reservation.Statut.ANNULE) continue;
                if (reservation.chevauche(r)) {
                    conflits.add("L'enseignant a déjà un cours de " + r.getHeureDebut() + " à " + r.getHeureFin());
                    break;
                }
            }
        }

        // Vérification capacité (si cours)
        if (reservation.getCours() != null) {
            // On pourrait estimer le nombre d'étudiants
            int capacite = reservation.getSalle().getCapacite();
            // Par défaut, on considère que la capacité est suffisante
            // mais on pourrait ajouter une règle.
        }

        return conflits;
    }
}