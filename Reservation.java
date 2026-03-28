package com.univ.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;

public class Reservation {
    public enum Statut { PLANIFIE, CONFIRME, ANNULE }
    public enum TypeReservation { COURS, REUNION, SOUTENANCE, ETUDE }

    private int id;
    private Cours cours;
    private Salle salle;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Statut statut = Statut.PLANIFIE;
    private TypeReservation type = TypeReservation.COURS;

    public Reservation() {}

    public Reservation(Salle salle, LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        this.salle = salle;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Cours getCours() { return cours; }
    public void setCours(Cours cours) { this.cours = cours; }
    public Salle getSalle() { return salle; }
    public void setSalle(Salle salle) { this.salle = salle; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }
    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }
    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }
    public TypeReservation getType() { return type; }
    public void setType(TypeReservation type) { this.type = type; }

    public boolean chevauche(Reservation autre) {
        if (!this.date.equals(autre.date)) return false;
        if (this.salle == null || autre.salle == null) return false;
        if (this.salle.getId() != autre.salle.getId()) return false;
        return this.heureDebut.isBefore(autre.heureFin) && autre.heureDebut.isBefore(this.heureFin);
    }

    public long getDureeEnMinutes() {
        return Duration.between(heureDebut, heureFin).toMinutes();
    }

    @Override
    public String toString() {
        return date + " " + heureDebut + "-" + heureFin + " : " + salle;
    }
}