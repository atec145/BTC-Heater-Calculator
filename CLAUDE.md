# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Android app (Kotlin + Jetpack Compose) that shows whether running a Bitcoin miner as a space heater is profitable compared to an oil-fired boiler. Fetches live data from four public APIs, calculates a break-even electricity price, and displays an hourly price chart color-coded against that threshold.

## Build Commands

```bash
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build (minified, requires signing config)
./gradlew lint                   # Lint check (must pass before commit)
./gradlew test                   # Unit tests
./gradlew test --tests "com.example.app.domain.usecase.CalculateProfitabilityUseCaseTest"  # Single test class
./gradlew connectedAndroidTest   # Instrumented tests (emulator required)
```

No `google-services.json` is needed — Firebase was removed from this project. The `.claude/rules/firebase.md` rule file is a leftover from the project template and does not apply here.

**Build config:** minSdk 24, targetSdk/compileSdk 35, Java 17, versionName "1.3.0"

## Architecture

Clean Architecture with three strict layers. Data flows one way: Presentation → Domain → Data.

```
domain/        Pure Kotlin — no Android imports
  model/       MinerConfig, MarketData, HourlyPrice, SaleMode, ProfitabilityResult
  repository/  Interfaces only (MarketDataRepository, MinerConfigRepository)
  usecase/     CalculateProfitabilityUseCase, FetchOilPriceUseCase

data/
  remote/      Retrofit APIs: AwattarApi, BlockchainInfoApi (in MempoolApi.kt), CoinGeckoApi, HeizOel24Api
               MarketDataRemoteDataSource assembles the first three into a MarketData object
               HeizOelPriceRemoteDataSource fetches oil price history from heizoel24.de
  local/       Room: MinerConfigEntity, MinerConfigDao, AppDatabase (btc_heater.db)
  repository/  MarketDataRepositoryImpl, MinerConfigRepositoryImpl (uses SharedPreferences: btc_heater_prefs)

presentation/
  screens/home/        Dashboard — HomeViewModel, HomeScreen, HomeUiState
  screens/settings/   Miner config + sale mode + oil settings — SettingsViewModel, SettingsScreen
  components/         PriceBarChart (pure Canvas, no external chart lib)
  navigation/         AppNavigation with Screen sealed class (Home, Settings)

di/
  NetworkModule        Retrofit instances (one per base URL, @Named qualifier)
  DatabaseModule       Room + SharedPreferences
  RepositoryModule     @Binds interfaces to implementations
```

## Key Business Logic

**Break-even electricity price** (`CalculateProfitabilityUseCase`):
- `btcPerDay = (hashrate_Hs × 86400) / (difficulty × 2^32) × 3.125`
- `breakEven = (eurPerDay / kWhPerDay) + oilBoilerCostPerKwh`
- `isWorthIt = breakEven > currentElectricityPrice`
- Oil cost: `heizölPreis / (10 kWh/L × boilerEfficiency)`
- Signature: `invoke(miners, market, saleMode, boilerEfficiency, minerWaermeeffizienz)`

**`ProfitabilityResult` fields include:**
- `profitableHoursToday`, `waermeEnergyKwhProfitable`, `oilLiterAvoided`, `oilEurSaved`, `co2KgAvoided` (factor: 2.68 kg/L)

**`FetchOilPriceUseCase`**: Fetches oil price from heizoel24.de with 24-hour caching. Supports manual vs. auto price modes via `MinerConfigRepository`.

**Sale modes** (`SaleMode` sealed class):
- `Instant` — uses live BTC/EUR from CoinGecko
- `Hodl(targetPriceEur)` — uses user-entered target price

**API endpoints:**
- Awattar AT: `https://api.awattar.at/v1/marketdata` — hourly prices in €/MWh; final price = `(raw/1000 × 1.03 + 0.015) × 1.20`
- blockchain.info: `https://blockchain.info/q/getdifficulty` — returns plain-text Double (parsed via `ScalarsConverterFactory`); file is named `MempoolApi.kt`
- CoinGecko: `https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=eur`
- heizoel24.de: `https://www.heizoel24.de/api/chartapi/GetAveragePriceHistory` — returns price history in €/100L

**Heizölpreis** defaults to 1.10 €/L in SharedPreferences. Supports auto-fetch from heizoel24.de or manual override.

## Tech Stack

- **Language:** Kotlin + Coroutines
- **UI:** Jetpack Compose + Material 3
- **DI:** Hilt (`@HiltViewModel`, `@Binds`, `@Named` for multiple Retrofit instances)
- **Network:** Retrofit 2 + OkHttp + Gson; blockchain.info uses `ScalarsConverterFactory` for plain-text Double
- **Local storage:** Room (miner configs) + SharedPreferences (sale mode, oil price, boiler efficiency, grid surcharge, miner heat efficiency, oil fetch timestamps)
- **No Firebase** — removed; app uses only public APIs

## Development Workflow

1. `/requirements` — Describe feature → creates GitHub Issue with spec
2. `/architecture` — Design tech decisions → updates issue
3. `/android-dev` — Build Kotlin/Compose implementation
4. `/qa` — Test + security audit
5. `/deploy` — Build release APK/AAB

Feature tracking via GitHub Issues. Commit format: `feat(#12): description`.

```bash
gh issue list --label "type:feature"
gh issue view <number>
```

## UI / Compose Conventions

- All screens expose a single `UiState` data class with `isLoading`, `error`, and data fields
- ViewModel exposes `StateFlow<UiState>`, collected via `collectAsState()` in Composables
- Dynamic colors (Material You / `dynamicDarkColorScheme`) are intentionally disabled — use the static color scheme in `Color.kt`
- `PriceBarChart` uses pure Canvas (no external chart library) — keep it that way
- **Exception:** `PriceBarChart` currently hardcodes bar colors (green/red/yellow) directly in the component — new chart colors should be added to `Color.kt` instead

## Constraints

- Max 2 miners configurable
- Miner fields validated: hashrate > 0, watt > 0
- When no miners are active, dashboard shows "Kein Miner aktiv" instead of calculation
- Awattar fallback: if current hour not found in response, use last available price
- Only one test class exists: `CalculateProfitabilityUseCaseTest` — no ViewModel or repository tests yet
