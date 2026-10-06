# Notes de version

## 0.24.0 — 6 octobre 2026
### ✨ Nouveautés
- Agenda : depuis Accueil > À venir, un nouveau bouton ouvre un écran listant toutes tes prochaines sorties (épisodes et films), groupées par date, en plus du bandeau existant
### 🐛 Corrections
- Un épisode ou un film déjà diffusé pouvait rester affiché indéfiniment dans "À venir" et le widget si les notifications de sortie n'étaient pas activées — la mise à jour des dates ne dépend plus de ce réglage
- Cocher un épisode qui rattrape automatiquement plusieurs épisodes précédents non vus l'indique maintenant clairement, au lieu de le faire silencieusement
- Retirer un titre depuis les résultats de recherche demande maintenant une confirmation, comme c'était déjà le cas depuis sa fiche détaillée
- Ajouter un titre depuis la fiche d'un·e acteur·rice ne laisse plus un écran de prévisualisation obsolète dans l'historique de navigation

## 0.23.1 — 20 septembre 2026
### 🐛 Corrections
- Après une réinstallation ou un changement d'appareil avec une bibliothèque déjà bien remplie, certaines séries pouvaient se resynchroniser avec leurs saisons visibles mais une liste d'épisodes vide — corrigé, avec réparation automatique au lancement pour les séries déjà touchées par ce problème

## 0.23.0 — 6 août 2026
### ✨ Nouveautés
- Widget écran d'accueil : ajoute le widget Reelia sur ton écran d'accueil pour voir en un coup d'œil tes 5 prochaines sorties (séries et films), avec affiche et compte à rebours ; un appui sur un titre ouvre directement sa fiche, et un bouton dédié permet de le rafraîchir manuellement

## 0.22.0 — 4 août 2026
### ✨ Nouveautés
- Profil > Sauvegarde : exporte toute ta bibliothèque (séries, films, historique de visionnage) dans un fichier JSON, et restaure-la à tout moment depuis ce fichier — un filet de sécurité indépendant de la synchronisation cloud, utile aussi pour migrer vers un nouveau compte
- Rappels de sortie : active les notifications dans Profil > Paramètres pour être alerté quand un film ou le prochain épisode d'une série suivie approche, avec des délais configurables (30, 14, 8, 1 jour avant, ou le jour même)

## 0.21.1 — 31 juillet 2026
### 🔒 Sécurité
- L'application est désormais compilée et signée avec une clé de publication dédiée, séparée de la clé de développement — auparavant, la version distribuée utilisait encore la clé de debug
- Avant d'installer une mise à jour téléchargée, l'application vérifie maintenant son empreinte cryptographique (SHA-256) fournie par GitHub ; en cas de fichier corrompu ou d'empreinte manquante, l'installation est bloquée

## 0.21.0 — 16 juillet 2026
### 🐛 Corrections
- Fiches de prévisualisation (recherche, tendances de l'accueil, filmographie d'un acteur) : un titre déjà dans ta bibliothèque affiche maintenant "Ajouté" et t'emmène vers ta fiche suivie, au lieu de proposer de l'ajouter à nouveau
- Statistique "temps regardé" : les épisodes et films dont TMDB ne donne aucune durée étaient comptés comme 0 minute au lieu d'une estimation, ce qui sous-évaluait discrètement le total (particulièrement visible après un import TV Time) — corrigé pour les prochains visionnages, et ton historique déjà enregistré a été corrigé rétroactivement

