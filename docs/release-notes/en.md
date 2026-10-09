# Release notes

## 0.35.0 — October 9, 2026
### ✨ New
- Episode detail sheet: new "My thoughts" field to jot down free-text notes on an episode —
  synced across devices and included in library backups
### 🐛 Fixes
- Release notes: opening/closing a version now animates smoothly instead of popping instantly,
  which used to clash with the tap ripple

## 0.34.0 — October 9, 2026
### ✨ New
- Library: an ongoing show you're fully caught up on (nothing new to watch right now) no longer
  stays listed under "Watching". If the next episode already has a known air date, it now only
  shows up in the "Upcoming" carousel; otherwise it moves to a new "Up to date" section
- Library and Search: the Filter icon now switches color and shows a badge with the number of
  active filters as soon as at least one is selected, so it's clear at a glance whether filters
  are applied
- Search (add to library) and Library's local search: the number of results found is now shown
  while searching

## 0.33.0 — October 8, 2026
### 🐛 Fixes
- **Important — sync reliability**: in some cases, one device's sync push could overwrite the cloud copy of a season's "watched" episodes that another device hadn't locally loaded yet, which could then disappear after syncing. Fixed: sync now only adds/updates fields, never replaces the whole document
- An announced-but-not-yet-aired episode can no longer show up wrongly checked as "watched" — a fix that self-applies automatically if an episode had ever ended up in that state
- An ongoing show you're fully caught up on (next episode not out yet): the progress ring/bar now reads 100%, instead of a percentage that looked stuck below it
- An upcoming episode's detail sheet now shows "Airs today/tomorrow/in N days" instead of a bare date when no summary is available yet
- A TMDB rating with no votes yet no longer displays as a fake "0.0/10" on detail and preview screens

## 0.32.0 — October 8, 2026
### ✨ New
- Stats: chart points and the line connecting them are now perfectly aligned (a slight visual offset fixed), and no longer overflow the chart box when a value is 0
- Home: the Agenda entry point is now more visible — a proper "Agenda" button instead of a bare arrow
- Library and Search: the initial load now shows an animated preview of the upcoming layout instead of a plain spinner
- Clearer empty states (Home, Library, Search, Agenda, Stats): an icon now accompanies the message instead of plain text alone
- General visual consistency refined (theme, spacing, pickers) — groundwork that sets up future screens, not very visible day-to-day
### 🐛 Fixes
- Accessibility: a poster's favorite badge and the release notes' expand/collapse button now correctly announce their state to screen readers

## 0.30.0 — October 8, 2026
### ✨ New
- Library: new original-language filter, same as on the Search screen (a new field stored per tracked title, backfilled gradually on the next refresh — no impact on titles already tracked)
- Stats: scatter/dot plots now connect their points with a thin line, and show a dashed average reference line with its exact value in a caption
### 🐛 Fixes
- A show with no confirmed air date (e.g. an announced season TMDB already lists episodes for but without a date yet, like Ahsoka season 2 or The Trauma Code) could wrongly show up in "Continue" on Home, the show's detail screen, and Library — fixed in all three places

## 0.29.0 — October 8, 2026
### ✨ New
- Trailers now play directly inside the app, right on a show's or movie's detail screen (its own section, under "About") — no more jumping out to YouTube
- Search screen (add to library): a title's preview now uses the same layout as a tracked title's detail screen, trailer included
- Changing the language in Profil now triggers a background refresh of already-tracked titles, so title/overview/episode names actually switch into the new language instead of staying frozen in the old one
- Profil tab: Settings reorganized into clear sections (Account, Library, Preferences, Notifications, API Key, Updates) with visual separation, instead of a list with no real hierarchy
- Stats: the weekly/monthly/day-of-week charts are now scatter/dot plots, still tappable for the exact value
### 🐛 Fixes
- **Important**: a bug could silently reset a tracked title's status, personal rating, favorite flag, and even watched episodes during automatic background refreshes. This is fixed — if you noticed statuses or episodes seemingly "unchecking" themselves over the past few days, this was the cause

## 0.27.0 — October 7, 2026
### ✨ New
- Search: new Sort button (separate from Filter) with 5 options — relevance, date (newest/oldest), rating, all-time popularity. Default: relevance in Title mode, all-time popularity in Actor/Actress mode
- Actor/actress search: genre/year/rating/language filters now also work on an actor's filmography (e.g. narrow it down to shows of one genre)
### 🐛 Fixes
- Search: the input field no longer grows taller when switching to Actor/Actress mode (the label used to wrap to two lines)

