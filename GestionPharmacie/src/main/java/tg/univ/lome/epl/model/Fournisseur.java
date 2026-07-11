/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.model;

/**
 *
 * @author USER
 */


public class Fournisseur {
    private int    idFournisseur;
    private String nom;
    private String contact;
    private String email;
    private String adresse;

    public Fournisseur() {}
    public Fournisseur(int id, String nom, String contact, String email, String adresse) {
        this.idFournisseur = id;
        this.nom = nom;
        this.contact = contact;
        this.email = email;
        this.adresse = adresse;
    }

    public int    getIdFournisseur() {
        return idFournisseur; 
    }
    
    public String getNom() {
        return nom; 
    }
    
    public String getContact() {
        return contact; 
    }
    
    public String getEmail() {
        return email; 
    }
    
    public String getAdresse() {
        return adresse; 
    }

    public void setIdFournisseur(int id) {
        this.idFournisseur = id; 
    }
    
    public void setNom(String nom) {
        this.nom = nom; 
    }
    
    public void setContact(String contact) {
        this.contact = contact; 
    }
    
    public void setEmail(String email) {
        this.email = email; 
    }
    
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
}
