# UNIV-SCHEDULER

**Application de Gestion Intelligente des Salles et des Emplois du Temps**

---

## 📖 Description

UNIV-SCHEDULER est une application Java conçue pour faciliter la gestion des salles et la planification des emplois du temps au sein d’un établissement universitaire. Elle permet aux administrateurs, gestionnaires, enseignants et étudiants de visualiser, réserver et optimiser l’utilisation des ressources (salles, équipements) en temps réel.

Le projet répond aux problématiques suivantes :
- Conflits de réservation de salles
- Salles surchargées ou sous‑utilisées
- Difficulté à trouver rapidement une salle disponible
- Absence de visibilité en temps réel
- Gestion manuelle complexe avec Excel/Paperboard

---

## 🚀 Fonctionnalités principales

### 👥 Gestion des utilisateurs
- Authentification sécurisée avec différents rôles : `ADMIN`, `GESTIONNAIRE`, `ENSEIGNANT`, `ETUDIANT`
- Profils personnalisés avec droits d’accès distincts

### 🏢 Gestion des infrastructures
- Ajout, modification, suppression de bâtiments, salles et équipements
- Association d’équipements aux salles (vidéoprojecteur, tableau interactif, climatisation…)

### 📅 Emploi du temps
- Vue calendrier interactive (jour, semaine, mois)
- Affichage des créneaux horaires (8h – 19h) avec jours de la semaine en colonnes et heures en lignes
- Double‑clic sur une cellule pour ajouter, modifier ou annuler une réservation
- Affichage du texte **libre** dans les créneaux vacants

### 🔍 Recherche de salles disponibles
- Filtres avancés : date, heure, capacité minimale, type de salle, équipements requis
- Résultats en temps réel

### 📊 Rapports et statistiques
- Tableau de bord avec cartes de synthèse (nombre de salles, cours, réservations, utilisateurs)
- Courbe d’évolution des réservations sur une période choisie
- Graphique en barres de l’occupation par salle
- Camembert de répartition par type de réservation (cours, réunion, soutenance, étude)
- Taux d’occupation global automatiquement calculé

### 📎 Export de données
- Export CSV (format universel)
- Export PDF (avec iText) des rapports de réservations
- (Optionnel) Export Excel avec Apache POI

### 📧 Notifications
- Notifications simulées en console pour :
  - Nouvelle réservation
  - Modification de réservation
  - Annulation de réservation
  - Disponibilité de salle (exemple : à tous les enseignants)

---

## 🛠 Technologies utilisées

| Technologie | Version / Description |
|-------------|-----------------------|
| **Java** | 17 |
| **JavaFX** | 17.0.18 (SDK) |
| **Base de données** | MySQL 8.0.33 (ou SQLite) |
| **Build** | Maven 3.11+ |
| **Persistance** | JDBC (DAO pattern) |
| **Graphiques** | JavaFX Charts (LineChart, BarChart, PieChart) |
| **Export PDF** | iText 7 Core |
| **Export CSV** | API Java standard |
| **IDE** | Eclipse (ou IntelliJ, VS Code) |

---

## 📦 Installation

### Prérequis
- Java Development Kit (JDK) 17
- MySQL Server (ou SQLite)
- Maven (inclus dans l’IDE ou à installer)
- Git (facultatif pour cloner)

### Étape 1 : Cloner le projet
```bash
git clone https://github.com/ton-compte/univ-scheduler.git
cd univ-scheduler