## 0.26.1 — October 7, 2026
### 🐛 Fixes
- Show detail: cast now comes from TMDB's aggregated credits across every season — an actor who joined the show partway through (e.g. Parminder Nagra in The Blacklist) now shows up, instead of being missing from a list limited to a single season
- Actor/actress search: results are now ranked by vote count instead of TMDB's "popularity" (a score reflecting current trending, not lasting fame) — genuinely well-known roles surface instead of obscure titles that happen to be trending right now

## 0.26.0 — October 7, 2026
### ✨ New
- Search (add to library): when a filter chip (genre, year, rating, language) is active and no text is typed, results now come from the whole matching TMDB catalog instead of just this week's trends
### 🐛 Fixes
- Actor/actress search: self-appearances (talk shows, award ceremonies) no longer drown out real shows/movies in the results, which are now ranked by the title's own popularity instead of air date

## 0.25.0 — October 7, 2026
### ✨ New
- Search (add to library): new actor/actress search mode — a "Title"/"Actor-Actress" toggle on the same field. In Actor mode, typing a name shows their full filmography (shows + movies) directly as results, no detour through their person page

## 0.24.5 — October 7, 2026
### ✨ New
- Search (add to library): new Year, Minimum rating and Language filters, alongside the existing genre filter
- Search: filter icon uniformized with the one on Shows/Movies

## 0.24.4 — October 7, 2026
### ✨ New
- Home and Shows/Movies: "Upcoming" cards are now clickable — tapping a show opens its next episode directly, tapping a movie opens its detail screen
- Agenda: when a whole season releases on the same day, its episodes now collapse into a single row ("Season X — N episodes available") instead of one row per episode
### 🐛 Fixes
- A show with no next episode confirmed by TMDB could stay stuck showing "Aired" forever in "Upcoming" (Home, Shows/Movies, and the widget) — all three now use the same reliable source as the Agenda, so the show correctly drops off the list

## 0.24.3 — October 7, 2026
### ✨ New
- Home: Suggestions now appears before Favorites
- Shows/Movies: filtering (status, genre, favorites, channel, year, my rating) and sorting each get their own button instead of being mixed into one sheet
- Shows/Movies: new filter criteria — favorites only, broadcast channel, release year, minimum personal rating
- Search (add to library): added a back arrow, consistent with the rest of the app

## 0.24.2 — October 7, 2026
### ✨ New
- Agenda: every upcoming episode of an announced season is now listed individually with its own date (not just the very next one), and each row shows the season alongside the episode
- Agenda: tapping an episode opens its detail directly within the show, on the Episodes tab
### 🐛 Fixes
- A show with no next episode confirmed by TMDB could stay stuck in the Agenda showing "Aired" forever — it now correctly drops off the list

## 0.24.1 — October 7, 2026
### 🐛 Fixes
- The fix for stuck "Upcoming" dates (0.24.0) could take up to 24h to kick in while waiting for the periodic background task — dates now also refresh right on app launch

## 0.24.0 — October 6, 2026
### ✨ New
- Agenda: from Home > Upcoming, a new button opens a full screen listing every upcoming release (episodes and movies), grouped by date, alongside the existing row
### 🐛 Fixes
- An already-aired episode or released movie could stay stuck showing up in "Upcoming" and the widget forever if release notifications weren't enabled — date refreshing no longer depends on that setting
- Checking an episode that automatically catches up several earlier unwatched ones now clearly tells you so, instead of doing it silently
- Removing a title from search results now asks for confirmation, matching its detail screen
- Adding a title from an actor's/actress's page no longer leaves a stale preview screen in your navigation history

## 0.23.1 — September 20, 2026
### 🐛 Fixes
- After a reinstall or device switch with an already sizeable library, some shows could resync with their seasons visible but an empty episode list — fixed, with an automatic repair on launch for shows already affected

## 0.23.0 — August 6, 2026
### ✨ New
- Home screen widget: add the Reelia widget to your home screen to see your next 5 upcoming releases (shows and movies) at a glance, with poster and countdown; tap a title to jump straight to its detail screen, and use the built-in button to refresh it manually

