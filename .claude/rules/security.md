---
name: security
description: Secrets management, ProGuard, API key handling
paths:
  - "app/src/**"
  - "*.properties"
  - "app/google-services.json"
  - "app/build.gradle*"
---

# Security Rules

## Secrets Management

- NEVER commit `google-services.json`, API keys, or signing keystores to git
- Store sensitive values in `local.properties` (already in `.gitignore`)
- Access `local.properties` values via `BuildConfig` fields defined in `build.gradle.kts`
- Document all required secrets in `local.properties.example` with dummy values

```kotlin
// build.gradle.kts pattern:
buildConfigField("String", "MY_API_KEY", "\"${localProperties["MY_API_KEY"]}\"")
```

## ProGuard / R8

- Always enable minification and obfuscation for release builds
- Keep rules for: Firebase, Hilt, Retrofit models, Parcelables
- Test release builds before submitting to Play Store — ProGuard can break things silently

## Network Security

- Use HTTPS exclusively — no HTTP traffic
- Add `network_security_config.xml` to prevent cleartext traffic
- Certificate pinning for sensitive API endpoints

## Code Review Triggers

Any of these changes require explicit user approval before proceeding:
- Firestore Security Rules changes
- Authentication flow changes
- New external API integrations
- Changes to ProGuard rules that keep additional classes
