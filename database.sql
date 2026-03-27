CREATE DATABASE IF NOT EXISTS univ_scheduler;
USE univ_scheduler;

-- Table des bâtiments
CREATE TABLE batiment (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    localisation VARCHAR(255),
    nombre_etages INT
);

-- Table des salles
CREATE TABLE salle (
    id INT PRIMARY KEY AUTO_INCREMENT,
    numero VARCHAR(20) NOT NULL,
    capacite INT NOT NULL,
    type ENUM('TD', 'TP', 'AMPHI') NOT NULL,
    batiment_id INT,
    etage INT,
    FOREIGN KEY (batiment_id) REFERENCES batiment(id)
);

-- Table des équipements
CREATE TABLE equipement (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    description TEXT
);

-- Table de liaison salle-équipement (relation many-to-many)
CREATE TABLE salle_equipement (
    salle_id INT,
    equipement_id INT,
    quantite INT DEFAULT 1,
    PRIMARY KEY (salle_id, equipement_id),
    FOREIGN KEY (salle_id) REFERENCES salle(id),
    FOREIGN KEY (equipement_id) REFERENCES equipement(id)
);

-- Table des utilisateurs
CREATE TABLE utilisateur (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'GESTIONNAIRE', 'ENSEIGNANT', 'ETUDIANT') NOT NULL
);

-- Table des cours
CREATE TABLE cours (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    matiere VARCHAR(255),
    enseignant_id INT,
    classe VARCHAR(100),
    groupe VARCHAR(50),
    FOREIGN KEY (enseignant_id) REFERENCES utilisateur(id)
);

-- Table des réservations
CREATE TABLE reservation (
    id INT PRIMARY KEY AUTO_INCREMENT,
    cours_id INT,
    salle_id INT NOT NULL,
    date DATE NOT NULL,
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    statut ENUM('PLANIFIE', 'CONFIRME', 'ANNULE') DEFAULT 'PLANIFIE',
    type_reservation ENUM('COURS', 'REUNION', 'SOUTENANCE', 'ETUDE') DEFAULT 'COURS',
    FOREIGN KEY (cours_id) REFERENCES cours(id),
    FOREIGN KEY (salle_id) REFERENCES salle(id)
);

-- Index pour accélérer les recherches
CREATE INDEX idx_reservation_date_salle ON reservation(date, salle_id);
CREATE INDEX idx_reservation_cours ON reservation(cours_id);

-- Insertion de quelques données de test (facultatif)
INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role) VALUES
('Admin', 'Super', 'admin@univ.edu', 'admin123', 'ADMIN'),
('Gestion', 'Master', 'gestionnaire@univ.edu', 'gest123', 'GESTIONNAIRE'),
('Diallo', 'Abdoulaye', 'Abdoulaye.Diallo@univ.edu', 'ens123', 'ENSEIGNANT'),
('Diop', 'papa', 'papa.diop@univ.edu', 'ens123', 'ENSEIGNANT'),
('Mboup', 'Elhadji', 'Elhadji.Mboup@univ.edu', 'ens123', 'ENSEIGNANT'),
('Diouf', 'Massour', 'massour.Diouf@univ.edu', 'ens123', 'ENSEIGNANT'),
('Mbaye', 'Seny', 'Seny.Mbaye@univ.edu', 'ens123', 'ENSEIGNANT'),
('Sarr', 'Dethie', 'Dethie.Sarr@univ.edu', 'ens123', 'ENSEIGNANT')
('Diallo', 'Awa', 'awa.diallo@univ.edu', 'etu123', 'ETUDIANT');
('Faye', 'Bineta', 'bineta.faye@univ.edu', 'etu123', 'ETUDIANT');

INSERT INTO batiment (nom, localisation, nombre_etages) VALUES
('Bâtiment A', 'Campus Nord', 3),
('Bâtiment B', 'Campus Sud', 2);

INSERT INTO salle (numero, capacite, type, batiment_id, etage) VALUES
('101', 30, 'TD', 1, 1),
('102', 25, 'TP', 1, 1),
('201', 100, 'AMPHI', 1, 2),
('B01', 20, 'TD', 2, 0);

INSERT INTO equipement (nom, description) VALUES
('Vidéoprojecteur', 'Projecteur HDMI/VGA'),
('Tableau interactif', 'Tableau blanc interactif'),
('Climatisation', 'Climatisation réversible');

INSERT INTO salle_equipement (salle_id, equipement_id, quantite) VALUES
(1, 1, 1),
(1, 3, 1),
(2, 1, 1),
(3, 1, 2);