# Privacy Policy

Last updated: October 10, 2026

**⚠️ This document is a draft, not legal advice.** It accurately describes what the app
actually does today (verified directly in the code), but should be reviewed before any real
publication - in particular to fill in the fields marked `[TO FILL IN]`.

## Who is responsible for processing your data?

`[TO FILL IN: name or company name, contact email address]`

## What data is collected?

- **Account**: your email address (email/password sign-in) or the minimal Google account
  information needed for authentication (Google sign-in) - handled by Firebase
  Authentication (Google LLC).
- **Library**: the shows and movies you track, your watch status, personal ratings,
  favorites, and watch history - stored in Cloud Firestore (Google LLC), tied only to your
  account.
- **TMDB API key**: the personal key you provide to query TMDB is stored (locally and
  synced via Firestore) so it works across all your devices.
- **Google Drive backup (optional)**: if you enable this feature, a copy of your library is
  stored in a hidden folder of your own Google Drive account (`drive.appdata` scope) - never
  sent to Reelia or to any third party other than Google.
- **TV Time import (optional)**: the export file you provide yourself is read only on your
  device to populate your library; it is never sent to any server.

## What is not collected

No audience measurement, no advertising tools, no third-party trackers. The app uses
neither Firebase Analytics nor Crashlytics nor any advertising SDK.

## Who is your data shared with?

- **Google / Firebase** (Authentication, Cloud Firestore, App Check): technical processor
  required for the app to function.
- **TMDB (The Movie Database)**: your title searches are sent to TMDB together with your
  personal API key to fetch public show/movie information - TMDB never receives any data
  about your personal library.

No data is sold, rented, or shared for advertising purposes.

## How long is your data kept?

For as long as your account exists. You can delete your account and all associated data
directly from the app (Profile > Delete account).

## Your rights (GDPR)

If you are in the European Union, you have the right to access, rectify, erase, and port
your data. Access and deletion are available directly in the app; for any other request,
contact `[TO FILL IN: contact email]`.

## Security

Cloud Firestore access rules strictly isolate each account's data (no public access
possible). Published builds are protected by Google Play Integrity (App Check) against
illegitimate clients.

## Future changes

This policy covers the app's current, entirely free state. If a paid offering is
introduced in the future, this document will be updated accordingly before launch.
