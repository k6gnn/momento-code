# Momento Prototype

A full-stack prototype for the Momento location-based time capsule application.

## Stack

| Layer | Technology |
|---|---|
| Mobile | React Native · **Expo SDK 54** |
| Backend | Java 21 · Spring Boot |
| Database | PostgreSQL + PostGIS |
| Auth | Firebase Authentication (email/password) |
| Media | AWS S3 (private bucket, pre-signed URLs) |
| Notifications | Firebase Cloud Messaging (scaffolded) |

---

## Included

- Expo mobile app with authentication, map discovery, create capsule, profile, history, and dropped-capsules screens
- Spring Boot backend with Firebase JWT validation, capsule CRUD, S3 upload/signing flow, PostGIS proximity queries, expiry job, and notification hooks
- PostgreSQL/PostGIS schema
- Environment templates and setup guide

---

## Dependency versions (Expo SDK 54)

All mobile dependencies are pinned to versions compatible with **Expo SDK 54 / React Native 0.79.7**:

| Package | Version |
|---|---|
| `expo` | `~54.0.0` |
| `react` | `19.0.0` |
| `react-native` | `0.79.7` |
| `expo-location` | `~18.0.10` |
| `expo-image-picker` | `~16.0.6` |
| `expo-notifications` | `~0.29.14` |
| `expo-av` | `~15.0.2` |
| `expo-file-system` | `~18.0.12` |
| `expo-status-bar` | `~2.2.3` |
| `expo-asset` | `~11.0.4` |
| `react-native-safe-area-context` | `4.12.0` |
| `react-native-screens` | `~4.4.0` |
| `@react-native-async-storage/async-storage` | `2.1.0` |

> Do **not** upgrade these packages independently. Always run `npx expo install <package>` so Expo resolves the correct version for your SDK.

---

## Run order

1. Create Firebase project and enable **Email/Password** auth
2. Create PostgreSQL database with PostGIS enabled; run `database/schema.sql`
3. Create private S3 bucket and IAM user with `s3:GetObject` + `s3:PutObject`
4. Fill in `backend/.env` (see `ENVIRONMENT_SETUP.md`)
5. Start backend: `./gradlew bootRun` from the `backend/` folder
6. Fill in `mobile/.env` (see `ENVIRONMENT_SETUP.md`)
7. `npm install && npx expo start` from the `mobile/` folder

---

## Architecture decisions

- **Auth persistence**: Firebase Auth is initialized with `getReactNativePersistence(AsyncStorage)` so the session survives app restarts without requiring a fresh sign-in.
- **Token refresh**: The Axios request interceptor calls `getIdToken(false)` — reuses the cached token if valid, silently refreshes only when needed.
- **Network error normalisation**: All network/timeout errors produce a human-readable message so screens can show `e.message` directly in alerts.
- **Image picker**: Uses `ImagePicker.MediaType.images` (SDK 16+ API, not the deprecated `MediaTypeOptions`).
- **Location cleanup**: `useLocation` uses a `ref` to track the `watchPositionAsync` subscription so it is always removed on unmount, even when assigned asynchronously.
- **Keyboard avoidance**: `Screen` wraps scrollable content in `KeyboardAvoidingView` so text inputs are never hidden by the keyboard.
- **Delete confirmation**: The Dropped Capsules screen shows a confirmation dialog before deleting; per-item loading state prevents double-taps.
- **S3 privacy**: Media is never exposed as permanent public URLs; only short-lived signed URLs are returned after a proximity-verified unlock.

---

## What still needs real deployment secrets

- Firebase project credentials (`apiKey`, `appId`)
- Firebase Admin SDK service account JSON path
- Google Maps API key (for a future full map view)
- AWS S3 bucket + IAM credentials
- Database password

See `ENVIRONMENT_SETUP.md` for the full checklist.

---

## Known prototype trade-offs

- Media upload sends metadata only; switch to multipart upload or direct presigned PUT for production.
- Notification service is scaffolded but not fully wired (FCM token registration + send calls needed).
- No Google / Apple sign-in yet; email/password is the only provider.
- No rate limiting, audit logs, or anti-spoofing on the mobile side.
