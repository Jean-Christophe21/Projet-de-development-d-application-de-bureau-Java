/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.model;

/**
 *
 * @author USER
 */

public class Categorie {
    private int    idCategorie;
    private String libelle;

    public Categorie() {}
    public Categorie(int id, String libelle) {
        this.idCategorie = id;
        this.libelle = libelle;
    }

    public int    getIdCategorie() {
        return idCategorie; 
    }
    
    public String getLibelle() {
        return libelle; 
    }

    public void setIdCategorie(int id) {
        this.idCategorie = id; 
    }
    
    public void setLibelle(String libelle) {
        this.libelle = libelle; 
    }

    @Override public String toString() {
        return libelle; 
    }
}
