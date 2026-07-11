# TimeLine

Suivi de séries et films pour Android — remplaçant personnel de TV Time (Kotlin, Jetpack Compose, Material 3).

## Fonctionnement

- Bibliothèque locale (Room) : aucune donnée envoyée à un serveur perso.
- Ajout d'une série/film via une recherche TMDB (poster, synopsis, saisons/épisodes récupérés automatiquement).
- Suivi épisode par épisode, statut de visionnage, statistiques (temps regardé, nombre d'épisodes/films vus).

## Prérequis pour builder

- Android Studio (dernière version stable) ou JDK 17 + Android SDK (compileSdk 35, minSdk 26) installés en local.
- Une clé API TMDB personnelle, gratuite : créer un compte sur [themoviedb.org](https://www.themoviedb.org/), puis générer une clé API v3 dans les paramètres du compte. Elle se saisit dans l'écran **Réglages** de l'app (stockée uniquement sur l'appareil via DataStore, jamais committée dans le repo).

## Build

```
./gradlew assembleDebug
```

## État actuel

Squelette de projet + premier parcours de bout en bout (ajouter une série/film via TMDB → bibliothèque → cocher des épisodes vus → stats de base). Statuts de bibliothèque détaillés, calendrier "à venir", notes, et statistiques avancées (genres, récap annuel) arrivent dans une itération suivante.