## 0.22.0 — August 4, 2026
### ✨ New
- Profile > Backup: export your entire library (shows, movies, watch history) to a JSON file, and restore it anytime from that file — a safety net independent of cloud sync, also handy for moving to a new account
- Release reminders: turn on notifications in Profile > Settings to get alerted when a movie or a tracked show's next episode is coming up, with configurable reminder offsets (30, 14, 8, 1 day before, or the day of)

## 0.21.1 — July 31, 2026
### 🔒 Security
- The app is now built and signed with a dedicated release key, separate from the development key — the previously distributed build was still using the debug key
- Before installing a downloaded update, the app now verifies its cryptographic checksum (SHA-256) provided by GitHub; a corrupted file or a missing checksum blocks the install

## 0.21.0 — July 16, 2026
### 🐛 Fixed
- Preview screens (search, Home's trending sections, an actor's filmography): a title already in your library now shows "Added" and takes you to your tracked copy, instead of offering to add it again
- "Hours watched" stat: episodes and movies TMDB has no runtime data for were counted as 0 minutes instead of a reasonable estimate, silently undercounting the total (especially noticeable after a TV Time import) — fixed going forward, and your already-logged watch history has been corrected retroactively

## 0.20.2 — July 15, 2026
### 🐛 Fixed
- Preview screens (before adding, reached from search or from an actor's filmography): tapping a cast member did nothing — it now correctly opens their page
- Home, "Continue Watching" section: a show whose only remaining unwatched episode belongs to an announced-but-unreleased season no longer shows up there

## 0.20.1 — July 15, 2026
### 🐛 Fixed
- Updating to 0.20.0 accidentally wiped the entire local library cache, forcing a full resync from the cloud and a fresh TMDB re-download of every show/movie — almost certainly the cause of the very slow first Home load, with categories only appearing after ~20 seconds. Fixed: updating to this version no longer touches your local data
### 🔧 Improved
- Home: the Movies, Shows, and Suggestions sections now each load independently instead of waiting for all three to be ready before showing anything

## 0.20.0 — July 15, 2026
### ✨ New
- Home: the "Trending Now", "Latest Movie Releases", and "Latest Show Releases" sections are replaced by two sections — **Movies** and **Shows** — each with a dropdown next to the title to pick Popular, Top Rated, Upcoming, or Right Now
- Show/movie detail pages (and the pre-add preview screens) now show the age/content rating (e.g. "16", "PG-13", "TV-MA") next to the star rating, whenever TMDB has one for the title
### 🔧 Improved
- Posters and images now fade in instead of popping in abruptly, for smoother scrolling

## 0.19.0 — July 14, 2026
### ✨ New
- Home: a "+" button now appears on the "Suggested for you", "Trending Now", "Latest Movie Releases", and "Latest Show Releases" sections to add a title straight to your library without leaving Home
### 🔧 Improved
- Home: titles already in your library no longer show up in these 4 sections, and disappear automatically as soon as you add them

## 0.18.0 — July 14, 2026
### 🔧 Improved
- Show detail, episode tab: a season that hasn't aired yet now keeps its full episode list (no more message replacing it) — a compact "Upcoming season" indicator appears between the episode counter and the "mark all watched" button, and every episode shows its own countdown (or "Date unknown" when TMDB hasn't published an air date yet)
- Search: when bulk-adding several titles in a row, the ✓ shown on an already-added title is now tappable and removes it from your library — a quick way to undo an accidental add without leaving the screen
- Search: trending no longer shows titles already in your library
- Search: an actual text search now shows a "+" for titles not in your library, or a tappable ✓ (to remove) for titles already there

## 0.17.0 — July 14, 2026
### ✨ New
- The quick "+" button on a search result no longer opens the title's detail screen — you stay on the search screen to add several titles in a row, and a ✓ replaces the "+" once added
- Show detail, episode tab: a season that hasn't aired yet now shows "Season not yet released" instead of an empty list; the next episode to watch shows "Upcoming episode" with a days-until countdown if it hasn't aired yet; every unreleased episode now shows its own countdown
### 🐛 Fixed
- Local library search: the search stayed active in the background after leaving the screen (a detail page, another tab), silently filtering the list with no visible field to explain why — it's now reset whenever you leave the screen
### 🔧 Improved
- "Recently added"/"recently watched" sort now shows a real section label (like every other sort) instead of a bare spacer

