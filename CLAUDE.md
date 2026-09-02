# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

QB Tester is a native Android (Kotlin + Jetpack Compose) trivia app: it shows one of the 32 NFL
teams at a time, styled in that team's colors, and quizzes the user on the team's current
starting quarterback. See `README.md` for the full picture, especially the "Quarterback data
source" section - it explains why QB data comes from a live API instead of being hard-coded, and
exactly how the "current starter" is determined.

## Commands

```
./gradlew assembleDebug        # build the debug APK
./gradlew testDebugUnitTest    # run all unit tests (JVM, no emulator)
./gradlew installDebug         # install on a connected device/emulator
```

Run a single test class or method with `--tests`, e.g.:
```
./gradlew testDebugUnitTest --tests "com.qbtester.app.domain.AnswerMatcherTest"
./gradlew testDebugUnitTest --tests "com.qbtester.app.domain.AnswerMatcherTest.accepts last name only"
```

Requires JDK 17+ (the Gradle daemon itself must run on it) and the Android SDK
(compileSdk 34 / build-tools 34.0.0) via `local.properties` (`sdk.dir=...`, git-ignored - Android
Studio creates this automatically when the project is opened).

## Architecture

The rest of the app talks only to `QuarterbackRepository` (in `data/repository/`) - never
directly to Sleeper. That's the seam for swapping data providers later:

```
ui/            Compose screens + ViewModels (home/, quiz/, results/, components/)
domain/        AnswerMatcher - pure, testable answer-normalization logic
data/
  remote/      SleeperApi (Retrofit) + SleeperPlayerDto + SleeperRemoteDataSource -
               the ONLY place that knows Sleeper's JSON shape or the starter-resolution rule
               (single active player at depth_chart_order == 1 wins; 0 or >1 candidates means
               that team is reported Unavailable and excluded from the quiz, never guessed)
  local/       QbCacheDataSource - DataStore-backed cache of the last successful fetch
  repository/  QuarterbackRepository (interface) + Impl + RefreshPolicy (staleness cutoff)
model/         NflTeam (static branding catalog: colors/city/name/abbreviation), Quarterback,
               QbLookupResult (Available/Unavailable)
di/            AppContainer - hand-written dependency container (deliberately no Hilt/Dagger;
               see README for why) + AppViewModelFactory
```

Navigation (`ui/navigation/QbTesterNavHost.kt`) nests `quiz` and `results` under a shared
`quiz_flow` back-stack entry so both screens can bind to the same `QuizViewModel` instance
(standard `navController.getBackStackEntry(route)` pattern) - "Play Again" resets that same
ViewModel rather than navigating to a fresh instance.

Team branding colors live only in `NflTeam.kt` - never hard-code a team's hex color anywhere
else. `ui/theme/contrastingOnColor()` picks white/near-black text based on a team color's
luminance, since some teams' primary colors are very light or very dark; readability takes
priority over always using a fixed on-color.

## Testing conventions

- JVM unit tests only (`src/test/`) - no Robolectric/instrumented tests in this project yet.
- `QuarterbackRepositoryImplTest` fakes `QbRemoteDataSource`/`QbLocalCache` (interfaces
  introduced specifically so the repository doesn't need a mocking library).
- `QuizViewModelTest` uses `MainDispatcherRule` (`src/test/.../MainDispatcherRule.kt`) to make
  `viewModelScope.launch` run synchronously via `UnconfinedTestDispatcher`.
- `SleeperRemoteDataSourceTest` tests `SleeperRemoteDataSource.resolveStarters(...)` directly as a
  pure function against constructed `SleeperPlayerDto` maps - no network/Retrofit involved.

## Things to keep in mind when changing this app

- Never accept a bare first name as a correct answer in `AnswerMatcher`, even via the `Soundex`
  phonetic path (many QBs share first names) - explicit product requirement.
- `AnswerMatcher` allows phonetic (Soundex) tolerance for misspellings ("Jackson Dart" for
  "Jaxson Dart") but never generic edit-distance fuzzy matching - the former is scoped by sound
  groups and hard to accidentally collide, the latter risks silently accepting a wrong player.
- Never let a resolved starter be guessed when Sleeper's depth chart is ambiguous (0 or >1
  candidates for a team) - mark it `Unavailable` and keep it out of the quiz pool instead.
- The quiz must keep working offline from cache; don't add a code path that requires a network
  call before a quiz can start.
- Don't fetch QB data on every question or every screen - the whole point of `RefreshPolicy` +
  local caching is to make at most one network call per staleness window.
