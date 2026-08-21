/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.model;

/**
 *
 * @author USER
 */

public class Client {
    private int    idClient;
    private String nom;
    private String prenom;
    private String telephone;
    private String email;

    public Client() {}
    public Client(int id, String nom, String prenom, String telephone, String email) {
        this.idClient   = id;
        this.nom        = nom;
        this.prenom     = prenom;
        this.telephone  = telephone;
        this.email      = email;
    }

    public int    getIdClient() {
        return idClient; 
    }
    
    public String getNom() {
        return nom; 
    }
    
    public String getPrenom() {
        return prenom; 
    }
    
    public String getTelephone() {
        return telephone; 
    }
    
    public String getEmail() {
        return email; 
    }

    public void setIdClient(int id) {
        this.idClient = id; 
    }
    
    public void setNom(String nom) {
        this.nom = nom; 
    }
    
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    
    public void setTelephone(String tel) {
        this.telephone = tel; 
    }
    
    public void setEmail(String email) {
        this.email = email; 
    }
}
