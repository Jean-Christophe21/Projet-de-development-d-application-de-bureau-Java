/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.model;

/**
 *
 * @author USER
 */
public class Lot {

    private int idLot;
    private String numeroLot;
    private int quantiteInitiale;
    private int quantiteRestante;
    private double prixUnitaire;
    private String datePeremption;   // ISO 8601 "YYYY-MM-DD"
    private int idMedicament;
    private Integer idFournisseur;

    // Données jointes utiles à l'affichage
    private String nomMedicament;
    private String nomFournisseur;

    public Lot() {
    }

    // Getters 
    public int getIdLot() {
        return idLot;
    }

    public String getNumeroLot() {
        return numeroLot;
    }

    public int getQuantiteInitiale() {
        return quantiteInitiale;
    }

    public int getQuantiteRestante() {
        return quantiteRestante;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public String getDatePeremption() {
        return datePeremption;
    }

    public int getIdMedicament() {
        return idMedicament;
    }

    public Integer getIdFournisseur() {
        return idFournisseur;
    }

    public String getNomMedicament() {
        return nomMedicament;
    }

    public String getNomFournisseur() {
        return nomFournisseur;
    }

    //  Setters
    public void setIdLot(int id) {
        this.idLot = id;
    }

    public void setNumeroLot(String num) {
        this.numeroLot = num;
    }

    public void setQuantiteInitiale(int qi) {
        this.quantiteInitiale = qi;
    }

    public void setQuantiteRestante(int qr) {
        this.quantiteRestante = qr;
    }

    public void setPrixUnitaire(double prix) {
        this.prixUnitaire = prix;
    }

    public void setDatePeremption(String date) {
        this.datePeremption = date;
    }

    public void setIdMedicament(int idMed) {
        this.idMedicament = idMed;
    }

    public void setIdFournisseur(Integer idF) {
        this.idFournisseur = idF;
    }

    public void setNomMedicament(String nom) {
        this.nomMedicament = nom;
    }

    public void setNomFournisseur(String nom) {
        this.nomFournisseur = nom;
    }

    /**
     * Vrai si la date de péremption est dans moins de N jours
     */
    public boolean expirereDans(int jours) {
        if (datePeremption == null) {
            return false;
        }
        java.time.LocalDate expiry = java.time.LocalDate.parse(datePeremption);
        return expiry.isBefore(java.time.LocalDate.now().plusDays(jours));
    }
}

