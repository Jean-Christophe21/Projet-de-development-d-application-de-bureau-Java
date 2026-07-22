CREATE TABLE IF NOT EXISTS Utilisateurs (
    id_utilisateur INTEGER PRIMARY KEY AUTOINCREMENT,
    nom            TEXT NOT NULL,
    prenom         TEXT NOT NULL,
    identifiant    TEXT NOT NULL UNIQUE,
    mot_de_passe   TEXT NOT NULL,
    role           TEXT NOT NULL CHECK(role IN ('ADMIN', 'VENDEUR'))
);

INSERT OR IGNORE INTO Utilisateurs (nom, prenom, identifiant, mot_de_passe, role)
VALUES ('Admin', 'System', 'admin', 'admin123', 'ADMIN');

CREATE TABLE IF NOT EXISTS Fournisseurs (
    id_fournisseur INTEGER PRIMARY KEY AUTOINCREMENT,
    nom            TEXT NOT NULL,
    contact        TEXT,
    email          TEXT,
    adresse        TEXT
);

CREATE TABLE IF NOT EXISTS Clients (
    id_client  INTEGER PRIMARY KEY AUTOINCREMENT,
    nom        TEXT NOT NULL,
    prenom     TEXT,
    telephone  TEXT,
    email      TEXT
);

CREATE TABLE IF NOT EXISTS Categories (
    id_categorie INTEGER PRIMARY KEY AUTOINCREMENT,
    libelle      TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS Medicaments (
    id_medicament           INTEGER PRIMARY KEY AUTOINCREMENT,
    nom_commercial          TEXT NOT NULL,
    dci                     TEXT,
    description             TEXT,
    comment_utiliser        TEXT,
    effets_secondaires      TEXT,
    est_generique           INTEGER NOT NULL DEFAULT 0 CHECK(est_generique IN (0,1)),
    seuil_alerte            INTEGER NOT NULL DEFAULT 10,
    id_medicament_reference INTEGER,
    FOREIGN KEY (id_medicament_reference)
        REFERENCES Medicaments(id_medicament) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS MedicamentCategories (
    id_medicament INTEGER NOT NULL,
    id_categorie  INTEGER NOT NULL,
    PRIMARY KEY (id_medicament, id_categorie),
    FOREIGN KEY (id_medicament) REFERENCES Medicaments(id_medicament) ON DELETE CASCADE,
    FOREIGN KEY (id_categorie)  REFERENCES Categories(id_categorie)  ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Lots (
    id_lot            INTEGER PRIMARY KEY AUTOINCREMENT,
    numero_lot        TEXT    NOT NULL UNIQUE,
    quantite_initiale INTEGER NOT NULL CHECK(quantite_initiale > 0),
    quantite_restante INTEGER NOT NULL CHECK(quantite_restante >= 0),
    prix_unitaire     REAL    NOT NULL CHECK(prix_unitaire > 0),
    date_peremption   TEXT    NOT NULL,
    id_medicament     INTEGER NOT NULL,
    id_fournisseur    INTEGER,
    FOREIGN KEY (id_medicament)  REFERENCES Medicaments(id_medicament) ON DELETE CASCADE,
    FOREIGN KEY (id_fournisseur) REFERENCES Fournisseurs(id_fournisseur) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS Ventes (
    id_vente       INTEGER PRIMARY KEY AUTOINCREMENT,
    code_vente     TEXT NOT NULL UNIQUE
                   DEFAULT (strftime('%Y%m%d%H%M%S','now') || substr(abs(random()),1,4)),
    date_vente     TEXT NOT NULL DEFAULT (datetime('now','localtime')),
    montant_total  REAL NOT NULL DEFAULT 0.00,
    montant_recu   REAL,
    id_utilisateur INTEGER,
    id_client      INTEGER,
    FOREIGN KEY (id_utilisateur) REFERENCES Utilisateurs(id_utilisateur) ON DELETE SET NULL,
    FOREIGN KEY (id_client)      REFERENCES Clients(id_client)           ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS LignesVentes (
    id_ligne               INTEGER PRIMARY KEY AUTOINCREMENT,
    quantite               INTEGER NOT NULL CHECK(quantite > 0),
    prix_unitaire_applique REAL    NOT NULL CHECK(prix_unitaire_applique > 0),
    id_vente               INTEGER NOT NULL,
    id_lot                 INTEGER NOT NULL,
    FOREIGN KEY (id_vente) REFERENCES Ventes(id_vente)  ON DELETE CASCADE,
    FOREIGN KEY (id_lot)   REFERENCES Lots(id_lot)      ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS Paiements (
    id_paiement    INTEGER PRIMARY KEY AUTOINCREMENT,
    mode_paiement  TEXT NOT NULL DEFAULT 'ESPECES'
                   CHECK(mode_paiement IN ('ESPECES','CARTE','MOBILE','CHEQUE')),
    montant        REAL NOT NULL CHECK(montant > 0),
    date_paiement  TEXT NOT NULL DEFAULT (datetime('now','localtime')),
    id_vente       INTEGER NOT NULL,
    FOREIGN KEY (id_vente) REFERENCES Ventes(id_vente) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS DemandesStock (
    id_demande        INTEGER PRIMARY KEY AUTOINCREMENT,
    statut            TEXT NOT NULL DEFAULT 'EN_ATTENTE'
                      CHECK(statut IN ('EN_ATTENTE','VALIDEE','RECUE','ANNULEE')),
    quantite_demandee INTEGER NOT NULL CHECK(quantite_demandee > 0),
    notes             TEXT,
    date_demande      TEXT NOT NULL DEFAULT (datetime('now','localtime')),
    date_traitement   TEXT,
    id_medicament     INTEGER NOT NULL,
    id_fournisseur    INTEGER,
    id_utilisateur    INTEGER,
    FOREIGN KEY (id_medicament)  REFERENCES Medicaments(id_medicament)   ON DELETE CASCADE,
    FOREIGN KEY (id_fournisseur) REFERENCES Fournisseurs(id_fournisseur) ON DELETE SET NULL,
    FOREIGN KEY (id_utilisateur) REFERENCES Utilisateurs(id_utilisateur) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS ConfigAlertes (
    id_config              INTEGER PRIMARY KEY AUTOINCREMENT,
    jours_avant_peremption INTEGER NOT NULL DEFAULT 90,
    seuil_stock_critique   INTEGER NOT NULL DEFAULT 5,
    date_maj               TEXT NOT NULL DEFAULT (datetime('now','localtime')),
    id_utilisateur         INTEGER,
    FOREIGN KEY (id_utilisateur) REFERENCES Utilisateurs(id_utilisateur) ON DELETE SET NULL
);
