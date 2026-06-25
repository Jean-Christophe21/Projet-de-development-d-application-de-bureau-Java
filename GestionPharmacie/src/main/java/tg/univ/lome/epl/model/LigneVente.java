/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.model;

/**
 *
 * @author USER
 */
public class LigneVente {

    private int idLigne;
    private int quantite;
    private double prixUnitaireApplique;
    private int idVente;
    private int idLot;

    // Données jointes
    private String nomMedicament;
    private String numeroLot;

    public LigneVente() {
    }

    public LigneVente(int quantite, double prix, int idVente, int idLot) {
        this.quantite = quantite;
        this.prixUnitaireApplique = prix;
        this.idVente = idVente;
        this.idLot = idLot;
    }

    public int getIdLigne() {
        return idLigne;
    }

    public int getQuantite() {
        return quantite;
    }

    public double getPrixUnitaireApplique() {
        return prixUnitaireApplique;
    }

    public int getIdVente() {
        return idVente;
    }

    public int getIdLot() {
        return idLot;
    }

    public String getNomMedicament() {
        return nomMedicament;
    }

    public String getNumeroLot() {
        return numeroLot;
    }

    public double getSousTotal() {
        return quantite * prixUnitaireApplique;
    }

    public void setIdLigne(int id) {
        this.idLigne = id;
    }

    public void setQuantite(int q) {
        this.quantite = q;
    }

    public void setPrixUnitaireApplique(double prix) {
        this.prixUnitaireApplique = prix;
    }

    public void setIdVente(int idV) {
        this.idVente = idV;
    }

    public void setIdLot(int idL) {
        this.idLot = idL;
    }

    public void setNomMedicament(String nom) {
        this.nomMedicament = nom;
    }

    public void setNumeroLot(String num) {
        this.numeroLot = num;
    }
}
