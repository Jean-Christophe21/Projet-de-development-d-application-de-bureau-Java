CREATE TABLE IF NOT EXISTS Utilisateurs (
    id_utilisateur INTEGER PRIMARY KEY AUTOINCREMENT,
    nom            TEXT NOT NULL,
    prenom         TEXT NOT NULL,
    identifiant    TEXT NOT NULL UNIQUE,
    mot_de_passe   TEXT NOT NULL,
    role           TEXT NOT NULL CHECK(role IN ('ADMIN', 'VENDEUR'))
);

INSERT OR IGNORE INTO Utilisateurs (nom, prenom, identifiant, mot_de_passe, role)
VALUES 
('Admin', 'System', 'admin', 'admin123', 'ADMIN'),
('Vendeur', 'Bob', 'vendeur', 'vendeur123', 'VENDEUR');

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
    monnaie_rendue REAL,
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

-- DUMMY DATA FOR TESTING
INSERT OR IGNORE INTO Categories (id_categorie, libelle) VALUES 
(1, 'Antalgique'),
(2, 'Antibiotique'),
(3, 'Anti-inflammatoire'),
(4, 'Antipaludéen'),
(5, 'Vitamines');

INSERT OR IGNORE INTO Fournisseurs (id_fournisseur, nom, contact, email, adresse) VALUES
(1, 'Pharma Supply Co.', '+228 90 00 00 01', 'contact@pharmasupply.tg', 'Lome, Togo'),
(2, 'Medicus Distribution', '+228 91 11 22 33', 'sales@medicus.tg', 'Kara, Togo');

INSERT OR IGNORE INTO Medicaments (id_medicament, nom_commercial, dci, description, comment_utiliser, effets_secondaires, est_generique, seuil_alerte) VALUES
(1, 'Paracétamol 500mg', 'Paracétamol', 'Boîte de 16 comprimés', 'Voie orale, 1 comprimé si douleur', 'Rares', 1, 20),
(2, 'Amoxicilline 1g', 'Amoxicilline', 'Boîte de 14 comprimés', '1 comprimé matin et soir', 'Troubles digestifs', 1, 10),
(3, 'Ibuprofène 400mg', 'Ibuprofène', 'Boîte de 20 comprimés', '1 comprimé par repas', 'Maux d''estomac', 1, 15),
(4, 'Artequin', 'Artésunate', 'Traitement antipaludique', 'Selon prescription', 'Maux de tête', 0, 5),
(5, 'Vitamine C', 'Acide Ascorbique', 'Tube de 10 comprimés effervescents', '1 le matin', 'Aucun', 0, 30);

INSERT OR IGNORE INTO MedicamentCategories (id_medicament, id_categorie) VALUES
(1, 1), (2, 2), (3, 3), (4, 4), (5, 5);

INSERT OR IGNORE INTO Lots (id_lot, numero_lot, quantite_initiale, quantite_restante, prix_unitaire, date_peremption, id_medicament, id_fournisseur) VALUES
(1, 'L2023-001', 100, 100, 500.0, '2028-12-31', 1, 1),
(2, 'L2023-002', 50, 50, 2000.0, '2026-06-30', 2, 2),
(3, 'L2023-003', 75, 75, 800.0, '2027-10-15', 3, 1),
(4, 'L2023-004', 30, 30, 4500.0, '2025-05-20', 4, 2),
(5, 'L2023-005', 150, 150, 1000.0, '2029-01-01', 5, 1);

