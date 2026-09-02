# QB Tester

A native Android trivia app: it shows you one of the NFL's 32 teams at a time, styled in that
team's colors, and asks you to name the current starting quarterback.

## Quarterback data source

The app needs to know who is *currently* starting at QB for all 32 teams, and that changes
constantly (injuries, benchings, trades, byes, new seasons). Hard-coding it would go stale
within weeks, so quarterback data comes from **Sleeper's public players API**
(`https://api.sleeper.app/v1/players/nfl`) - a free, keyless, documented endpoint
(docs: https://docs.sleeper.com/) built for fantasy football tools.

**How the starter is determined:** Sleeper maintains `depth_chart_position` and
`depth_chart_order` per player. A team's starter is the player who is the *only* active QB
listed at `depth_chart_position == "QB"` and `depth_chart_order == 1` for that team. If a team
has zero such players (depth chart data missing) or more than one (a genuinely unsettled QB
competition), the app marks that team **temporarily unavailable** and excludes it from the quiz
rather than guessing - see `SleeperRemoteDataSource.kt`. As of this writing all 32 teams resolve
unambiguously.

This is a heuristic, not an official NFL announcement - Sleeper's depth charts are maintained by
their staff/community for fantasy purposes and can occasionally lag a roster move by hours. That
tradeoff is what buys us a free, no-signup, no-backend data source; see "Alternatives considered"
below for what a more authoritative (but paid) option would look like.

**Headshots** come from the same provider, using the player's id:
`https://sleepercdn.com/content/nfl/players/{player_id}.jpg`. If an image is missing or fails to
load, the app falls back to a generic silhouette (`ic_player_silhouette.xml`) rather than showing
a broken image.

**Team branding** (name/city/abbreviation/primary+secondary colors) does *not* come from an API -
it's a static catalog in `NflTeam.kt`, since that data is stable for a season and not worth a
network round trip.

### Caching and refresh

The last successfully fetched quarterback data is cached locally (DataStore Preferences, as a
small JSON blob) so the quiz still works offline using the last-known-good data. The app does
*not* hit the network on every launch:

- On app start, it refreshes only if the cache is missing or older than the configured interval
  (default: 24 hours - see `RefreshPolicy.DEFAULT_STALE_AFTER_MILLIS`).
- A manual **"Refresh QB Data"** action on the Home screen always re-fetches.
- The Home screen shows a subtle "QB data updated: <date>" label so you know how current the data
  is.
- If a refresh fails (no internet, API hiccup), the app silently keeps using whatever cached data
  it already has and shows a small status message - it never crashes because an external provider
  had a problem.

### No secrets to manage

Sleeper's players endpoint requires no API key or authentication, so there is nothing to keep out
of source control for this data source. If a future revision switches to an authenticated
provider (see below), that key must **not** be embedded in the APK - it would need either a
build-time secret injected via a local (git-ignored) `local.properties`/Gradle property *and* a
willingness to accept it can be extracted from the APK, or (better) a small backend proxy the app
calls instead of the provider directly, so the real key never ships to a device.

### Alternatives considered

- **ESPN's hidden `site.api.espn.com` / `sports.core.api.espn.com` endpoints** - also expose a
  depth chart, but are fully undocumented, unofficial, and can change or start blocking requests
  without notice. Not used for v1; a plausible future fallback provider behind the same
  `QbRemoteDataSource` interface.
- **SportsDataIO** - purpose-built "starting lineups" data, most authoritative option, but it's a
  paid commercial API (~$25+/mo and up) and would require a backend proxy to protect the API key
  instead of shipping it in the APK. Overkill for v1.
- **TheSportsDB** - free, but not depth-chart-aware; not a reliable signal for "current starter."

## Architecture

```
ui/            Compose screens + ViewModels (Home, Quiz, Results)
domain/        AnswerMatcher - pure, testable answer-normalization logic
data/
  remote/      SleeperApi (Retrofit), SleeperPlayerDto, SleeperRemoteDataSource
               (the ONLY place that knows Sleeper's JSON shape or starter-resolution rules)
  local/       QbCacheDataSource (DataStore-backed cache)
  repository/  QuarterbackRepository interface + impl, RefreshPolicy
model/         NflTeam (static branding catalog), Quarterback, QbLookupResult
di/            AppContainer - small hand-written dependency container (no Hilt/Dagger)
```

The rest of the app only ever talks to `QuarterbackRepository`'s
`getSnapshot()` / `refreshIfStale()` / `forceRefresh()`. Swapping data providers later (e.g. to
ESPN or SportsDataIO) means writing a new `QbRemoteDataSource` implementation and wiring it into
`AppContainer` - no ViewModel or Compose code needs to change.

A full DI framework (Hilt/Dagger) was intentionally skipped in favor of a small manual
`AppContainer` + `ViewModelProvider.Factory` - the app is simple enough that annotation
processing would add build complexity without a real benefit.

### Answer matching

`AnswerMatcher` (in `domain/`) is case/whitespace/punctuation insensitive, strips generational
suffixes (Jr/Sr/II/III/IV), and accepts a bare last name ("Prescott"). It also forgives "sounds
right but spelled wrong" guesses - e.g. "Jackson Dart" for "Jaxson Dart" - via `Soundex` phonetic
encoding (`domain/Soundex.kt`) applied to the last name, either alone or as part of a full-name
guess where every word matches phonetically. It deliberately does **not** do generic
edit-distance fuzzy matching (that could accept an unrelated but similarly-spelled name) and does
**not** accept a bare first name, even phonetically - too many QBs share first names for that to
be safe.

## Building and running

Requirements: JDK 17+ and the Android SDK (compileSdk 34 / build-tools 34.0.0). Android Studio
handles both automatically if you open the project there.

```
./gradlew assembleDebug      # build the debug APK
./gradlew testDebugUnitTest  # run all unit tests
./gradlew installDebug       # install on a connected device/emulator
```

`local.properties` (git-ignored) must contain `sdk.dir=<path to your Android SDK>` - Android
Studio creates this automatically when you open the project; if building from the command line
without Studio, create it yourself.

## Tests

Unit tests (JVM, no emulator needed) cover the logic the product spec called out explicitly:

- `AnswerMatcherTest` - normalization, last-name acceptance, first-name rejection (even
  phonetically), phonetic tolerance for misspellings, rejection of non-phonetic near-misses
- `SoundexTest` - phonetic encoding against classic reference vectors plus the Jaxson/Jackson case
- `RefreshPolicyTest` - stale/fresh cache decisions
- `SleeperRemoteDataSourceTest` - starter resolution: single candidate, zero candidates, tied
  candidates, inactive players, missing names
- `QuarterbackRepositoryImplTest` - cache fallback, refresh-only-when-stale, failed refresh
  preserves existing cache
- `QuizViewModelTest` - full 32-team session with no repeats, retry-on-wrong-answer without
  scoring it, give-up flow, completion totals, play-again reshuffle, teams with an unresolved
  starter excluded from the pool

## Deliberately out of scope for v1

Multiple-choice mode, AFC/NFC or division-only quizzes, previous-season QBs, difficulty levels,
timed mode, streaks, accounts, and any other-position quizzes were all left out on purpose per the
product brief. The `QuarterbackRepository` / `AnswerMatcher` seams should make most of these
additive rather than requiring a rewrite.
