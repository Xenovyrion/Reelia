# Reelia

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![minSdk](https://img.shields.io/badge/minSdk-26-3DDC84?logo=android&logoColor=white)
![targetSdk](https://img.shields.io/badge/targetSdk-35-3DDC84?logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-Auth%20%2B%20Firestore-FFCA28?logo=firebase&logoColor=black)
![Version](https://img.shields.io/badge/version-0.13.0-8C8FFF)
![Android CI](https://github.com/Xenovyrion/Reelia/actions/workflows/android-build.yml/badge.svg)

Reelia est une application Android personnelle de suivi de séries et films — un
remplaçant de TV Time, avec une bibliothèque synchronisée entre plusieurs appareils.

## Fonctionnalités

- **Accueil** — hub de découverte : suite de visionnage, à venir, suggestions basées
  sur ta bibliothèque (recommandations TMDB), tendances du moment, derniers
  films/séries sortis, et une recherche rapide sur séries + films. 100% via l'API
  TMDB déjà utilisée par l'app, aucun service tiers payant.
- **Séries / Films** — deux onglets dédiés (filtre statut/genre, recherche scopée au
  type, affichage grille ou liste), ajout via recherche TMDB (poster, synopsis,
  saisons/épisodes récupérés automatiquement), suivi épisode par épisode. Cocher un
  épisode rattrape automatiquement les épisodes précédents non vus de la saison ; un
  appui long permet de cocher/décocher un épisode individuellement. Le clic sur un
  épisode ouvre une fenêtre de détail (image, résumé, date de diffusion, note).
- **Recherche** — cartes affiches comme le reste de l'appli, filtre par genre, et
  une recherche qui n'interroge l'API qu'après une pause de saisie (pas d'appel à
  chaque lettre tapée).
- **Fiches acteurs/actrices** — biographie (avec repli automatique en anglais si la
  traduction française n'existe pas sur TMDB), filmographie complète cliquable, et
  récompenses/nominations (via Wikidata, TMDB n'ayant aucune donnée sur le sujet).
- **Distribution et réalisation** — sur chaque fiche série/film, la distribution et
  l'équipe technique (réalisateur, scénariste, compositeur, créateur) sont cliquables
  et renvoient vers la fiche de la personne.
- **Import depuis TV Time** — reprend l'export de données TV Time (séries, films,
  historique de visionnage) et l'ajoute automatiquement à la bibliothèque via
  correspondance TMDB.
- **Statuts fonctionnels** — en cours, terminé, à voir, en pause, abandonné — plus un
  drapeau **favori** indépendant du statut de visionnage.
- **Statistiques** — temps regardé (avec équivalent mois/jours/heures), nombre
  d'épisodes/films vus, graphiques hebdo/mensuel/jour-de-la-semaine tapables pour voir
  la valeur exacte, répartition par genre/chaîne/statut de diffusion — chaque
  répartition s'ouvre en plein écran pour voir la liste complète des titres concernés.
- **Synchronisation multi-appareils** — via un compte (email/mot de passe ou Google),
  la bibliothèque, les statuts vu/non-vu et la clé API TMDB se synchronisent
  automatiquement entre deux appareils via Firebase.
- **Réinitialisation de bibliothèque**, **notes de version** et **guide d'utilisation**
  consultables directement dans l'appli (Profil).
- **Annonces in-app** — un message important (édité directement sur GitHub, sans nouvelle
  version de l'appli) peut s'afficher en bannière ou en popup au lancement.
- **Français / English** — l'interface est entièrement traduite dans les deux langues,
  y compris le format des dates selon la région.

Voir [`docs/guide/fr.md`](https://github.com/Xenovyrion/reelia-content/blob/main/docs/guide/fr.md)
([`en`](https://github.com/Xenovyrion/reelia-content/blob/main/docs/guide/en.md)) pour le guide
d'utilisation détaillé (premiers pas, fonctionnement de la synchro, confidentialité) — le
même contenu que l'écran Aide de l'appli.

Le code source de l'appli vit ici ; les notes de version, le guide, les annonces in-app et
les APK publiés vivent dans [`reelia-content`](https://github.com/Xenovyrion/reelia-content),
un dépôt public séparé — ça permet à ce dépôt-ci de passer privé sans casser la mise à jour
auto, les notes de version, le guide ou les annonces (qui vont tous chercher leur contenu sur
GitHub sans authentification, donc ont besoin d'un dépôt public).

## Stack technique

| Domaine | Bibliothèque |
| --- | --- |
| Langage | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Injection de dépendances | Hilt |
| Persistance locale | Room |
| Réseau | Retrofit + OkHttp + kotlinx.serialization |
| Images | Coil |
| Auth + sync cloud | Firebase Authentication + Cloud Firestore |
| Connexion Google | Credential Manager + Google ID |
| Données | [TMDB API](https://www.themoviedb.org/) |

Architecture MVVM + Repository : ViewModels exposent des `StateFlow` consommés par
Compose, les repositories arbitrent entre Room (source de vérité locale) et TMDB/Firestore
(remote), et un outbox de synchronisation (`SyncOutboxEntity`) garantit qu'aucune
modification locale n'est perdue si l'appareil est hors-ligne au moment du changement.

## Prérequis pour builder

- Android Studio (dernière version stable) ou JDK 17 + Android SDK (compileSdk 35,
  minSdk 26) installés en local.
- Une clé API TMDB personnelle, gratuite : créer un compte sur
  [themoviedb.org](https://www.themoviedb.org/), puis générer une clé API v3 dans les
  paramètres du compte. Elle se saisit dans l'écran **Réglages** de l'app (stockée en
  local via DataStore, et synchronisée dans Firestore une fois connecté — jamais
  committée dans le repo).
- Un projet Firebase (offre gratuite Spark) avec Authentication (Email/Password, et
  Google si tu veux ce mode de connexion) et Cloud Firestore activés. Le
  `google-services.json` du projet doit être placé dans `app/`. App Check (Play
  Integrity en release, provider debug en debug) protège Auth/Firestore contre les
  clients non légitimes — le token debug se récupère directement dans l'appli
  (Profil > App Check, uniquement en debug) pour l'enregistrer côté console Firebase.
- Pour que la CI publie les releases sur `reelia-content` : un secret de dépôt
  `CONTENT_REPO_TOKEN` (PAT fine-grained, accès limité à `reelia-content`,
  permission Contents: Read and write).

## Build

```
./gradlew assembleDebug
```

La CI GitHub Actions compile chaque push/PR vers `main` et publie l'APK debug le plus
récent en tant que [release "debug-latest"](https://github.com/Xenovyrion/reelia-content/releases/tag/debug-latest)
sur `reelia-content`. Un tag `vX.Y.Z` poussé sur ce dépôt publie en plus une vraie
release versionnée (non-prerelease) au même endroit, que l'appli détecte via son
vérificateur de mise à jour intégré.

## État actuel

Application fonctionnelle de bout en bout : ajout de séries/films, suivi épisode par
épisode, import depuis TV Time, statistiques détaillées, synchronisation complète
(bibliothèque, statuts, journal de visionnage, clé API) entre appareils via compte
Firebase. Voir [`docs/release-notes/fr.md`](https://github.com/Xenovyrion/reelia-content/blob/main/docs/release-notes/fr.md)
pour le détail des versions.