## 0.20.2 — 15 juillet 2026
### 🐛 Corrections
- Fiches de prévisualisation (avant ajout, depuis la recherche ou depuis la filmographie d'un acteur) : cliquer sur un acteur ne faisait rien — ça ouvre maintenant bien sa fiche
- Accueil, section "Continuer" : une série dont le seul épisode restant à voir fait partie d'une saison annoncée mais pas encore sortie n'apparaît plus dans cette section

## 0.20.1 — 15 juillet 2026
### 🐛 Corrections
- La mise à jour vers la 0.20.0 déclenchait par erreur une réinitialisation complète du cache local de la bibliothèque, forçant une resynchronisation intégrale depuis le cloud et un nouveau téléchargement de chaque série/film depuis TMDB — c'est très probablement ce qui causait le premier affichage très lent de l'accueil et les catégories qui n'apparaissaient qu'après ~20 secondes. Corrigé : la mise à jour vers cette version ne touche plus à tes données locales
### 🔧 Améliorations
- Accueil : les sections Films, Séries et Suggestions se chargent maintenant chacune indépendamment au lieu d'attendre que toutes soient prêtes avant de s'afficher

## 0.20.0 — 15 juillet 2026
### ✨ Nouveautés
- Accueil : les sections "Tendances du moment", "Derniers films sortis" et "Dernières séries sorties" sont remplacées par deux sections **Films** et **Séries**, chacune avec un menu déroulant à côté du titre pour choisir Populaire, Mieux notés, À venir ou Du moment
- Fiches films/séries (et écrans de prévisualisation avant ajout) : affichage de la classification d'âge (ex. "16", "PG-13", "TV-MA") à côté de la note, quand TMDB fournit l'info
### 🔧 Améliorations
- Chargement des affiches et images avec un fondu enchaîné au lieu d'une apparition brutale, pour un défilement plus fluide

## 0.19.0 — 14 juillet 2026
### ✨ Nouveautés
- Accueil : un bouton "+" apparaît maintenant sur les sections "Suggestions pour toi", "Tendances du moment", "Derniers films sortis" et "Dernières séries sorties" pour ajouter un titre directement à ta bibliothèque sans quitter l'écran d'accueil
### 🔧 Améliorations
- Accueil : les titres déjà présents dans ta bibliothèque n'apparaissent plus dans ces 4 sections, et disparaissent automatiquement dès leur ajout

## 0.18.0 — 14 juillet 2026
### 🔧 Améliorations
- Fiche d'une série, onglet épisodes : une saison pas encore sortie garde maintenant sa liste complète d'épisodes (fini le message qui la remplaçait) — un petit indicateur "Saison à venir" apparaît entre le compteur d'épisodes et le bouton "tout cocher", et chaque épisode affiche son propre compte à rebours (ou "Date inconnue" si TMDB n'a pas encore communiqué la date)
- Recherche : quand tu ajoutes plusieurs titres à la suite, le ✓ affiché sur un titre déjà ajouté est maintenant cliquable et le retire de ta bibliothèque — pratique pour annuler un ajout par erreur sans changer d'écran
- Recherche : les tendances n'affichent plus les titres déjà présents dans ta bibliothèque
- Recherche : une vraie recherche par texte affiche maintenant un "+" si le titre n'est pas dans ta bibliothèque, ou un ✓ cliquable (pour le retirer) s'il y est déjà

## 0.17.0 — 14 juillet 2026
### ✨ Nouveautés
- Depuis la recherche, le bouton "+" rapide n'ouvre plus la fiche du titre : tu restes sur l'écran de recherche pour enchaîner les ajouts, et un ✓ remplace le "+" une fois le titre ajouté
- Fiche d'une série, onglet épisodes : une saison pas encore sortie affiche maintenant "Saison pas encore sortie" au lieu d'une liste vide ; le prochain épisode à regarder affiche "Épisode à venir" avec le nombre de jours restants s'il n'est pas encore diffusé ; chaque épisode à venir affiche maintenant son propre compte à rebours
### 🐛 Corrections
- Recherche dans ma bibliothèque : la recherche restait active en arrière-plan après avoir quitté l'écran (fiche détail, autre onglet), filtrant silencieusement la liste sans que le champ de recherche soit visible pour l'expliquer — elle est maintenant réinitialisée à chaque sortie de l'écran
### 🔧 Améliorations
- Le tri "ajout récent"/"dernier vu" affiche maintenant un vrai libellé de section (comme les autres tris) au lieu d'un simple espace

## 0.16.1 — 14 juillet 2026
### 🐛 Corrections
- Recherche dans ma bibliothèque : impossible d'effacer le texte saisi dans certains cas — le champ réagit maintenant instantanément à la saisie et un bouton "X" permet de tout effacer d'un coup
- Retour après ajout d'un titre depuis la recherche : navigation rendue plus fiable pour atterrir sur l'onglet Séries/Films au lieu de la fiche de recherche
- Tri "ajout récent"/"dernier vu" : les éléments n'étaient plus séparés visuellement de la section "À venir"
### 🔧 Améliorations
- Les boutons "rechercher dans ma bibliothèque" et "rechercher sur TMDB pour ajouter" ont maintenant des icônes bien distinctes (loupe vs "+") pour éviter la confusion

