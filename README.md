# 💰 Gestion d'Argent

Application desktop Java (JavaFX) de suivi d'épargne et de budget personnel,
avec stockage local dans un fichier SQLite (`.db`).

## Fonctionnalités

- 🔒 Protection par mot de passe/PIN au démarrage (créé au premier lancement)
- 📊 Tableau de bord : solde, revenus/dépenses du mois, répartition des
  dépenses par catégorie (graphique), transactions récentes
- 💳 Gestion des transactions (ajout, modification, suppression, filtre par mois)
- 🎯 Budgets mensuels par catégorie avec barre de progression et alertes de
  dépassement
- 🏷️ Catégories prédéfinies + création de catégories personnalisées
- 📄 Export des transactions en PDF et Excel

## Prérequis

- **JDK 17** ou supérieur ([Adoptium](https://adoptium.net/) recommandé)
- **Maven 3.8+**

Vérifiez vos installations :
```bash
java -version
mvn -version
```

## Installation

1. Décompressez le projet, puis placez-vous dans le dossier :
   ```bash
   cd gestion-argent
   ```

2. Téléchargez les dépendances et compilez :
   ```bash
   mvn clean install
   ```

## Lancer l'application

**Option recommandée (via exec-maven-plugin, la plus fiable) :**
```bash
mvn clean compile exec:java
```

**Option JAR exécutable :**
```bash
mvn clean package
java -jar target/gestion-argent.jar
```

## Stockage des données

La base de données SQLite est créée automatiquement au premier lancement dans :
- Windows : `C:\Users\<votre_nom>\.gestion-argent\gestion-argent.db`
- macOS / Linux : `~/.gestion-argent/gestion-argent.db`

Vos données restent donc en local sur votre ordinateur, même après avoir
déplacé ou supprimé le dossier du projet.

## Structure du projet

```
src/main/java/com/gestionargent/
├── model/          → Transaction, Categorie, Budget, TypeTransaction
├── dao/             → accès SQLite (DatabaseManager, *DAO)
├── service/         → logique métier (calculs, validations, export)
├── controller/       → contrôleurs JavaFX liés aux vues FXML
├── util/             → utilitaires (hachage mot de passe, alertes)
└── App.java           → point d'entrée de l'application

src/main/resources/com/gestionargent/
├── view/             → fichiers FXML (une vue par écran)
└── css/               → feuille de style de l'application
```

## Premier lancement

Au tout premier démarrage, l'application vous demande de créer un mot de
passe (4 caractères minimum). Ce mot de passe est haché (SHA-256 + sel) avant
d'être stocké — il n'est jamais conservé en clair.

## Réinitialiser l'application

Pour repartir de zéro (mot de passe oublié, données de test à effacer),
supprimez simplement le fichier :
```
~/.gestion-argent/gestion-argent.db
```

## Créer un installateur Windows (.exe) partageable

L'appli peut être packagée en un **vrai installateur Windows**, avec icône,
raccourcis et Java embarqué — la personne qui le reçoit n'a besoin d'installer
ni Java ni Maven.

### Prérequis (uniquement sur TA machine de build)

1. **JDK 17+** (déjà nécessaire pour compiler)
2. **WiX Toolset v3** : télécharge et installe depuis
   [wixtoolset.org](https://wixtoolset.org/) (nécessaire pour que `jpackage`
   puisse générer un `.exe`/`.msi` sur Windows)

### Générer l'installateur

Depuis le dossier du projet :
```bash
mvn clean package jpackage:jpackage
```

L'installateur `.exe` est généré dans :
```
target/installateur/GestionArgent-1.0.0.exe
```

### Partager l'application

Envoie simplement ce fichier `.exe`. La personne le lance, suit l'assistant
d'installation (avec choix du dossier d'installation, raccourci bureau et
menu Démarrer déjà configurés), et l'application est prête — aucune
installation de Java requise de son côté.

### Icône de l'application

L'icône se trouve dans `packaging/icon.ico` (utilisée par l'installateur et
l'exécutable) et `packaging/icon.png` (aperçu / autres usages). Pour la
changer, remplace ces fichiers en conservant les mêmes noms.

## Générer les installateurs Mac et Linux (sans machine Mac/Linux)

`jpackage` ne peut construire un installateur que pour l'OS sur lequel il
tourne : impossible de créer un `.dmg` ou un `.deb` depuis Windows. Le projet
inclut un **workflow GitHub Actions** (`.github/workflows/build-installers.yml`)
qui construit automatiquement les 3 installateurs (Windows `.exe`, macOS
`.dmg`, Linux `.deb`) sur les machines gratuites fournies par GitHub.

### Mise en place (une seule fois)

1. Crée un compte [GitHub](https://github.com) si tu n'en as pas
2. Crée un nouveau dépôt (public ou privé) et pousse le projet dedans :
   ```bash
   cd gestion-argent
   git init
   git add .
   git commit -m "Version initiale"
   git branch -M main
   git remote add origin https://github.com/<ton-compte>/<ton-depot>.git
   git push -u origin main
   ```

### Récupérer les 3 installateurs

Dès que tu pousses sur `main` (ou en cliquant sur "Run workflow" dans l'onglet
**Actions** du dépôt GitHub) :

1. Va dans l'onglet **Actions** de ton dépôt GitHub
2. Ouvre l'exécution en cours (ou la dernière terminée)
3. En bas de la page, section **Artifacts**, télécharge :
   - `installateur-windows` → contient le `.exe`
   - `installateur-macos` → contient le `.dmg`
   - `installateur-linux` → contient le `.deb`

Chaque build prend 2 à 5 minutes environ. Aucune installation de WiX ni de
machine supplémentaire n'est nécessaire de ton côté : tout se passe sur les
serveurs de GitHub.



- Transactions récurrentes (abonnements, salaire automatique)
- Multi-comptes (courant, épargne, carte...)
- Suivi d'investissements (actions, crypto)
- Synchronisation / sauvegarde cloud

## Page de téléchargement (site web public)

Le fichier `index.html` à la racine du projet est une page de téléchargement
autonome, avec détection automatique du système d'exploitation du visiteur et
un bouton par plateforme (Windows `.exe`, macOS `.dmg`, Linux `.deb`). Les
boutons pointent vers les liens **permanents** de la dernière Release GitHub
(mis à jour automatiquement par le workflow à chaque build réussi).

### Mettre la page en ligne gratuitement avec GitHub Pages

1. Sur ton dépôt GitHub, va dans **Settings** > **Pages** (menu de gauche)
2. Sous "Build and deployment" > "Source", choisis **"Deploy from a branch"**
3. Sous "Branch", choisis **`main`** et le dossier **`/ (root)`**, puis **Save**
4. Attends 1 à 2 minutes, puis rafraîchis la page : une adresse du type
   `https://bartbayemi-ctrl.github.io/Gestion-Argent/` apparaît en haut —
   c'est l'adresse publique de ta page de téléchargement, à partager librement.
