/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author USER
 */
public class Vente {

    private int idVente;
    private String codeVente;       // Order ID affiché en UI
    private String dateVente;
    private double montantTotal;
    private Double montantRecu;
    private Double monnaieRendue;   // colonne virtuelle SQLite
    private Integer idUtilisateur;
    private Integer idClient;

    // Données jointes
    private String nomUtilisateur;
    private String nomClient;
    private List<LigneVente> lignes = new ArrayList<>();

    public Vente() {
    }

    // ── Getters ──────────────────────────────
    public int getIdVente() {
        return idVente;
    }

    public String getCodeVente() {
        return codeVente;
    }

    public String getDateVente() {
        return dateVente;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public Double getMontantRecu() {
        return montantRecu;
    }

    public Double getMonnaieRendue() {
        return monnaieRendue;
    }

    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public Integer getIdClient() {
        return idClient;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public String getNomClient() {
        return nomClient;
    }

    public List<LigneVente> getLignes() {
        return lignes;
    }

    // ── Setters ──────────────────────────────
    public void setIdVente(int id) {
        this.idVente = id;
    }

    public void setCodeVente(String code) {
        this.codeVente = code;
    }

    public void setDateVente(String date) {
        this.dateVente = date;
    }

    public void setMontantTotal(double mt) {
        this.montantTotal = mt;
    }

    public void setMontantRecu(Double mr) {
        this.montantRecu = mr;
    }

    public void setMonnaieRendue(Double mr) {
        this.monnaieRendue = mr;
    }

    public void setIdUtilisateur(Integer id) {
        this.idUtilisateur = id;
    }

    public void setIdClient(Integer id) {
        this.idClient = id;
    }

    public void setNomUtilisateur(String nom) {
        this.nomUtilisateur = nom;
    }

    public void setNomClient(String nom) {
        this.nomClient = nom;
    }

    public void setLignes(List<LigneVente> l) {
        this.lignes = l;
    }
}
