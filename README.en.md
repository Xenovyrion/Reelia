# Reelia

*[Lire en français](README.md)*

![Latest release](https://img.shields.io/github/v/release/Xenovyrion/Reelia?label=latest%20release&color=8C8FFF)
![License](https://img.shields.io/badge/license-proprietary-lightgrey)
![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)

Reelia is a personal Android app for tracking shows and movies, built as a replacement for
TV Time: a single library, synced across all your devices, to track what you watch episode by
episode rather than show by show.

## Features

- **Episode-by-episode tracking**, with automatic catch-up of earlier unwatched episodes in
  the same season.
- **Home**: continue-watching, suggestions based on your library, current trends, latest
  released movies/shows.
- **Agenda**: every upcoming release grouped by date, with configurable reminders and a home
  screen widget.
- **Detailed statistics**: time watched, favorite genres/networks, weekly and monthly charts, a
  "Wrapped"-style yearly recap.
- **Personal ratings** per episode and per movie, with season/show averages computed
  automatically.
- **Search by title or by actor/actress**, with filters (genre, year, rating, language) and
  trailers playable directly in the app.
- **Import from TV Time** of your existing library.
- **Multi-device sync** via an account (email/password or Google).
- **Manual or Google Drive backup**, in addition to cloud sync.
- **French / English**: the interface is fully translated in both languages.

All show and movie information (posters, synopses, cast, ratings) comes from
[TMDB](https://www.themoviedb.org/), a free community-driven database. *This product uses the
TMDB API but is not endorsed or certified by TMDB.*

Reelia is built by a single person, with no ads and no data collection for commercial purposes.

## Download

Latest APK on the [Releases page](https://github.com/Xenovyrion/Reelia/releases) (Android 8.0
or newer). The app detects further updates on its own, no need to come back here every time.

## User guide

Full documentation (getting started, how sync works, privacy) is on the
[GitHub wiki](https://github.com/Xenovyrion/Reelia/wiki) - the same content linked from the
app's Help screen.

## Report a bug or request a feature

Directly from the app (Settings > Help and feedback), or via a
[new issue](https://github.com/Xenovyrion/Reelia/issues/new/choose).

## Privacy

See [`docs/privacy-policy/en.md`](docs/privacy-policy/en.md)
([`fr`](docs/privacy-policy/fr.md)).

## Repo structure

This repo hosts the app's public-facing content and release channel; the app's source code
lives in a separate, private repo. This one stays public so a few things keep working without
authentication:

- `docs/release-notes/{fr,en}.md` - in-app release notes, fetched live from GitHub.
- `docs/about/{fr,en}.md` - in-app "About" screen content, fetched live from GitHub.
- `docs/announcement.json` - in-app banner/popup message, edited directly here to broadcast a
  message to every install without a new app version.
- GitHub Releases on this repo - downloadable APK builds (versioned releases), since Release
  assets require the hosting repo to be public for anonymous download.
- GitHub Wiki - the detailed user guide.

Edit the files above directly on GitHub (web UI or a clone) to update what the app shows - no
app update needed.
