# Currency Demo

![Coverage](https://img.shields.io/badge/coverage-100.00%25-brightgreen)

Android sample project that showcases usages of a `CurrencyListFragment` using Kotlin, Jetpack Compose, Room, Hilt, and Coroutines.

## Project Highlights
- **DemoActivity + CurrencyListFragment** driven by Compose UI so the fragment is reusable in other contexts.
- **Room** persistence with Flow-based DAO queries that satisfy the search matching rules from the spec (prefix match, match after a whitespace, symbol prefix).
- **Hilt** DI graph that wires the database, repository, coroutine dispatchers, and view models.
- **Demo controls** (5 buttons) to clear/seed the DB, switch between currency list A (crypto), list B (fiat), or show all purchasable currencies.
- **Search UX** with cancel/back handling and an explicit empty state.
- **Unit tests** covering the view-model search rules plus a reusable `MainDispatcherRule`.

## Getting Started
1. Ensure you have JDK 17 installed (`/usr/libexec/java_home -v 17` should resolve one).  
2. From the repo root, build and run unit tests:
   ```bash
   ./gradlew test
   ```
3. Open the project in Android Studio (Giraffe or newer) and run the `app` configuration on an emulator or device.

## Test Coverage
- Latest combined line coverage: <!-- COVERAGE:START -->100.00%<!-- COVERAGE:END -->
- Generate the combined JVM + instrumentation coverage report:
  ```bash
  ./gradlew testDebugUnitTest connectedDebugAndroidTest jacocoTestReport
  ```
- Reports are written to `app/build/reports/jacoco/jacocoTestReport/html/index.html`.

## Tech Stack
| Layer | Implementation |
|-------|----------------|
| UI | Compose Material 3 inside `DemoActivity` and `CurrencyListFragment` |
| State | `DemoViewModel`, `CurrencyListViewModel`, Kotlin `StateFlow` |
| DI | Hilt (`@HiltAndroidApp`, modules for DB, repository, dispatchers) |
| Data | Room (`CurrencyInfoEntity`, DAO, converters) with Flow-backed queries |
| Domain | `CurrencyInfo` + enums for dataset selection |
| Tests | `CurrencyListViewModelTest` with Turbine + `MainDispatcherRule` |

## Data & Requirements
- `SampleCurrencyData` contains the two lists Crypto list A & Fiat list B and the fragment uses the Room data, not in-memory lists.
- Search rules  
  1. name starts with search term  
  2. name contains "` space + search term`"  
  3. symbol starts with search term  
- The fragment exposes normal, searching, and empty UI states per requirement.

## Useful Paths
- `app/src/main/java/com/crypto/demo/ui/DemoActivity.kt` – wiring of controls and fragment swapping.
- `app/src/main/java/com/crypto/demo/ui/CurrencyListFragment.kt` – Compose-driven list implementation.
- `app/src/main/java/com/crypto/demo/data/repository/CurrencyRepositoryImpl.kt` – DB/Flow integration.
- `app/src/test/java/com/crypto/demo/ui/CurrencyListViewModelTest.kt` – verifies search behaviour.

## Notes
- Gradle wrapper is pinned to **8.7** with Kotlin **1.9.24** and Compose BOM **2024.06.00**.
- The project seeds the database automatically on first launch (and exposes a button to reseed).
