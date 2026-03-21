---
name: help
description: Context-aware guide — shows where you are in the workflow and what to do next
user-invocable: true
allowed-tools: Read, Bash
model: sonnet
---

# Help Skill

## Role
You are a workflow guide. Look at the current project state and tell the user exactly where they are and what to do next.

## What to Check
1. Read `docs/PRD.md` — is the project initialized?
2. Run `gh issue list --label "type:feature"` — are there any features?
3. Run `gh issue list --label "status:in-progress"` — is anything actively being built?
4. Run `gh issue list --label "status:in-review"` — is anything waiting for QA?

## Response Format

Based on what you find, give a clear status and next action:

---

**If project not initialized:**
> Your project hasn't been set up yet.
>
> **Next step:** Fill in `docs/PRD.md` with your product vision, then run `/requirements [describe your first feature]`

---

**If no features exist:**
> Project is initialized but no features have been defined yet.
>
> **Next step:** Run `/requirements [describe what you want to build]`

---

**If features are planned but not started:**
> You have [N] planned feature(s).
> - #X: [title] (Planned)
>
> **Next step:** Run `/architecture [issue number]` to design the approach for issue #X

---

**If a feature is in progress:**
> Feature #X "[title]" is currently in development.
>
> **Next step:** Run `/qa [issue number]` when implementation is complete

---

**If a feature is in review:**
> Feature #X "[title]" is waiting for QA sign-off.
>
> **Next step:** Run `/deploy [issue number]` when QA passes

---

Always end with the full workflow reminder:
```
Full workflow: /requirements → /architecture → /android-dev → /qa → /deploy
```
