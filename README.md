# reelia-content

Public content and release channel for [Reelia](https://github.com/Xenovyrion/Reelia), a
personal Android series/movie tracking app. The app's source code lives in a private repo;
this one stays public so a few things keep working without authentication:

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
