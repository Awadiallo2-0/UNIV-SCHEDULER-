package com.univ.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utilitaires pour la manipulation des dates et heures avec Java 8 Time API.
 */
public class DateUtils {
    // Formateurs par défaut (modifiables selon les besoins)
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Convertit une chaîne au format dd/MM/yyyy en LocalDate.
     * @param dateStr la chaîne représentant la date
     * @return LocalDate correspondante ou null si le format est invalide
     */
    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Convertit une chaîne au format HH:mm en LocalTime.
     * @param timeStr la chaîne représentant l'heure
     * @return LocalTime correspondante ou null si le format est invalide
     */
    public static LocalTime parseTime(String timeStr) {
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Formate une LocalDate en chaîne selon le formateur par défaut.
     */
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    /**
     * Formate une LocalTime en chaîne selon le formateur par défaut.
     */
    public static String formatTime(LocalTime time) {
        return time.format(TIME_FORMATTER);
    }

    /**
     * Vérifie si deux intervalles horaires se chevauchent (les dates doivent être égales par ailleurs).
     * Utile pour la détection de conflits.
     */
    public static boolean chevauchement(LocalTime deb1, LocalTime fin1, LocalTime deb2, LocalTime fin2) {
        return deb1.isBefore(fin2) && deb2.isBefore(fin1);
    }
}