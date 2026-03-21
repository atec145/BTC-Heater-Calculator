---
name: firebase
description: Firebase Auth, Firestore, Storage rules and patterns
paths:
  - "app/src/**/remote/**"
  - "app/src/**/repository/**"
  - "firestore.rules"
  - "storage.rules"
---

# Firebase Rules

## Firestore Security Rules (MANDATORY)

- ALWAYS write Firestore Security Rules before or alongside data access code
- Default rule must be `deny all` — never leave rules open
- Users may only read/write their own documents unless explicitly shared
- Validate data types and required fields in rules, not just in the app

```
// Minimum rule pattern:
match /users/{userId} {
  allow read, write: if request.auth != null && request.auth.uid == userId;
}
```

## Authentication

- Check `FirebaseAuth.currentUser` before every Firestore operation
- Use `FirebaseUser.uid` as the primary key for user documents
- Handle all auth states: signed in, signed out, loading
- For Google Sign-In: always use the official `GoogleSignInClient`, never store tokens manually

## Firestore Patterns

- Structure collections for your query patterns — Firestore doesn't support JOINs
- Use subcollections for one-to-many relationships (e.g. `users/{uid}/posts/{postId}`)
- Always paginate list queries with `.limit()` — never fetch unbounded collections
- Use `snapshots()` (Flow) for realtime data, `get()` for one-time reads
- Wrap Firestore calls in `try/catch` and map to `Result<T>` in the data layer

## Data Mapping

- Never expose Firestore `DocumentSnapshot` to the domain or presentation layer
- Map Firestore documents to domain models in the data layer
- Use `@ServerTimestamp` for creation/update timestamps — never set them from the client

## Storage

- Always set Storage Security Rules — default open rules are not acceptable
- Validate file type and size before upload
- Use signed URLs for sensitive files