## 0.16.1 — July 14, 2026
### 🐛 Fixed
- Local library search: couldn't clear the typed text in some cases — the field now reacts instantly to typing, and a new "X" button clears it in one tap
- Post-add navigation from search made more reliable, landing on the Shows/Movies tab instead of the search results screen
- "Recently added"/"recently watched" sort: list items were no longer visually separated from the "Upcoming" section
### 🔧 Improved
- The "search my library" and "search TMDB to add" buttons now use clearly distinct icons (magnifying glass vs "+") to avoid confusion

## 0.16.0 — July 14, 2026
### ✨ New
- New local search in Shows/Movies: text search across your own library (title or episode name) — separate from the existing TMDB search used to find new titles to add
### 🐛 Fixed
- After adding a show or movie from search, the phone's back button returned to the search results screen instead of the Shows/Movies tab
- The progress ring on grid posters didn't have the same dark backing as Home's ring, making it hard to see against a bright poster
- "Recently watched" sort: a freshly added show could show up first if old watch history already existed for it (e.g. removed and re-added, or TV Time import) — only a watch that happened after the add now counts toward this sort
### 🔧 Improved
- The filter/sort sheet's "Reset" button now applies the reset immediately instead of waiting for a separate tap on "Apply"

## 0.15.2 — July 14, 2026
### 🐛 Fixed
- The filter/sort sheet's "Reset" button didn't reset the sort order back to its default, only the status/genre filters
- Filter/sort sheet: on small screens, the "Apply" button could be out of reach at the bottom with no way to scroll to it — the sheet now has a fixed height with internal scrolling, so the Reset/Apply row always stays visible
- Scroll-to-top on bottom tab tap looked like the whole list was "flying" upward on long lists — it now jumps instantly instead of animating
### ✨ New
- New "Recently watched" sort option in Shows/Movies, based on watch history
- Alphabetical sort now groups titles into per-letter sections (with a "0-9" bucket for titles starting with a digit) instead of one long undifferentiated list
### 🔧 Improved
- Status, genre, recently-added, and recently-watched sorts now apply a secondary alphabetical sort within each group
- Grid view in Shows/Movies optimized for better responsiveness on long libraries
- More precise App Check error message: now distinguishes a rate limit ("too many attempts") from the API being blocked on Google Cloud's side, with a tailored explanation for each

## 0.15.1 — July 14, 2026
### 🐛 Fixed
- Scroll-to-top on bottom tab tap still didn't work in some cases (e.g. Profile → Shows → back to Profile) — the previous fix wasn't enough, the whole mechanism was rebuilt
- Tapping "Update" could get stuck with no feedback if the download failed — the error message the app already computed but never showed is now displayed on screen
### ✨ New
- Added a loading indicator (spinner + "Downloading…" text) during updates, so it no longer looks like nothing is happening
### 🔧 Improved
- Sort (status, alphabetical, genre, recently added) is now part of the filter button in Shows/Movies instead of two nearly-identical buttons in the top bar
- App Check (debug) button: a 60-second cooldown after a failed fetch avoids compounding Firebase's own "too many attempts" rate limit by retrying too fast, and the error message is now translated and clearer. This limit comes from Firebase's own backend and can't be removed client-side — only handled more gracefully

## 0.15.0 — July 14, 2026
### 🐛 Fixed
- Important fix: account deletion was already wiping all data (library, history) before checking whether a fresh sign-in was needed — if the session wasn't recent enough, the data was lost while the account itself survived undeleted. Reauthentication is now always required before anything is touched, for both password and Google accounts
- The TV Time import link could open the TV Time app itself instead of a browser — it now forces the default browser
- Tapping a bottom tab didn't always scroll back to the top — it now always does
### ✨ New
- New sort menu in Shows/Movies: by status, alphabetical, genre, or recently added
- Track special episodes ("Specials", season 0) — without them counting toward a show's completion percentage
- "Learn more" now shows a presentation of the app, separate from the user guide
### 🔧 Improved
- The Profile tab is now split into Settings and Stats sub-tabs instead of one long screen
- Language picker only offers French/English, the only languages with real translated text
- The "data source" section is hidden while there's only one provider (TMDB)
- Sign-up verification email temporarily disabled (was landing in spam without a dedicated sending domain)
- GitHub release titles now include the version number

