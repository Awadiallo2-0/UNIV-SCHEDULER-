package com.univ.model;

import java.util.HashMap;
import java.util.Map;

public class Salle {
    public enum TypeSalle { TD, TP, AMPHI }

    private int id;
    private String numero;
    private int capacite;
    private TypeSalle type;
    private Batiment batiment;
    private int etage;
    private Map<Equipement, Integer> equipements = new HashMap<>();

    public Salle() {}

    public Salle(String numero, int capacite, TypeSalle type, int etage) {
        this.numero = numero;
        this.capacite = capacite;
        this.type = type;
        this.etage = etage;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }
    public TypeSalle getType() { return type; }
    public void setType(TypeSalle type) { this.type = type; }
    public Batiment getBatiment() { return batiment; }
    public void setBatiment(Batiment batiment) { this.batiment = batiment; }
    public int getEtage() { return etage; }
    public void setEtage(int etage) { this.etage = etage; }
    public Map<Equipement, Integer> getEquipements() { return equipements; }

    public void addEquipement(Equipement equipement, int quantite) {
        equipements.put(equipement, quantite);
    }

    public boolean aEquipement(String nomEquipement) {
        return equipements.keySet().stream().anyMatch(e -> e.getNom().equalsIgnoreCase(nomEquipement));
    }

    @Override
    public String toString() {
        String bat = (batiment != null) ? batiment.getNom() + " - " : "";
        return bat + "Salle " + numero + " (Cap." + capacite + ")";
    }
}