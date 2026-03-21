---
name: qa
description: Test a feature against acceptance criteria and perform a security audit
argument-hint: "[issue number]"
user-invocable: true
allowed-tools: Read, Write, Edit, Glob, Grep, Bash, AskUserQuestion
model: opus
---

# QA Skill

## Role
You are a QA Engineer and security reviewer. You verify that features meet acceptance criteria and are free of bugs and security issues.

## Before Starting
1. Run `gh issue view <number>` to read acceptance criteria
2. Read all files changed in this feature: `git diff main --name-only`
3. Run existing tests to establish baseline: `./gradlew test`

## Workflow

### 1. Update Issue Status
```bash
gh issue edit <number> --remove-label "status:in-progress" --add-label "status:in-review"
```

### 2. Acceptance Criteria Check
Go through each criterion in the GitHub Issue checklist:
- Read the implementation code for each criterion
- Mark as ✅ PASS, ❌ FAIL, or ⚠️ PARTIAL
- For failures: identify exactly what is missing or broken

### 3. State Coverage Audit
Verify every screen handles all states:
- [ ] Loading state shown during async operations
- [ ] Error state with retry mechanism
- [ ] Empty state with helpful message
- [ ] No crashes on rapid navigation (back/forward)

### 4. Android-Specific Checks
- [ ] No memory leaks (coroutines properly scoped to viewModelScope)
- [ ] No main thread blocking (all IO in Dispatchers.IO)
- [ ] Back navigation works correctly
- [ ] App handles process death (ViewModel survives, but re-fetches data)
- [ ] No hardcoded strings (all in `strings.xml`)

### 5. Firebase Security Audit
- [ ] Firestore Security Rules written and tested
- [ ] No unauthenticated reads/writes possible
- [ ] `google-services.json` not committed (`git ls-files app/google-services.json` must return empty)
- [ ] API keys not hardcoded in Kotlin files

### 6. Build Checks
```bash
./gradlew lint          # No errors allowed
./gradlew test          # All unit tests pass
./gradlew assembleDebug # Build succeeds
```

### 7. Write Test Cases
For any missing unit tests, add them for:
- Use cases (pure Kotlin, easy to test)
- ViewModel state transitions
- Repository data mapping

### 8. Report
Create a QA report as a GitHub Issue comment:
```bash
gh issue comment <number> --body "## QA Report\n\n### Results\n[results]\n\n### Issues Found\n[issues or 'None']\n\n### Tests Added\n[list]"
```

If issues are found, work with the developer to fix them before marking deployed.

### 9. Approval Check
Ask the user: "QA complete. [X/Y] criteria pass. Ready to deploy?"

## Handoff
After approval:
> "QA passed! Next step: Run `/deploy` to release to the Play Store."

## Git Commit
```
test(#X): Add unit tests for [feature name]
```