## 0.14.0 — July 14, 2026
### ✨ New
- Safer, simpler account deletion: when Firebase requires a recent sign-in, the app now offers to confirm right there with your password (or a fresh Google sign-in) instead of forcing a full sign-out/sign-in
- Password strength indicator at sign-up, with a minimum of 8 characters required
- Eye icon on password fields to reveal what you typed
- Verification email sent automatically on sign-up
- The "Learn more" button on the sign-in screen now opens an in-app presentation (same look as the guide) instead of a GitHub page
### 🐛 Fixed
- After signing out or deleting your account, the sign-in screen kept the previously typed email and password instead of being blank

## 0.13.3 — July 14, 2026
### ✨ New
- Home now has two sections, "Favorite Shows" and "Favorite Movies" — favoriting a title used to not show up anywhere
### 🐛 Fixed
- After resetting the library, the stats at the top of the Profile tab could briefly flash inconsistent numbers before settling at zero
### 🔧 Improved
- Search screen: title and year are now centered under the poster, with no wasted blank line when the title fits on one line (up to 3 lines for long titles)
- Profile tab: the "Save", "Check for updates" and "Release notes" buttons are now centered; the "Help" button moved into the top bar so it's visible without scrolling to the bottom
- Removed the temporary diagnostic code added to track down the crash fixed in 0.13.1

## 0.13.2 — July 14, 2026
### 🐛 Fixed
- The release notes' "Current" badge stayed stuck on the newest published version instead of following the version actually installed — it now tracks your version, and newer entries get a distinct "New version" badge
- On launch, an "empty library" text could briefly flash while the suggestions were still loading — Home now keeps the animated loading icon up until the content is actually ready
### 🔧 Improved
- Cleaned up a few deprecated technical bits (icons, APIs) flagged by Android Studio, no visible impact

## 0.13.1 — July 14, 2026
### 🐛 Fixed
- The app could crash right after signing in on a freshly reset library, before the TMDB API key had time to sync back down
### 🔧 Improved
- Native Android back gesture (edge swipe) support
- Internal app naming cleanup for consistency

## 0.13.0 — July 14, 2026
### ✨ New
- In-app announcements: an important message can show as a banner or a popup on launch, published directly on GitHub without a new app version
- New **Help** screen (Profile): the user guide is now readable in-app, formatted into color-coded sections instead of plain text
- Remove a show or movie from the library directly from its detail screen (trash button, with confirmation)
### 🐛 Fixed
- The add button on Search results now navigates straight to the added title's detail screen; going back returns to the Series/Films home instead of the Search screen
- Resetting the library then uninstalling/reinstalling the app could bring old shows back and auto-sign you in — Android's default auto-backup, which was restoring a stale local copy, is now disabled
### 🔧 Improved
- The back arrow has a more polished look across every screen that uses it
- In-app updates now follow real version numbers instead of triggering on every new commit
- Stronger protection on Auth/Firestore access (Firebase App Check)

## 0.12.0 — July 13, 2026
### ✨ New
- Library is split back into two dedicated tabs, Séries and Films, each with its own filter, search, and grid/list toggle — searching from Séries only searches shows, and from Films only movies
- The Search screen is fully redesigned to match the rest of the app (poster cards, sections), with a clear button and a genre filter
- Home now has a search button (covering both series and movies) and an "Upcoming" section, matching Séries/Films
### 🔧 Improved
- Search no longer fires a network call on every keystroke: longer debounce and a 2-character minimum before it queries

## 0.11.0 — July 13, 2026
### ✨ New
- Person pages now show an Awards & Nominations section (via Wikidata, since TMDB has no awards data)
- Show/movie pages now have a clickable Crew section (director, writer, composer, creator) alongside Cast — tap through to their person page just like an actor
- Home's greeting now reflects the time of day and your first name when signed in, instead of a flat "Bonjour"
### 🐛 Fixed
- The in-app update could fail to install with a generic "problem with the app file" error and no way to understand why — a failed download is now caught and shown as a real, retryable error
- Home's discovery rows (and the person page's filmography) shifted around while scrolling because card height varied by title length — every card now reserves a consistent height
- The Continue Watching progress ring and episode text could become unreadable on a bright/light backdrop image — both now sit on a dark scrim so they stay legible regardless of the artwork
### 🔧 Improved
- Cast/Crew rows widened so full names are actually readable instead of truncated to a few letters
- Crew job titles (Director, Writer, Composer, Creator) are now translated instead of always showing the raw English TMDB term

