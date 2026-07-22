package tg.univ.lome.epl.util;

import tg.univ.lome.epl.model.Utilisateur;

public class SessionManager {
    private static Utilisateur utilisateurConnecte;

    public static Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public static void setUtilisateurConnecte(Utilisateur utilisateur) {
        utilisateurConnecte = utilisateur;
    }

    public static void clearSession() {
        utilisateurConnecte = null;
    }
}
