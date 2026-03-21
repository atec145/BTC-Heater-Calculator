---
name: android
description: Kotlin, Jetpack Compose, MVVM + Clean Architecture rules
paths:
  - "app/src/**"
---

# Android Development Rules

## Architecture (MANDATORY)
Follow Clean Architecture with three layers — never mix them:

- **Data layer** (`data/`) — Firebase calls, Room queries, repository implementations. No business logic here.
- **Domain layer** (`domain/`) — Use cases, models, repository interfaces. No Android dependencies (pure Kotlin).
- **Presentation layer** (`presentation/`) — ViewModels + Composables. No direct Firebase/Room calls.

Data flows only in one direction: Presentation → Domain → Data.

## Jetpack Compose

- Use `@Composable` functions for all UI — no XML layouts
- Keep Composables stateless where possible; lift state to ViewModel
- Use `StateFlow` in ViewModel, collect with `collectAsState()` in Composable
- Separate Screen Composables (full screens) from Component Composables (reusable UI)
- Always implement: loading state, error state, empty state
- Use `LazyColumn` / `LazyRow` for lists — never `Column` with `forEach` for dynamic content

## ViewModels

- One ViewModel per screen
- Expose state via a single `UiState` sealed class or data class
- All business logic goes through use cases, not directly in ViewModel
- Use `viewModelScope` for coroutines

```kotlin
// Pattern to follow:
data class MyScreenUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val data: List<Item> = emptyList()
)
```

## Kotlin Conventions

- Use `data class` for models
- Prefer `sealed class` for state and results
- Use `Result<T>` or a custom `Resource<T>` wrapper for data layer responses
- Use coroutines + Flow for async operations — no callbacks
- Extension functions for reusable logic that doesn't belong in a class

## Material 3

- Use Material 3 components from `androidx.compose.material3`
- Define colors, typography, and shapes in `presentation/theme/` — never hardcode values
- Support both light and dark themes from the start

## Navigation

- Use Navigation Compose (`androidx.navigation:navigation-compose`)
- Define all routes as constants or a sealed class, not raw strings
- Pass only primitive types or IDs between screens — fetch data in the destination ViewModel