## 0.10.0 — July 13, 2026
### ✨ New
- Home is now a discovery hub instead of repeating the Library grid: Suggestions based on your library, Trending Now, and Latest Movie/Show Releases, alongside Continue Watching — all via TMDB, no paid third-party service
- Person pages now show a full filmography (TMDB combined credits), with posters linking to each title
### 🐛 Fixed
- Dates (birthday/death date, upcoming release dates) were shown as raw ISO strings instead of following the app's language — a French device would still see "1955-01-18" instead of "18 janvier 1955"
- A long character name on a filmography poster could push the release year off-screen
### 🔧 Improved
- Person pages redesigned into cards (biography, filmography), matching the show/movie detail screens
- An actor/actress biography now falls back to English when TMDB has no translation for the app's language, instead of showing an empty biography

## 0.9.0 — July 13, 2026
### ✨ New
- Tapping an episode opens a detail sheet with its image, title, air date, rating, and overview
- Checking an episode now auto-fills every earlier unwatched episode in the season (catch-up); long-press a checkmark to toggle just that one episode individually
### 🐛 Fixed
- The season "mark all watched" checkmark only ever marked watched — tapping it while the season was fully watched now un-marks the whole season
- The episode detail sheet's mark-watched button could get stuck reflecting a stale state and stop responding to repeated taps
- The episode detail sheet could cut off the mark-watched button on long overviews — its content now scrolls
### 🔧 Improved
- Episode list rows redesigned as cards with a clearer watched state
- Show/movie detail "About" sections split into cards (overview, schedule, cast, watch providers), matching the Stats screen's look

## 0.8.0 — July 13, 2026
### ✨ New
- Charts (weekly/monthly/weekday) are now tappable to reveal a bar's exact value
- The "Shows by status" breakdown now opens a detail view, same as genres and networks
### 🐛 Fixed
- The theme followed the phone's system light/dark setting instead of always using the dark "Aubergine" theme — on a phone in light mode, the app rendered in very different, unfinished-looking colors
- Genre/network detail panels were capped in height and didn't show the full list — they now open full-screen, fully scrollable
### 🔧 Improved
- Favorite genres ranking now shows 10 genres instead of 5
- Detail lists (genre/network/status) are now sorted alphabetically

## 0.7.0 — July 13, 2026
### ✨ New
- "Reset library" button in Profile > Account
- App version and release notes are now viewable in Settings
### 🐛 Fixed
- The TV Time import screen got stuck at 100% (cloud sync was blocking the summary screen)
- Resetting the library was very slow (documents were deleted one at a time)
### 🔧 Improved
- Profile buttons now line up in a uniform grid
- More readable stat numbers (thousands separators, no more overflow)
- Total watch time now shown as months/days/hours
- TV Time import screen: shorter text, direct link to gdpr.tvtime.com, collapsible details

## 0.6.0 — July 13, 2026
### ✨ New
- Import your library from TV Time: shows, movies, and watch history, automatically matched on TMDB
### 🔧 Improved
- Clearer explanation of which file to pick on the TV Time import screen

## 0.5.0 — July 13, 2026
### 🐛 Fixed
- A show/movie's status didn't switch to "Completed" once fully watched
- The "Completed" stat and library-completion percentage stayed stuck at 0
- The "Shows airing" stat stayed stuck at 0
### 🔧 Improved
- Network breakdown stats can now be drilled into, same as genres
- More breathing room across charts and sections

## 0.4.0 — July 12, 2026
### ✨ New
- In-app update checking and installation
- Account deletion
### 🔧 Improved
- Redesigned login screen (logo, gradient, Google sign-in)
- Sync extended to followed shows/movies, watched episodes, and the watch log
- Stats and Settings merged into a single Profile tab
- Richer stats charts (estimated time remaining, weekly average, breakdown by status/network/day)

## 0.3.0 — July 12, 2026
### ✨ New
- New "Aubergine" theme (colors, typography, shapes)
- 4-tab navigation: Home, Library, Stats, Settings
- Home screen with a "Continue watching" carousel
- Favorites for shows and movies
- Sign-in and cross-device sync via an account

## 0.2.0 — July 12, 2026
### ✨ New
- Trailers, clickable cast, and a dedicated page for each actor/director
- Networks and broadcast status on the show page
### 🔧 Improved
- All episodes now load as soon as a show is added (reliable progress from the start)
- Redesigned Show and Movie pages

## 0.1.0 — July 11, 2026
### ✨ New
- First version: track shows and movies via TMDB
- Library, search, stats, and settings (French/English)
- Mark episodes watched, including in bulk per season
