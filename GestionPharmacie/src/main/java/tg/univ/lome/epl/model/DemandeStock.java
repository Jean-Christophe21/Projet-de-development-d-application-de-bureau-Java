/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.model;

/**
 *
 * @author USER
 */
public class DemandeStock {

    private int idDemande;
    private String statut;           // EN_ATTENTE | VALIDEE | RECUE | ANNULEE
    private int quantiteDemandee;
    private String notes;
    private String dateDemande;
    private String dateTraitement;
    private int idMedicament;
    private Integer idFournisseur;
    private Integer idUtilisateur;

    // Données jointes
    private String nomMedicament;
    private String nomFournisseur;
    private String nomUtilisateur;

    public DemandeStock() {
    }

    public int getIdDemande() {
        return idDemande;
    }

    public String getStatut() {
        return statut;
    }

    public int getQuantiteDemandee() {
        return quantiteDemandee;
    }

    public String getNotes() {
        return notes;
    }

    public String getDateDemande() {
        return dateDemande;
    }

    public String getDateTraitement() {
        return dateTraitement;
    }

    public int getIdMedicament() {
        return idMedicament;
    }

    public Integer getIdFournisseur() {
        return idFournisseur;
    }

    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public String getNomMedicament() {
        return nomMedicament;
    }

    public String getNomFournisseur() {
        return nomFournisseur;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setIdDemande(int id) {
        this.idDemande = id;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public void setQuantiteDemandee(int qte) {
        this.quantiteDemandee = qte;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setDateDemande(String date) {
        this.dateDemande = date;
    }

    public void setDateTraitement(String date) {
        this.dateTraitement = date;
    }

    public void setIdMedicament(int idM) {
        this.idMedicament = idM;
    }

    public void setIdFournisseur(Integer idF) {
        this.idFournisseur = idF;
    }

    public void setIdUtilisateur(Integer idU) {
        this.idUtilisateur = idU;
    }

    public void setNomMedicament(String nom) {
        this.nomMedicament = nom;
    }

    public void setNomFournisseur(String nom) {
        this.nomFournisseur = nom;
    }

    public void setNomUtilisateur(String nom) {
        this.nomUtilisateur = nom;
    }
}
