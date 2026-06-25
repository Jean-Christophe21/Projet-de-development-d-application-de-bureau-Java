/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univ.lome.epl.model;

/**
 *
 * @author USER
 */


public class Utilisateur {
    private int    idUtilisateur;
    private String nom;
    private String prenom;
    private String identifiant;
    private String motDePasse;
    private String role; // celui qui se connecte peut etre ADMIN ou bien VENDEUR

    public Utilisateur() {}
    public Utilisateur(int id, String nom, String prenom,
                       String identifiant, String motDePasse, String role) {
        this.idUtilisateur = id;
        this.nom = nom;
        this.prenom = prenom;
        this.identifiant = identifiant;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    public int    getIdUtilisateur()  {
        return idUtilisateur; 
    }
    
    public String getNom() {
        return nom;
    }
    
    public String getPrenom() {
        return prenom; 
    }
    
    public String getIdentifiant() {
        return identifiant; 
    }
    
    public String getMotDePasse() {
        return motDePasse; 
    }
    
    public String getRole() {
        return role; 
    }

    public void setIdUtilisateur(int id) {
        this.idUtilisateur = id; 
    }
    
    public void setNom(String nom) {
        this.nom = nom; 
    }
    
    public void setPrenom(String prenom) {
        this.prenom = prenom; 
    }
    
    public void setIdentifiant(String ident)  {
        this.identifiant = ident; 
    }
    
    public void setMotDePasse(String mdp) {
        this.motDePasse = mdp; 
    }
    
    public void setRole(String role) {
        this.role = role; 
    }

    @Override public String toString() {
        return prenom + " " + nom + " [" + role + "]";
    }
}