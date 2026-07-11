/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.model;

/**
 *
 * @author USER
 */
public class Paiement {

    private int idPaiement;
    private String modePaiement;  // ESPECES | CARTE | MOBILE | CHEQUE
    private double montant;
    private String datePaiement;
    private int idVente;

    // Données jointes
    private String codeVente;

    public Paiement() {
    }

    public int getIdPaiement() {
        return idPaiement;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public double getMontant() {
        return montant;
    }

    public String getDatePaiement() {
        return datePaiement;
    }

    public int getIdVente() {
        return idVente;
    }

    public String getCodeVente() {
        return codeVente;
    }

    public void setIdPaiement(int id) {
        this.idPaiement = id;
    }

    public void setModePaiement(String mode) {
        this.modePaiement = mode;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public void setDatePaiement(String date) {
        this.datePaiement = date;
    }

    public void setIdVente(int idV) {
        this.idVente = idV;
    }

    public void setCodeVente(String code) {
        this.codeVente = code;
    }
}
