# Politique de confidentialité

Dernière mise à jour : 10 octobre 2026

**⚠️ Ce document est un brouillon, pas un avis juridique.** Il décrit fidèlement ce que
l'application fait réellement aujourd'hui (vérifié directement dans le code), mais doit être
relu avant toute publication réelle - en particulier pour compléter les champs marqués
`[À COMPLÉTER]`.

## Qui est responsable du traitement de tes données ?

`[À COMPLÉTER : nom ou raison sociale, adresse e-mail de contact]`

## Quelles données sont collectées ?

- **Compte** : ton adresse e-mail (connexion par e-mail/mot de passe) ou les informations
  minimales de ton compte Google nécessaires à l'authentification (connexion par Google) -
  gérées par Firebase Authentication (Google LLC).
- **Bibliothèque** : les séries et films que tu suis, ton statut de visionnage, tes notes
  personnelles, tes favoris et ton historique de visionnage - stockés dans Cloud Firestore
  (Google LLC), associés uniquement à ton compte.
- **Clé API TMDB** : la clé personnelle que tu fournis pour interroger TMDB est stockée
  (localement et synchronisée via Firestore) pour fonctionner sur tous tes appareils.
- **Sauvegarde Google Drive (optionnelle)** : si tu actives cette fonction, une copie de ta
  bibliothèque est déposée dans un dossier caché de ton propre compte Google Drive (scope
  `drive.appdata`) - jamais transmise à Reelia ni à un tiers autre que Google.
- **Import TV Time (optionnel)** : le fichier d'export que tu fournis toi-même est lu
  uniquement sur ton appareil pour peupler ta bibliothèque ; il n'est envoyé à aucun serveur.

## Ce qui n'est pas collecté

Aucune mesure d'audience, aucun outil publicitaire, aucun tracker tiers. L'application
n'utilise ni Firebase Analytics, ni Crashlytics, ni aucun SDK publicitaire.

## Avec qui tes données sont-elles partagées ?

- **Google / Firebase** (Authentication, Cloud Firestore, App Check) : sous-traitant
  technique nécessaire au fonctionnement de l'appli.
- **TMDB (The Movie Database)** : tes recherches de titres sont envoyées à TMDB avec ta clé
  API personnelle pour récupérer les informations publiques du film/de la série - TMDB ne
  reçoit aucune donnée sur ta bibliothèque personnelle.

Aucune donnée n'est vendue, louée, ni partagée à des fins publicitaires.

## Combien de temps tes données sont-elles conservées ?

Tant que ton compte existe. Tu peux supprimer ton compte et l'ensemble des données associées
directement depuis l'application (Profil > Supprimer le compte).

## Tes droits (RGPD)

Si tu es dans l'Union européenne, tu disposes d'un droit d'accès, de rectification,
d'effacement et de portabilité de tes données. L'accès et la suppression se font directement
dans l'application ; pour toute autre demande, contacte `[À COMPLÉTER : e-mail de contact]`.

## Sécurité

Les règles d'accès Cloud Firestore isolent strictement les données de chaque compte (aucun
accès public possible). Les builds publiés sont protégés par Google Play Integrity (App
Check) contre les clients non légitimes.

## Évolutions futures

Cette politique concerne l'état actuel, entièrement gratuit, de l'application. Si une offre
payante est introduite à l'avenir, ce document sera mis à jour en conséquence avant son
lancement.
