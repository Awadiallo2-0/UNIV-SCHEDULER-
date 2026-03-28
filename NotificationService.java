package com.univ.service;

import com.univ.model.Reservation;
import com.univ.model.Salle;
import com.univ.model.Utilisateur;

import java.time.format.DateTimeFormatter;

public class NotificationService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public void notifierNouvelleReservation(Reservation reservation) {
        System.out.println("\n📧 [NOTIFICATION] Nouvelle réservation créée :");
        System.out.println(formaterReservation(reservation));
        if (reservation.getCours() != null && reservation.getCours().getEnseignant() != null) {
            System.out.println("   Envoyé à : " + reservation.getCours().getEnseignant().getEmail());
        }
    }

    public void notifierAnnulationReservation(Reservation reservation) {
        System.out.println("\n📧 [NOTIFICATION] Réservation annulée :");
        System.out.println(formaterReservation(reservation));
        if (reservation.getCours() != null && reservation.getCours().getEnseignant() != null) {
            System.out.println("   Envoyé à : " + reservation.getCours().getEnseignant().getEmail());
        }
    }

    public void notifierDisponibiliteSalle(Salle salle, java.time.LocalDate date,
                                           java.time.LocalTime heureDebut,
                                           java.time.LocalTime heureFin) {
        System.out.println("\n📧 [NOTIFICATION] Salle disponible : " + salle.getNumero() +
                " le " + date.format(DATE_FORMATTER) + " de " +
                heureDebut.format(TIME_FORMATTER) + " à " + heureFin.format(TIME_FORMATTER));
    }

    private String formaterReservation(Reservation reservation) {
        StringBuilder sb = new StringBuilder();
        if (reservation.getCours() != null) {
            sb.append("Cours : ").append(reservation.getCours().getNom()).append("\n");
            if (reservation.getCours().getEnseignant() != null) {
                sb.append("Enseignant : ").append(reservation.getCours().getEnseignant().getPrenom())
                  .append(" ").append(reservation.getCours().getEnseignant().getNom()).append("\n");
            }
        } else {
            sb.append("Type : ").append(reservation.getType()).append("\n");
        }
        if (reservation.getSalle() != null) {
            sb.append("Salle : ").append(reservation.getSalle().getNumero()).append("\n");
        }
        sb.append("Date : ").append(reservation.getDate().format(DATE_FORMATTER)).append("\n");
        sb.append("Horaire : ").append(reservation.getHeureDebut().format(TIME_FORMATTER))
          .append(" - ").append(reservation.getHeureFin().format(TIME_FORMATTER)).append("\n");
        return sb.toString();
    }

	public void notifierModificationReservation(Reservation updatedReservation, String ancienneSalle,
			String ancienneDate, String ancienHeureDebut) {
		// TODO Auto-generated method stub
		
	}
}