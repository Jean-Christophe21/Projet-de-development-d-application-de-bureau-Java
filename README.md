# 🏥 Application Desktop de Gestion de Pharmacie

Pour ce projet nous avons réalisé une solution desktop locale sans connexion internet requise conçue pour automatiser le cycle de vente et de réapprovisionnement des pharmacies en Afrique de l'Ouest. Elle permet d'optimiser la chaîne de distribution des médicaments, de sécuriser la caisse et d'éviter les ruptures de stock ainsi que la gestion manuelle des péremptions.

## Fonctionnalités

* **Authentification & Rôles :** Connexion sécurisée avec détection automatique du rôle.
* **Tableau de bord :** Indicateurs clés en temps réel (ventes, alertes, ruptures) et suivi de l'activité récente.
* **Gestion de Caisse & Ventes :** Recherche de médicaments par lot, gestion du panier et validation des ventes.
* **Reçus Client :** Affichage d'un récapitulatif post-vente avec option d'impression papier.
* **Suivi de l'Inventaire :** Consultation du stock global avec filtres et statut visuel (*OK*, *Critique*, *Rupture*).
* **Gestion du Catalogue :** Création, modification et suppression des produits ainsi que configuration des seuils d'alerte.
* **Gestion des Lots :** Enregistrement des réceptions (numéro de lot, quantité, péremption) avec décrémentation automatique lors des ventes.
* **Alertes Centralisées :** Suivi prioritaire des ruptures de stock et des lots proches de la date d'expiration.
* **Rapports & Statistiques :** Génération de bilans (ventes, stock, péremptions) avec graphiques, tableaux et options d'exportation.

## Prérequis & Compilation

### Prérequis pour l'exécution du Jar:
* **OS :** Windows, Linux ou macOS
* **Runtime :** Java (JDK/JRE 21)
* **Base de données :** SQLite 3 (embarquée, aucun serveur à installer)

### Commandes de compilation et lancement

#### Avec Maven
```bash
mvn clean package
java -jar target/pharmacie-app.jar
#Commande pour compiler en exécutable pour une plateforme spécifique:
jpackage --type app-image --name AppGestionStockPharmacie --input target/ --main-jar GestionPharmacie.jar --main-class tg.univ.lome.epl.gestionpharmacie.Launcher --module-path target/libs --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,java.sql,java.naming,java.xml
