# Implementation Notes

## Prototype scope

Implements all required flows: authentication, capsule creation, nearby map discovery, proximity unlock, points/profile, discovery history, dropped capsules, expiry, and deletion.

## Security choices

- Firebase ID tokens are validated by the backend on every request.
- S3 remains private; media is returned only as short-lived signed URLs after the backend authorises an unlock.
- Self-discovery is blocked server-side.
- Discoveries are unique per user–capsule pair (no double-unlock).
- Opaque notification payloads assumed throughout.

## Expo SDK 54 compatibility

All dependencies are aligned to Expo SDK 54 / React Native 0.79.7. Notable changes from older SDKs:

- `expo-image-picker` v16: use `ImagePicker.MediaType.images` (not the deprecated `MediaTypeOptions.Images`).
- `expo-status-bar` v2: API unchanged; version bump only.
- `react-native-safe-area-context` 4.12: `SafeAreaView` accepts an `edges` prop.
- Firebase Auth persistence: use `initializeAuth` with `getReactNativePersistence(AsyncStorage)` instead of `getAuth` alone. This requires `@react-native-async-storage/async-storage` 2.1.0.

## Fixes applied in this version

| Area | Issue | Fix |
|---|---|---|
| `package.json` | Wrong dependency versions for SDK 54 (`react 19.1.0`, `react-native 0.81.5`, wrong expo-* versions) | Pinned all packages to SDK-54-compatible versions |
| `app.json` | Missing plugin declarations, permissions, and splash/icon config | Added `expo-location`, `expo-image-picker`, `expo-notifications` plugins with permission strings; added iOS `infoPlist` entries; added `newArchEnabled` |
| `AuthContext.js` | `getAuth()` used instead of `initializeAuth` with persistence — auth state lost on app restart | Switched to `initializeAuth` + `getReactNativePersistence(AsyncStorage)` |
| `api/client.js` | No response interceptor — network errors showed raw Axios errors | Added response interceptor that converts network failures to a human-readable message; added Firebase init guard |
| `useLocation.js` | Subscription stored in local `let` — cleanup callback captured stale reference | Moved subscription to `useRef`; added `isMounted` guard for async safety |
| `Screen.js` | No `KeyboardAvoidingView` — keyboard covered inputs | Added `KeyboardAvoidingView` + `keyboardShouldPersistTaps="handled"` |
| `FormControls.js` | No `loading` or `disabled` prop on Button — double-submit possible | Added `loading` (spinner), `disabled`, and `danger` kind; fixed multiline input height |
| `CreateCapsuleScreen.js` | No media library permission request; used deprecated `MediaTypeOptions` | Added `requestMediaLibraryPermissionsAsync()`; switched to `MediaType.images` |
| `LoginScreen.js` | No loading state or input validation | Added `loading`, `disabled`, field validation, autofill attributes |
| `RegisterScreen.js` | No loading state, no password length check | Added `loading`, `disabled`, min-length validation |
| `MapScreen.js` | `load` recreated every render causing infinite effect loop; no pull-to-refresh | Wrapped `load` in `useCallback`; added `RefreshControl` |
| `CapsuleDetailScreen.js` | No guard for missing `currentLocation`; media URLs not tappable | Added null guard; `Linking.openURL` on media; loading state |
| `DroppedCapsulesScreen.js` | No delete confirmation; no per-item loading; unhandled error on load | Added `Alert.alert` confirmation; per-item `deletingId` state; error handling |
| `HistoryScreen.js` | Unhandled error on fetch; no loading or empty state | Added loading/error/empty states; pull-to-refresh |
| `ProfileScreen.js` | No sign-out confirmation; used `profile?.username` without fallback to email | Added confirmation dialog; falls back to `user.email`; loading/refresh states |
| `App.js` | Tab bar unstyled; `NAV_THEME` object inlined in JSX | Extracted `NAV_THEME` constant; added tab bar colour theming; added `animation: 'fade'` |

## Production upgrade path

1. Replace placeholder media upload with presigned PUT upload from app to S3.
2. Add FCM token registration and actual notification send calls.
3. Add rolling 24-hour duplicate drop rule via a database trigger.
4. Add anti-spoofing, rate limits, audit logs, and DTO validation hardening.
5. Add account deletion and GDPR data export flows.
6. Add Google / Apple sign-in via Firebase.
