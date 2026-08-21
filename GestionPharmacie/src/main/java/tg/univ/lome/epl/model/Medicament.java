/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.model;

import java.util.ArrayList;
import java.util.List /**
         *
         * @author USER
         */
        ;

public class Medicament {

    private int idMedicament;
    private String nomCommercial;
    private String dci;                    // Dénomination Commune Internationale, c'est plus utilisé en pharmacie
    private String description;
    private String commentUtiliser;        // comment utiliser le médicament, j'ai vu ça sur la maquette
    private String effetsSecondaires;      // Les effets secondaires aussi, sur la maquette, je ne sais pas si on va enlever ça après ou pas
    private boolean estGenerique;
    private int seuilAlerte;
    private Integer idMedicamentReference; // null si princeps

    // Données calculées (issues de jointures / vues)
    private int stockTotal;             // SUM(quantite_restante) des lots actifs
    private int lifetimeSupply;         // SUM(quantite_initiale) tous lots
    private int lifetimeSales;          // lifetimeSupply - stockTotal
    private List<Categorie> categories = new ArrayList<>();

    public Medicament() {
    }

    //  Getters 
    public int getIdMedicament() {
        return idMedicament;
    }

    public String getNomCommercial() {
        return nomCommercial;
    }

    public String getDci() {
        return dci;
    }

    public String getDescription() {
        return description;
    }

    public String getCommentUtiliser() {
        return commentUtiliser;
    }

    public String getEffetsSecondaires() {
        return effetsSecondaires;
    }

    public boolean isEstGenerique() {
        return estGenerique;
    }

    public int getSeuilAlerte() {
        return seuilAlerte;
    }

    public Integer getIdMedicamentReference() {
        return idMedicamentReference;
    }

    public int getStockTotal() {
        return stockTotal;
    }

    public int getLifetimeSupply() {
        return lifetimeSupply;
    }

    public int getLifetimeSales() {
        return lifetimeSales;
    }

    public List<Categorie> getCategories() {
        return categories;
    }

    //  Setters
    public void setIdMedicament(int id) {
        this.idMedicament = id;
    }

    public void setNomCommercial(String nom) {
        this.nomCommercial = nom;
    }

    public void setDci(String dci) {
        this.dci = dci;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public void setCommentUtiliser(String cu) {
        this.commentUtiliser = cu;
    }

    public void setEffetsSecondaires(String es) {
        this.effetsSecondaires = es;
    }

    public void setEstGenerique(boolean eg) {
        this.estGenerique = eg;
    }

    public void setSeuilAlerte(int seuil) {
        this.seuilAlerte = seuil;
    }

    public void setIdMedicamentReference(Integer idRef) {
        this.idMedicamentReference = idRef;
    }

    public void setStockTotal(int stockTotal) {
        this.stockTotal = stockTotal;
    }

    public void setLifetimeSupply(int ls) {
        this.lifetimeSupply = ls;
    }

    public void setLifetimeSales(int ls) {
        this.lifetimeSales = ls;
    }

    public void setCategories(List<Categorie> cats) {
        this.categories = cats;
    }

    @Override
    public String toString() {
        return nomCommercial + (dci != null ? " (" + dci + ")" : "");
    }
}
