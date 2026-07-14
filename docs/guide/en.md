# User Guide — Reelia

Reelia is a personal show/movie tracking app, designed as a TV Time replacement. This guide
covers the parts that aren't obvious from daily use.

## Getting started

1. **TMDB API key** — Reelia uses [TMDB](https://www.themoviedb.org/) for all its info
   (posters, synopsis, episodes...). Create a free TMDB account, generate a v3 API key in
   your account settings, then paste it into **Settings** on first launch.
2. **Reelia account** — an email/password (or Google) account lets you sync your library
   across multiple devices.

## How sync works

When you're signed in, Reelia automatically syncs via Firebase:

- the shows/movies you follow;
- the watched/unwatched status of each episode and movie;
- your watch history (used for stats);
- your TMDB API key, so you don't have to re-enter it on a new device.

**Important note after reinstalling**: on first launch on a new device (or after
reinstalling), sign in with your account. If your TMDB API key was already synced from
another device, it imports automatically and your library resyncs right away. If that's
not the case yet (first-ever sign-in), re-enter your key once in Settings: this kicks off
the sync and brings back everything already saved on Firebase.

## Home screen

Home is a discovery hub, not a second library: it surfaces continue watching, suggestions
based on your favorites/recent additions, trending titles, and the latest movie/show
releases (via TMDB, free). A title tapped from Home isn't necessarily already in your
library — it opens its TMDB page with a button to add it, same as from Search.

## Marking episodes as watched

- **Checking an episode** automatically catches up every earlier unwatched episode in the
  same season (e.g. checking episode 10 while 1-9 were unwatched checks 1-9 too). Handy
  for a quick catch-up.
- **Unchecking** an episode only affects that one.
- To check/uncheck a single episode without that automatic catch-up (e.g. to mark one
  isolated episode without touching the others), long-press its checkmark.
- The checkmark at the top of the episode list (next to "X / Y episodes watched") marks or
  unmarks the whole season at once, in either direction.

## Privacy

- Your library and API key are stored in a Firestore document accessible only by your
  account (Firebase security rules — no one else can access it, even with the project URL).
- Your password is never stored by the app: it's sent to Firebase Authentication, which
  handles it end-to-end (the app never sees it in clear text after entry).

## Statuses and colors

Each color has a fixed meaning throughout the app:

- **Teal** — watching / completed
- **Amber** — planned / on hold
- **Periwinkle** — want to watch later
- **Coral** — favorite (independent of watch status)

## Known issues / limitations

- Watch history recorded *before* the update that added watch-log sync doesn't
  retroactively sync — only new entries do.
- Google sign-in requires the Google provider to be enabled on the Firebase side; without
  it, the button will show an error instead of working.
