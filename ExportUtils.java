package com.univ.utils;

import com.univ.model.Reservation;
import com.univ.model.Salle;
import com.univ.model.Cours;
import com.univ.model.Utilisateur;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utilitaires pour exporter des données dans des fichiers (CSV).
 * Ne nécessite aucune bibliothèque externe.
 */
public class ExportUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Exporte une liste de réservations vers un fichier CSV.
     * @param reservations la liste des réservations à exporter
     * @param cheminFichier le chemin complet du fichier de destination
     */
    public static void exporterReservationsVersCSV(List<Reservation> reservations, String cheminFichier) {
        // Vérification que la liste n'est pas null ou vide
        if (reservations == null || reservations.isEmpty()) {
            System.err.println("⚠️ Aucune réservation à exporter.");
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(cheminFichier))) {
            // En-tête du fichier CSV
            writer.println("Date;Début;Fin;Salle;Bâtiment;Cours;Enseignant;Type;Statut");

            // Parcours de toutes les réservations
            for (Reservation r : reservations) {
                StringBuilder ligne = new StringBuilder();
                
                // Date
                if (r.getDate() != null) {
                    ligne.append(r.getDate().format(DATE_FORMATTER));
                }
                ligne.append(";");
                
                // Heure début
                if (r.getHeureDebut() != null) {
                    ligne.append(r.getHeureDebut().format(TIME_FORMATTER));
                }
                ligne.append(";");
                
                // Heure fin
                if (r.getHeureFin() != null) {
                    ligne.append(r.getHeureFin().format(TIME_FORMATTER));
                }
                ligne.append(";");
                
                // Salle
                Salle salle = r.getSalle();
                if (salle != null) {
                    // Numéro de salle
                    if (salle.getNumero() != null) {
                        ligne.append(salle.getNumero());
                    }
                    ligne.append(";");
                    
                    // Bâtiment
                    if (salle.getBatiment() != null && salle.getBatiment().getNom() != null) {
                        ligne.append(salle.getBatiment().getNom());
                    }
                } else {
                    ligne.append(";");
                }
                ligne.append(";");
                
                // Cours
                Cours cours = r.getCours();
                if (cours != null) {
                    // Nom du cours
                    if (cours.getNom() != null) {
                        ligne.append(cours.getNom());
                    }
                    ligne.append(";");
                    
                    // Enseignant
                    if (cours.getEnseignant() != null) {
                        String prenom = cours.getEnseignant().getPrenom() != null ? 
                                      cours.getEnseignant().getPrenom() : "";
                        String nom = cours.getEnseignant().getNom() != null ? 
                                   cours.getEnseignant().getNom() : "";
                        String nomComplet = (prenom + " " + nom).trim();
                        if (!nomComplet.isEmpty()) {
                            ligne.append(nomComplet);
                        }
                    }
                } else {
                    ligne.append(";");
                }
                ligne.append(";");
                
                // Type
                if (r.getType() != null) {
                    ligne.append(r.getType().toString());
                }
                ligne.append(";");
                
                // Statut
                if (r.getStatut() != null) {
                    ligne.append(r.getStatut().toString());
                }
                
                // Écrire la ligne dans le fichier
                writer.println(ligne.toString());
            }
            
            System.out.println("✅ Fichier CSV créé avec succès : " + cheminFichier);
            
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la création du fichier CSV : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Version simplifiée pour exporter sur le Bureau avec un nom de fichier horodaté.
     * @param reservations la liste des réservations à exporter
     */
    public static void exporterReservationsVersCSV(List<Reservation> reservations) {
        String home = System.getProperty("user.home");
        String chemin = home + "/Desktop/reservations_" + 
                       System.currentTimeMillis() + ".csv";
        exporterReservationsVersCSV(reservations, chemin);
    }
}