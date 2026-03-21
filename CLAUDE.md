# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Android app (Kotlin + Jetpack Compose) that shows whether running a Bitcoin miner as a space heater is profitable compared to an oil-fired boiler. Fetches live data from three public APIs, calculates a break-even electricity price, and displays an hourly price chart color-coded against that threshold.

## Build Commands

```bash
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build (minified)
./gradlew lint                   # Lint check (must pass before commit)
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests (emulator required)
```

No `google-services.json` is needed — Firebase was removed from this project.

## Architecture

Clean Architecture with three strict layers. Data flows one way: Presentation → Domain → Data.

```
domain/        Pure Kotlin — no Android imports
  model/       MinerConfig, MarketData, HourlyPrice, SaleMode, ProfitabilityResult
  repository/  Interfaces only (MarketDataRepository, MinerConfigRepository)
  usecase/     CalculateProfitabilityUseCase — the core business logic

data/
  remote/      Retrofit APIs: AwattarApi, MempoolApi, CoinGeckoApi
               MarketDataRemoteDataSource assembles all three into a MarketData object
  local/       Room: MinerConfigEntity, MinerConfigDao, AppDatabase
  repository/  MarketDataRepositoryImpl, MinerConfigRepositoryImpl (uses SharedPreferences for settings)

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

**Sale modes** (`SaleMode` sealed class):
- `Instant` — uses live BTC/EUR from CoinGecko
- `Hodl(targetPriceEur)` — uses user-entered target price

**API endpoints:**
- Awattar AT: `https://api.awattar.at/v1/marketdata` — hourly prices in €/MWh (÷1000 → €/kWh)
- mempool.space: `https://mempool.space/api/v1/difficulty-adjustment`
- CoinGecko: `https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=eur`

**Heizölpreis** is entered manually by the user (no scraping in MVP). Stored in SharedPreferences, default 1.10 €/L.

## Tech Stack

- **Language:** Kotlin + Coroutines
- **UI:** Jetpack Compose + Material 3
- **DI:** Hilt (`@HiltViewModel`, `@Binds`, `@Named` for multiple Retrofit instances)
- **Network:** Retrofit 2 + OkHttp + Gson (one Retrofit instance per base URL)
- **Local storage:** Room (miner configs) + SharedPreferences (sale mode, oil price, boiler efficiency)
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

## Constraints

- Max 2 miners configurable
- Miner fields validated: hashrate > 0, watt > 0
- When no miners are active, dashboard shows "Kein Miner aktiv" instead of calculation
- Awattar fallback: if current hour not found in response, use last available price
