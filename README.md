# Reelia

*[Read in English](README.en.md)*

![Dernière version](https://img.shields.io/github/v/release/Xenovyrion/Reelia?label=derni%C3%A8re%20version&color=8C8FFF)
![Licence](https://img.shields.io/badge/licence-propri%C3%A9taire-lightgrey)
![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)

Reelia est une application Android personnelle de suivi de séries et films, pensée comme un
remplaçant de TV Time : une bibliothèque unique, synchronisée entre tous tes appareils, pour
suivre ce que tu regardes épisode par épisode plutôt que série par série.

## Fonctionnalités

- **Suivi épisode par épisode**, avec rattrapage automatique des épisodes précédents non vus
  d'une même saison.
- **Accueil** : suite de visionnage, suggestions basées sur ta bibliothèque, tendances du
  moment, derniers films/séries sortis.
- **Agenda** : toutes les sorties à venir groupées par date, avec rappels configurables et un
  widget d'écran d'accueil.
- **Statistiques détaillées** : temps regardé, genres/chaînes préférés, graphiques hebdo et
  mensuels, récap annuel façon "Wrapped".
- **Notes personnelles** par épisode et par film, avec moyennes saison/série calculées
  automatiquement.
- **Recherche par titre ou par acteur/actrice**, avec filtres (genre, année, note, langue) et
  bandes-annonces lisibles directement dans l'app.
- **Import depuis TV Time** de ta bibliothèque existante.
- **Synchronisation multi-appareils** via un compte (email/mot de passe ou Google).
- **Sauvegarde manuelle ou sur Google Drive**, en plus de la synchro cloud.
- **Français / English** : interface entièrement traduite dans les deux langues.

Toutes les informations sur les séries et films (affiches, résumés, casting, notes) viennent de
[TMDB](https://www.themoviedb.org/), une base de données communautaire gratuite. *This product
uses the TMDB API but is not endorsed or certified by TMDB.*

Reelia est développée par une seule personne, sans publicité ni collecte de données à des fins
commerciales.

## Télécharger

Dernière version APK sur la [page Releases](https://github.com/Xenovyrion/Reelia/releases)
(Android 8.0 ou plus récent). L'app détecte elle-même les mises à jour suivantes, pas besoin de
revenir ici à chaque fois.

## Guide d'utilisation

Toute la documentation (premiers pas, fonctionnement de la synchro, confidentialité) est sur le
[wiki GitHub](https://github.com/Xenovyrion/Reelia/wiki) - le même contenu que l'écran Aide de
l'application.

## Signaler un bug ou proposer une idée

Directement depuis l'app (Réglages > Aide et retours), ou via une
[nouvelle issue](https://github.com/Xenovyrion/Reelia/issues/new/choose).

## Confidentialité

Voir [`docs/privacy-policy/fr.md`](docs/privacy-policy/fr.md)
([`en`](docs/privacy-policy/en.md)).

## Structure de ce dépôt

Ce dépôt héberge le contenu public de l'app et son canal de distribution ; le code source vit
dans un dépôt séparé, privé. Celui-ci reste public pour que certaines choses continuent de
fonctionner sans authentification :

- `docs/release-notes/{fr,en}.md` - notes de version affichées dans l'app, récupérées en direct
  depuis GitHub.
- `docs/about/{fr,en}.md` - contenu de l'écran "À propos" dans l'app, récupéré en direct.
- `docs/announcement.json` - message de bannière/popup in-app, modifié directement ici pour
  diffuser un message à toutes les installations sans nouvelle version de l'app.
- Releases GitHub sur ce dépôt - APK téléchargeables (version de release), les assets de
  release nécessitant un dépôt public pour un téléchargement anonyme.
- Wiki GitHub - guide d'utilisation détaillé.

Modifier les fichiers ci-dessus directement sur GitHub (interface web ou un clone) met à jour
ce que l'app affiche - aucune mise à jour de l'app nécessaire.
