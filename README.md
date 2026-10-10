# Reelia

A personal Android app for tracking shows and movies episode by episode, with multi-device
sync, detailed stats, and a yearly recap - built as an alternative to TV Time. See
[`docs/about/en.md`](docs/about/en.md) ([`fr`](docs/about/fr.md)) for the full feature list,
and [`docs/guide/en.md`](docs/guide/en.md) ([`fr`](docs/guide/fr.md)) for how it works day to
day.

- **Download**: latest APK on the [Releases page](https://github.com/Xenovyrion/Reelia/releases).
- **Report a bug or request a feature**: open an [Issue](https://github.com/Xenovyrion/Reelia/issues/new/choose).
- **Privacy**: see [`docs/privacy-policy/en.md`](docs/privacy-policy/en.md) ([`fr`](docs/privacy-policy/fr.md)).

## Repo structure

This repo hosts the app's public-facing content and release channel; the app's source code
lives in a separate, private repo. This one stays public so a few things keep working
without authentication:

- `docs/release-notes/{fr,en}.md` - in-app release notes, fetched live from GitHub.
- `docs/guide/{fr,en}.md` - in-app user guide ("Aide" in Profil), fetched live from GitHub.
- `docs/about/{fr,en}.md` - in-app "About" screen content, fetched live from GitHub.
- `docs/announcement.json` - in-app banner/popup message, edited directly here to broadcast
  a message to every install without a new app version.
- GitHub Releases on this repo - downloadable APK builds (both the rolling dogfooding build
  and versioned releases), since Release assets require the hosting repo to be public for
  anonymous download.

Edit the files above directly on GitHub (web UI or a clone) to update what the app shows -
no app update needed.
