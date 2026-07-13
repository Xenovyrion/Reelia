# Reelia

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![minSdk](https://img.shields.io/badge/minSdk-26-3DDC84?logo=android&logoColor=white)
![targetSdk](https://img.shields.io/badge/targetSdk-35-3DDC84?logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-Auth%20%2B%20Firestore-FFCA28?logo=firebase&logoColor=black)
![Version](https://img.shields.io/badge/version-0.10.0-8C8FFF)
![Android CI](https://github.com/Xenovyrion/TimeLine/actions/workflows/android-build.yml/badge.svg)

Reelia est une application Android personnelle de suivi de séries et films — un
remplaçant de TV Time, avec une bibliothèque synchronisée entre plusieurs appareils.

## Fonctionnalités

- **Accueil** — hub de découverte : suite de visionnage, suggestions basées sur ta
  bibliothèque (recommandations TMDB), tendances du moment, derniers films/séries
  sortis. 100% via l'API TMDB déjà utilisée par l'app, aucun service tiers payant.
- **Bibliothèque** — ajout de séries/films via recherche TMDB (poster, synopsis,
  saisons/épisodes récupérés automatiquement), suivi épisode par épisode. Cocher un
  épisode rattrape automatiquement les épisodes précédents non vus de la saison ; un
  appui long permet de cocher/décocher un épisode individuellement. Le clic sur un
  épisode ouvre une fenêtre de détail (image, résumé, date de diffusion, note).
- **Fiches acteurs/actrices** — biographie (avec repli automatique en anglais si la
  traduction française n'existe pas sur TMDB), filmographie complète cliquable.
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
- **Réinitialisation de bibliothèque** et **notes de version** consultables directement
  dans l'appli (Profil > Compte).
- **Français / English** — l'interface est entièrement traduite dans les deux langues,
  y compris le format des dates selon la région.

Voir [`docs/GUIDE.md`](docs/GUIDE.md) pour le guide d'utilisation détaillé (premiers
pas, fonctionnement de la synchro, confidentialité).

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
  `google-services.json` du projet doit être placé dans `app/`.

## Build

```
./gradlew assembleDebug
```

La CI GitHub Actions compile chaque push/PR vers `main` et publie l'APK debug le plus
récent en tant que [release "debug-latest"](https://github.com/Xenovyrion/TimeLine/releases/tag/debug-latest).

## État actuel

Application fonctionnelle de bout en bout : ajout de séries/films, suivi épisode par
épisode, import depuis TV Time, statistiques détaillées, synchronisation complète
(bibliothèque, statuts, journal de visionnage, clé API) entre appareils via compte
Firebase. Voir [`docs/release-notes/fr.md`](docs/release-notes/fr.md) pour le détail
des versions.
