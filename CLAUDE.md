# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Android App Starter Kit

> A Kotlin + Jetpack Compose template with an AI-powered development workflow. Feature tracking via GitHub Issues.

## Tech Stack

- **Language:** Kotlin + Coroutines
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM + Clean Architecture
- **Auth + Database:** Firebase (Auth, Firestore, Storage)
- **Dependency Injection:** Hilt
- **Navigation:** Navigation Compose
- **Local Cache:** Room
- **Image Loading:** Coil
- **CI/CD:** GitHub Actions → Play Store

## Project Structure

```
app/src/main/java/com/example/app/
  data/               Data layer
    local/            Room database + DAOs
    remote/           Firebase data sources
    repository/       Repository implementations
  domain/             Business logic
    model/            Data classes / entities
    usecase/          Use cases (one action per class)
    repository/       Repository interfaces (abstractions)
  presentation/       UI layer
    screens/          Screen Composables + ViewModels
    components/       Reusable Composables
    theme/            Material Theme (Color, Typography, Shape)
  di/                 Hilt modules
docs/
  PRD.md              Product Requirements Document
.github/
  ISSUE_TEMPLATE/     GitHub Issue templates for features + bugs
```

## Development Workflow

1. `/requirements` — Describe feature → creates GitHub Issue with spec
2. `/architecture` — Design tech decisions → updates issue with design
3. `/android-dev` — Build Kotlin/Compose implementation
4. `/qa` — Test + security audit → updates issue to In Review
5. `/deploy` — Build release + Play Store upload

## Feature Tracking via GitHub Issues

Features live as GitHub Issues, not local files:

- **Labels:** `status:planned`, `status:in-progress`, `status:in-review`, `status:deployed`, `type:feature`, `type:bug`
- **Commit format:** `feat(#12): description` (referencing issue number)
- Skills create and update issues automatically via `gh` CLI
- You can also create issues manually on github.com — skills will pick them up

Useful commands:
```bash
gh issue list --label "type:feature"          # All features
gh issue list --label "status:in-progress"    # Active work
gh issue view <number>                         # Read a specific issue
```

## Build Commands

```bash
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests (emulator required)
./gradlew lint                   # Lint
```

## Firebase Setup

Firebase is not active until configured:

1. Create project at console.firebase.google.com
2. Add an Android app (use your package name)
3. Download `google-services.json` → place in `app/`
4. Enable Authentication and Firestore in Firebase console
5. `google-services.json` is in `.gitignore` — never commit it

## Path-Scoped Rules

Detailed rules in `.claude/rules/`, auto-applied by file path:
- `general.md` — project init, GitHub Issues workflow, git conventions
- `android.md` — Kotlin, Compose, MVVM patterns (`app/src/**`)
- `firebase.md` — Firestore, Auth, Security Rules
- `security.md` — secrets management, ProGuard, API keys
