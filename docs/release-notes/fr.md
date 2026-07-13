# Notes de version

## 0.12.0 — 13 juillet 2026
### ✨ Nouveautés
- L'onglet Bibliothèque redevient deux onglets distincts, Séries et Films, chacun avec son propre filtre, sa recherche et son affichage grille/liste — la recherche lancée depuis Séries ne porte que sur les séries, et inversement pour Films
- L'écran Recherche est entièrement redessiné pour ressembler au reste de l'appli (cartes affiches, sections), avec un bouton pour effacer la recherche et un filtre par genre
- L'accueil a maintenant un bouton recherche (portant sur séries et films) et une section "À venir", comme dans Séries/Films
### 🔧 Améliorations
- La recherche n'interroge plus l'API à chaque lettre tapée : délai augmenté et minimum de 2 caractères avant de lancer une requête

## 0.11.0 — 13 juillet 2026
### ✨ Nouveautés
- Les fiches acteur/actrice affichent maintenant une section Récompenses et nominations (via Wikidata, TMDB n'ayant aucune donnée sur le sujet)
- Les fiches série/film ont maintenant une section Réalisation cliquable (réalisateur, scénariste, compositeur, créateur) en plus de la Distribution — clique dessus pour ouvrir la fiche de la personne, comme pour un acteur
- La salutation d'accueil reflète maintenant l'heure de la journée et ton prénom si tu es connecté, au lieu d'un simple "Bonjour"
### 🐛 Corrections
- La mise à jour dans l'app pouvait échouer avec une erreur générique "problème avec le fichier de l'application" sans explication — un téléchargement raté est maintenant détecté et affiché comme une vraie erreur, avec possibilité de réessayer
- Les listes de découverte de l'accueil (et la filmographie de la fiche acteur) bougeaient pendant le défilement car la hauteur des cartes variait selon la longueur du titre — chaque carte réserve maintenant une hauteur constante
- Le cercle de progression et le texte d'épisode de "Continuer" pouvaient devenir illisibles sur une jaquette claire — les deux reposent maintenant sur un fond assombri pour rester lisibles quelle que soit l'image
### 🔧 Améliorations
- Vignettes distribution/réalisation élargies pour que les noms complets soient enfin lisibles au lieu d'être tronqués
- Les rôles de l'équipe technique (réalisateur, scénariste, compositeur, créateur) sont maintenant traduits au lieu d'afficher le terme anglais brut de TMDB

## 0.10.0 — 13 juillet 2026
### ✨ Nouveautés
- L'accueil devient un hub de découverte au lieu de dupliquer la bibliothèque : suggestions basées sur ta bibliothèque, tendances du moment, derniers films/séries sortis, en plus de "Continuer" — tout via TMDB, sans service tiers payant
- Les fiches acteur/actrice affichent maintenant la filmographie complète (crédits TMDB), avec des affiches cliquables vers chaque titre
### 🐛 Corrections
- Les dates (naissance/décès, sorties à venir) s'affichaient en format ISO brut au lieu de suivre la langue de l'appli — un appareil en français voyait encore "1955-01-18" au lieu de "18 janvier 1955"
- Un nom de personnage long sur une affiche de filmographie pouvait pousser l'année de sortie hors de l'écran
### 🔧 Améliorations
- Fiches acteur/actrice redessinées en cartes (biographie, filmographie), dans le même style que les fiches série/film
- La biographie d'un acteur/actrice bascule maintenant en anglais quand TMDB n'a pas de traduction française, au lieu d'afficher une biographie vide

## 0.9.0 — 13 juillet 2026
### ✨ Nouveautés
- Le clic sur un épisode ouvre une fenêtre de détail avec son image, son titre, sa date de diffusion, sa note et son résumé
- Cocher un épisode remplit automatiquement tous les épisodes non vus précédents de la saison (rattrapage) ; un appui long sur une coche permet de ne cocher/décocher que cet épisode individuellement
### 🐛 Corrections
- La coche "tout marquer vu" de la saison ne faisait que marquer vu — un appui dessus quand la saison est déjà entièrement vue la décoche maintenant entièrement
- Le bouton "vu" de la fenêtre de détail d'épisode pouvait rester bloqué sur un état obsolète et ne plus répondre aux appuis répétés
- La fenêtre de détail d'épisode pouvait masquer le bouton "vu" avec un long résumé — son contenu défile maintenant
### 🔧 Améliorations
- Les lignes d'épisodes redessinées en cartes avec un état "vu" plus lisible
- Les sections "À propos" des fiches série/film séparées en cartes (résumé, diffusion, casting, diffuseurs), dans le même style que l'écran de statistiques

## 0.8.0 — 13 juillet 2026
### ✨ Nouveautés
- Les graphiques (hebdo/mensuel/jour de la semaine) sont maintenant tapables pour afficher la valeur exacte d'une barre
- La répartition "Séries par statut" s'ouvre maintenant en détail, comme les genres et les chaînes
### 🐛 Corrections
- Le thème suivait le réglage clair/sombre du téléphone au lieu de toujours utiliser le thème sombre "Aubergine" — sur un téléphone en mode clair, l'appli s'affichait dans des couleurs très différentes et inachevées
- Les panneaux de détail par genre/chaîne étaient limités en hauteur et ne montraient pas toute la liste — ils s'ouvrent maintenant en plein écran, entièrement défilable
### 🔧 Améliorations
- Le classement des genres préférés passe de 5 à 10 genres affichés
- Les listes de détail (genre/chaîne/statut) sont maintenant triées par ordre alphabétique

## 0.7.0 — 13 juillet 2026
### ✨ Nouveautés
- Bouton "Réinitialiser la bibliothèque" dans Profil > Compte
- Version de l'application et notes de version consultables dans Réglages
### 🐛 Corrections
- L'écran d'import TV Time restait bloqué à 100% (la synchronisation cloud bloquait l'affichage du résumé)
- La réinitialisation de bibliothèque était très lente (suppression des données une par une)
### 🔧 Améliorations
- Boutons du profil alignés en grille uniforme
- Chiffres des statistiques plus lisibles (séparateurs de milliers, plus de débordement)
- Durée totale regardée affichée en mois/jours/heures
- Écran d'import TV Time : textes raccourcis, lien direct vers gdpr.tvtime.com, détail dépliable

## 0.6.0 — 13 juillet 2026
### ✨ Nouveautés
- Import de bibliothèque depuis TV Time : séries, films et historique de visionnage, avec correspondance automatique sur TMDB
### 🔧 Améliorations
- Explication plus claire du fichier à importer sur l'écran de TV Time

## 0.5.0 — 13 juillet 2026
### 🐛 Corrections
- Le statut d'une série/d'un film ne passait pas à "Terminé" une fois entièrement vu
- La statistique "Terminé" et le pourcentage de bibliothèque terminée restaient bloqués à 0
- La statistique "Séries en diffusion" restait bloquée à 0
### 🔧 Améliorations
- Statistiques par chaîne consultables en détail, comme pour les genres
- Graphiques et sections mieux aérés

## 0.4.0 — 12 juillet 2026
### ✨ Nouveautés
- Vérification et installation des mises à jour directement dans l'application
- Suppression de compte
### 🔧 Améliorations
- Écran de connexion redessiné (logo, dégradé, connexion Google)
- Synchronisation étendue aux séries/films suivis, épisodes vus et journal de visionnage
- Fusion des onglets Stats et Réglages en un seul onglet Profil
- Graphiques de statistiques enrichis (temps restant estimé, moyenne hebdomadaire, répartition par statut/chaîne/jour)

## 0.3.0 — 12 juillet 2026
### ✨ Nouveautés
- Nouveau thème "Aubergine" (couleurs, typographies, formes)
- Navigation à 4 onglets : Accueil, Bibliothèque, Stats, Réglages
- Écran d'accueil avec carrousel "Continuer le visionnage"
- Favoris sur les séries et films
- Connexion et synchronisation entre appareils via compte

## 0.2.0 — 12 juillet 2026
### ✨ Nouveautés
- Bande-annonce, casting cliquable et page dédiée pour chaque acteur/réalisateur
- Diffuseurs et statut de diffusion sur la fiche série
### 🔧 Améliorations
- Chargement de tous les épisodes dès l'ajout d'une série (progression fiable dès le départ)
- Fiches Séries et Films redessinées

## 0.1.0 — 11 juillet 2026
### ✨ Nouveautés
- Première version : suivi de séries et films via TMDB
- Bibliothèque, recherche, statistiques et réglages (français/anglais)
- Marquage des épisodes vus, y compris en masse par saison
