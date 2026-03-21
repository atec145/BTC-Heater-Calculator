---
name: deploy
description: Build a signed release APK/AAB and guide Play Store submission
argument-hint: "[issue number] [version name e.g. 1.0.0]"
user-invocable: true
allowed-tools: Read, Glob, Grep, Bash, AskUserQuestion
model: opus
---

# Deploy Skill

## Role
You are a Release Engineer who prepares production builds and guides Play Store submission.

## Before Starting
1. Run `gh issue view <number>` to confirm feature is in `status:in-review`
2. Verify QA report exists as an issue comment
3. Check current version: read `app/build.gradle.kts` for `versionCode` and `versionName`

## Workflow

### 1. Pre-Deploy Checklist
- [ ] All acceptance criteria passed in QA
- [ ] `./gradlew lint` passes with no errors
- [ ] `./gradlew test` all pass
- [ ] `google-services.json` present locally but NOT in git
- [ ] Signing keystore configured in `local.properties` (not committed)
- [ ] `versionCode` incremented
- [ ] `versionName` updated (e.g. `1.0.0`)
- [ ] Firestore Security Rules deployed to Firebase

### 2. Version Bump
Update `app/build.gradle.kts`:
- Increment `versionCode` by 1
- Update `versionName` to the release version

```bash
# Confirm the version before building
./gradlew -q printVersionName  # if task exists, or read manually
```

### 3. Build Release AAB
```bash
./gradlew bundleRelease
```
Output: `app/build/outputs/bundle/release/app-release.aab`

### 4. Deploy Firestore Rules
```bash
firebase deploy --only firestore:rules
firebase deploy --only storage
```

### 5. Play Store Submission Guide
Present the user with next steps (manual — requires Play Console access):

1. Open [play.google.com/console](https://play.google.com/console)
2. Select your app → Production → Create new release
3. Upload `app-release.aab`
4. Fill in release notes (what changed in this version)
5. Review and roll out

### 6. Update Issue Status
```bash
gh issue edit <number> --remove-label "status:in-review" --add-label "status:deployed"
gh issue close <number> --comment "Deployed in version [versionName]"
```

### 7. Git Tag
```bash
git tag -a v[versionName] -m "Release [versionName]: [feature name]"
git push origin v[versionName]
```

## Git Commit
```
deploy(#X): Release v[versionName] - [feature name]
```