## 0.16.0 — 14 juillet 2026
### ✨ Nouveautés
- Nouvelle recherche locale dans Séries/Films : recherche par texte dans ta bibliothèque (titre ou nom d'épisode), à ne pas confondre avec la recherche TMDB déjà existante qui sert à trouver de nouveaux titres à ajouter
### 🐛 Corrections
- Après avoir ajouté une série ou un film depuis la recherche, le bouton retour du téléphone ramenait sur l'écran de recherche au lieu de revenir sur l'onglet Séries/Films
- Le cercle de progression sur les affiches en grille n'avait pas le même fond sombre que celui de l'accueil, ce qui le rendait difficile à voir sur une affiche claire
- Tri "Dernier vu" : une série tout juste ajoutée pouvait apparaître en premier si un ancien historique de visionnage existait déjà pour elle (série supprimée puis réajoutée, ou import TV Time) — seul un visionnage postérieur à l'ajout compte désormais pour ce tri
### 🔧 Améliorations
- Le bouton "Réinitialiser" du panneau filtre/tri applique maintenant directement la réinitialisation au lieu d'attendre un appui supplémentaire sur "Appliquer"

## 0.15.2 — 14 juillet 2026
### 🐛 Corrections
- Le bouton "Réinitialiser" du panneau filtre/tri ne remettait pas l'ordre de tri à sa valeur par défaut, seulement les filtres de statut/genre
- Panneau filtre/tri : sur les petits écrans, le bouton "Appliquer" pouvait être hors d'atteinte tout en bas sans possibilité de défiler jusqu'à lui — le panneau a maintenant une hauteur fixe avec défilement interne, les boutons Réinitialiser/Appliquer restent toujours visibles
- Retour en haut d'écran : l'animation de défilement donnait l'impression que toute la liste "s'envolait" vers le haut sur les longues listes — le retour est maintenant instantané
### ✨ Nouveautés
- Nouvel ordre de tri "Dernier vu" dans Séries/Films, basé sur l'historique de visionnage
- Le tri alphabétique regroupe maintenant les titres par lettre (avec un groupe "0-9" pour les titres commençant par un chiffre) au lieu d'une longue liste indifférenciée
### 🔧 Améliorations
- Les tris par statut, genre, ajout récent et dernier vu appliquent maintenant un tri alphabétique secondaire au sein de chaque groupe
- Affichage en grille dans Séries/Films optimisé (meilleure réactivité sur les longues bibliothèques)
- Message d'erreur App Check plus précis : distingue maintenant une limite de fréquence ("too many attempts") d'un blocage de l'API côté Google Cloud, avec une explication adaptée à chaque cas

## 0.15.1 — 14 juillet 2026
### 🐛 Corrections
- Le retour en haut de l'écran en tapant sur un onglet en bas ne fonctionnait toujours pas dans certains cas (par exemple Profil → Séries → retour à Profil) — la correction précédente ne suffisait pas, le mécanisme a été refait
- La mise à jour restait bloquée sans rien afficher après avoir cliqué sur "Mettre à jour" en cas d'échec du téléchargement — le message d'erreur, déjà calculé mais jamais affiché, apparaît maintenant à l'écran
### ✨ Nouveautés
- Ajout d'un indicateur de chargement (icône + texte "Téléchargement…") pendant la mise à jour, pour ne plus donner l'impression que rien ne se passe
### 🔧 Améliorations
- Le tri (statut, alphabétique, genre, ajout récent) est maintenant intégré au bouton de filtre dans Séries/Films au lieu d'avoir deux boutons quasi identiques dans la barre du haut
- Bouton App Check (debug) : un délai de 60 secondes après un échec évite d'aggraver l'erreur "too many attempts" de Firebase en réessayant trop vite, et le message d'erreur est maintenant traduit et plus clair. Cette limite vient du serveur de Firebase lui-même et ne peut pas être supprimée côté application — seulement mieux gérée

## 0.15.0 — 14 juillet 2026
### 🐛 Corrections
- Correctif important : la suppression de compte effaçait déjà toutes les données (bibliothèque, historique) avant de demander une reconnexion récente si nécessaire — en cas de session pas assez récente, les données étaient perdues alors que le compte, lui, survivait. La reconnexion est désormais toujours demandée avant de toucher à quoi que ce soit, que le compte soit en mot de passe ou Google
- Le lien d'import TV Time pouvait s'ouvrir dans l'application TV Time elle-même au lieu du navigateur — il force maintenant l'ouverture dans le navigateur par défaut
- En tapant sur un onglet en bas, l'écran ne revenait pas toujours en haut — c'est maintenant systématique
### ✨ Nouveautés
- Nouvel onglet "Trier" dans Séries/Films : par statut, ordre alphabétique, genre ou ajout récent
- Suivi des épisodes spéciaux ("Spécials", saison 0) — sans qu'ils comptent dans le pourcentage de complétion d'une série
- Le bouton "En savoir plus" affiche maintenant une présentation de l'application, distincte du guide d'utilisation
### 🔧 Améliorations
- L'onglet Profil est réorganisé en deux sous-onglets, Paramètres et Statistiques, au lieu d'un seul long écran
- La sélection de langue ne propose plus que français/anglais, les seules langues réellement traduites
- La section "Source de données" est masquée tant qu'il n'y a qu'un seul fournisseur (TMDB)
- Email de vérification à l'inscription désactivé temporairement (partait en spam sans nom de domaine dédié)
- Le titre des releases GitHub inclut maintenant le numéro de version

## 0.14.0 — 14 juillet 2026
### ✨ Nouveautés
- Suppression de compte plus simple à sécuriser : si Firebase demande une reconnexion récente, l'appli propose maintenant de confirmer directement avec ton mot de passe (ou une reconnexion Google) au lieu de devoir te déconnecter puis te reconnecter
- Indicateur de force du mot de passe à l'inscription, avec un minimum de 8 caractères exigé
- Bouton œil sur les champs mot de passe pour voir ce que tu as saisi
- Email de vérification envoyé automatiquement à l'inscription
- Le bouton "En savoir plus" de l'écran de connexion ouvre maintenant une présentation dans l'appli (même forme que le guide) au lieu d'une page GitHub
### 🐛 Corrections
- Après une déconnexion ou une suppression de compte, l'écran de connexion gardait l'email et le mot de passe précédemment saisis au lieu d'être vide

## 0.13.3 — 14 juillet 2026
### ✨ Nouveautés
- L'accueil affiche maintenant deux sections "Séries favorites" et "Films favoris" — jusqu'ici, mettre un titre en favori ne se voyait nulle part
### 🐛 Corrections
- Après une réinitialisation de bibliothèque, les statistiques en haut de l'onglet Profil pouvaient afficher des chiffres incohérents pendant une fraction de seconde avant de retomber à zéro
### 🔧 Améliorations
- Écran de recherche : titre et année centrés sous l'affiche, sans ligne vide inutile quand le titre tient sur une seule ligne (jusqu'à 3 lignes pour les titres longs)
- Onglet Profil : les boutons "Enregistrer", "Rechercher les mises à jour" et "Notes de version" sont maintenant centrés ; le bouton "Aide" est désormais dans la barre du haut, visible sans avoir à faire défiler jusqu'en bas
- Retrait du code de diagnostic temporaire ajouté pour identifier le plantage corrigé en 0.13.1

