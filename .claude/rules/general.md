---
name: general
description: Project initialization, GitHub Issues workflow, git conventions
---

# General Project Rules

## New Project Detection (MANDATORY)
Before starting ANY work, check if the project has been initialized:
1. Read `docs/PRD.md` — if it still contains placeholder text like "_Describe what you are building_", the project is NOT initialized
2. Run `gh issue list --label "type:feature"` — if empty, no features have been defined

**If the project is not initialized:**
- Do NOT write any code
- Tell the user: "This project hasn't been set up yet. Run `/requirements` with a description of your feature (e.g. `/requirements I want to build a login screen with email + Google Sign-In`)."
- If the user already described their idea in the current message, run `/requirements` automatically

## GitHub Issues as Feature Tracker

Features are tracked as GitHub Issues, not local markdown files:

- **Creating:** `/requirements` skill creates the issue via `gh issue create`
- **Status updates:** Skills update labels via `gh issue edit --add-label / --remove-label`
- **Reading:** Always run `gh issue view <number>` to get current spec before starting work
- **Manual issues:** Users can create issues on github.com — always check `gh issue list` before assuming nothing exists

### Label Convention
| Label | Meaning |
|-------|---------|
| `type:feature` | New functionality |
| `type:bug` | Bug fix |
| `status:planned` | Spec written, ready to build |
| `status:in-progress` | Currently being built |
| `status:in-review` | QA testing in progress |
| `status:deployed` | Live in Play Store |

### Setup Labels (run once per repo)
```bash
gh label create "type:feature" --color "0075ca"
gh label create "type:bug" --color "d73a4a"
gh label create "status:planned" --color "e4e669"
gh label create "status:in-progress" --color "f9a825"
gh label create "status:in-review" --color "7057ff"
gh label create "status:deployed" --color "0e8a16"
```

## Git Conventions
- Commit format: `type(#issue): description` (e.g. `feat(#3): add login screen`)
- Types: feat, fix, refactor, test, docs, deploy, chore
- Always reference the GitHub issue number in commits

## Human-in-the-Loop
- Always ask for user approval before finalizing deliverables
- Present options using clear choices rather than open-ended questions
- Never proceed to the next workflow phase without user confirmation
