# Guide d'utilisation — Reelia

Reelia est une application personnelle de suivi de séries et films, pensée comme un
remplaçant de TV Time. Ce guide explique les points qui ne sont pas évidents en utilisant
l'app au quotidien.

## Premiers pas

1. **Clé API TMDB** — Reelia utilise [TMDB](https://www.themoviedb.org/) pour toutes les
   infos (posters, synopsis, épisodes...). Crée un compte gratuit sur TMDB, génère une clé
   API v3 dans les paramètres de ton compte, puis colle-la dans **Réglages** au premier
   lancement.
2. **Compte Reelia** — un compte email/mot de passe (ou Google) te permet de synchroniser
   ta bibliothèque entre plusieurs appareils.

## Comment fonctionne la synchronisation

Quand tu es connecté, Reelia synchronise automatiquement via Firebase :

- les séries/films que tu suis ;
- le statut vu/non-vu de chaque épisode et film ;
- ton historique de visionnage (utilisé pour les statistiques) ;
- ta clé API TMDB, pour ne pas avoir à la ressaisir sur un nouvel appareil.

**Point important après une réinstallation** : au premier lancement sur un appareil neuf
(ou après une réinstallation), connecte-toi avec ton compte. Si ta clé API TMDB était déjà
synchronisée depuis un autre appareil, elle s'importe automatiquement et ta bibliothèque se
resynchronise dans la foulée. Si ce n'est pas encore le cas (première connexion jamais
faite avant), ressaisis ta clé une fois dans Réglages : cela relance la synchro et rapatrie tout
ce qui était déjà enregistré sur Firebase.

## Écran d'accueil

L'accueil est un hub de découverte, pas une deuxième bibliothèque : il propose la suite
de visionnage, des suggestions basées sur tes favoris/ajouts récents, les tendances du
moment, et les derniers films/séries sortis (via TMDB, gratuit). Un titre cliqué depuis
l'accueil n'est pas forcément déjà dans ta bibliothèque — ça ouvre sa fiche TMDB avec un
bouton pour l'ajouter, comme depuis Rechercher.

## Marquer des épisodes comme vus

- **Cocher un épisode** rattrape automatiquement tous les épisodes précédents non vus de
  la même saison (ex : cocher l'épisode 10 alors que 1 à 9 ne l'étaient pas coche aussi
  1 à 9). Pratique pour un rattrapage rapide.
- **Décocher** un épisode n'affecte que celui-là.
- Pour cocher/décocher un seul épisode sans ce rattrapage automatique (par exemple pour
  marquer un épisode isolé sans toucher aux autres), fais un **appui long** sur sa coche.
- La coche en haut de la liste d'épisodes (à côté de "X / Y épisodes vus") marque ou
  démarque toute la saison d'un coup, dans les deux sens.

## Confidentialité

- Ta bibliothèque et ta clé API sont stockées dans un document Firestore accessible
  uniquement par ton compte (règles de sécurité Firebase — personne d'autre ne peut y
  accéder, même avec l'URL du projet).
- Ton mot de passe n'est jamais stocké par l'app : il est envoyé à Firebase Authentication,
  qui le gère de bout en bout (l'app ne le voit jamais en clair après la saisie).

## Statuts et couleurs

Chaque couleur a un sens fixe dans toute l'app :

- **Sarcelle (teal)** — en cours de visionnage / terminé
- **Ambre** — planifié / en pause
- **Périwinkle** — à voir plus tard
- **Corail** — favori (indépendant du statut de visionnage)

## Problèmes connus / limites

- L'historique de visionnage enregistré *avant* la mise à jour qui a ajouté la synchro du
  journal ne se synchronise pas rétroactivement — seules les nouvelles entrées le font.
- La connexion Google nécessite que le fournisseur Google soit activé côté Firebase ; sans
  ça, le bouton affichera une erreur au lieu de fonctionner.