## 0.13.2 — 14 juillet 2026
### 🐛 Corrections
- Le badge "Actuelle" des notes de version restait bloqué sur la dernière version publiée au lieu de suivre la version réellement installée — il suit maintenant ta version, et les versions plus récentes affichent un badge distinct "Nouvelle version"
- À l'ouverture de l'appli, un texte "bibliothèque vide" pouvait s'afficher brièvement le temps que les suggestions se chargent — l'accueil garde maintenant l'icône de chargement animée jusqu'à ce que le contenu soit prêt
### 🔧 Améliorations
- Nettoyage de quelques éléments techniques dépréciés (icônes, API) signalés par Android Studio, sans impact visible

## 0.13.1 — 14 juillet 2026
### 🐛 Corrections
- L'appli pouvait planter juste après la connexion sur une bibliothèque toute juste réinitialisée, avant que la clé API TMDB n'ait eu le temps de se resynchroniser
### 🔧 Améliorations
- Geste de retour (balayage depuis le bord) pris en charge nativement par Android
- Nettoyage interne du nom de l'appli pour qu'il soit cohérent partout

## 0.13.0 — 14 juillet 2026
### ✨ Nouveautés
- Annonces in-app : un message important peut s'afficher en bannière ou en popup au lancement, publié directement sur GitHub sans nouvelle version de l'appli
- Nouvel écran **Aide** (Profil) : le guide d'utilisation est désormais consultable dans l'appli, mis en forme en sections colorées plutôt que du texte brut
- Retirer un film ou une série de la bibliothèque directement depuis sa fiche (bouton corbeille, avec confirmation)
### 🐛 Corrections
- Le bouton d'ajout depuis la recherche amène maintenant directement sur la fiche du titre ajouté ; un retour arrière renvoie à l'accueil série/film plutôt qu'à l'écran de recherche
- Réinitialiser la bibliothèque puis désinstaller/réinstaller l'appli pouvait faire réapparaître d'anciennes séries et te reconnecter automatiquement — la sauvegarde automatique d'Android, qui restaurait une ancienne copie locale, est désormais désactivée
### 🔧 Améliorations
- La flèche retour a une présentation plus soignée sur tous les écrans concernés
- Les mises à jour dans l'appli suivent désormais de vraies versions numérotées au lieu de se déclencher à chaque nouveau commit
- Protection renforcée des accès à l'authentification et à la base de données (Firebase App Check)

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
