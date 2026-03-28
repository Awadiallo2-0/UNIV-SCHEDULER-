package com.univ.model;

public class Cours {
    private int id;
    private String nom;
    private String matiere;
    private Utilisateur enseignant;
    private String classe;
    private String groupe;

    public Cours() {}

    public Cours(String nom, String matiere, Utilisateur enseignant, String classe, String groupe) {
        this.nom = nom;
        this.matiere = matiere;
        this.enseignant = enseignant;
        this.classe = classe;
        this.groupe = groupe;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getMatiere() { return matiere; }
    public void setMatiere(String matiere) { this.matiere = matiere; }
    public Utilisateur getEnseignant() { return enseignant; }
    public void setEnseignant(Utilisateur enseignant) { this.enseignant = enseignant; }
    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }
    public String getGroupe() { return groupe; }
    public void setGroupe(String groupe) { this.groupe = groupe; }

    @Override
    public String toString() {
        return nom + " (" + matiere + ") - " + classe + " " + groupe;
    }
}