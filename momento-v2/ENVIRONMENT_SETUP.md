# Momento — Environment Setup

This guide covers the current stack: **Expo SDK 54** React Native mobile app, Spring Boot backend, PostgreSQL + PostGIS database, Firebase Authentication, and private AWS S3 media storage.

---

## 1. Create local env files

### Mobile: `mobile/.env`

```env
EXPO_PUBLIC_API_BASE_URL=http://192.168.1.135:8080/api
EXPO_PUBLIC_FIREBASE_API_KEY=
EXPO_PUBLIC_FIREBASE_AUTH_DOMAIN=momento-project-app.firebaseapp.com
EXPO_PUBLIC_FIREBASE_PROJECT_ID=momento-project-app
EXPO_PUBLIC_FIREBASE_STORAGE_BUCKET=momento-project-app.firebasestorage.app
EXPO_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=859202829327
EXPO_PUBLIC_FIREBASE_APP_ID=
EXPO_PUBLIC_GOOGLE_MAPS_API_KEY=
```

> **Replace `192.168.1.135`** with your computer's actual LAN IP address.  
> The mobile device and your computer must be on the same Wi-Fi network.

### Backend: `backend/.env`

```env
DB_URL=jdbc:postgresql://db.zpokigzetfhahllsffvs.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=
SERVER_PORT=8080
ALLOWED_ORIGINS=http://localhost:8081
PROXIMITY_RADIUS_METERS=50
DROP_POINTS=10
DISCOVERY_POINTS=15
AWS_REGION=us-east-1
AWS_BUCKET=momento-app-uploads
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_PRESIGNED_URL_MINUTES=15
FIREBASE_PROJECT_ID=momento-project-app
FIREBASE_SERVICE_ACCOUNT_PATH=
NOTIFICATIONS_ENABLED=false
```

---

## 2. Values still required before running

| Variable | Where needed |
|---|---|
| `EXPO_PUBLIC_FIREBASE_API_KEY` | Mobile |
| `EXPO_PUBLIC_FIREBASE_APP_ID` | Mobile |
| `EXPO_PUBLIC_GOOGLE_MAPS_API_KEY` | Mobile (for future map view) |
| `DB_PASSWORD` | Backend |
| `AWS_ACCESS_KEY_ID` | Backend |
| `AWS_SECRET_ACCESS_KEY` | Backend |
| `FIREBASE_SERVICE_ACCOUNT_PATH` | Backend |

---

## 3. Database

PostgreSQL with PostGIS must be enabled. If you are using Supabase, run the schema from `database/schema.sql` inside the Supabase SQL editor after confirming PostGIS is available.

---

## 4. Backend run

**macOS / Linux:**
```bash
cd backend
export $(grep -v '^#' .env | xargs)
./gradlew bootRun
```

**Windows PowerShell:**
```powershell
cd backend
Get-Content .env | ForEach-Object {
  if ($_ -match '^[^#].+=') {
    $name, $value = $_ -split '=', 2
    [System.Environment]::SetEnvironmentVariable($name, $value)
  }
}
./gradlew bootRun
```

---

## 5. Mobile run

```bash
cd mobile
npm install
npx expo start
```

Expo loads all `EXPO_PUBLIC_*` values from `mobile/.env` automatically.  
Use the **LAN** URL (not Tunnel) so the phone can reach the backend on your local network.

---

## 6. Firebase setup

1. Create a Firebase project and enable **Email/Password** sign-in.
2. Copy `apiKey`, `authDomain`, `projectId`, `storageBucket`, `messagingSenderId`, and `appId` into `mobile/.env`.
3. Download the **Admin SDK service account JSON** and set `FIREBASE_SERVICE_ACCOUNT_PATH` to its absolute path on the machine running the backend.

---

## 7. AWS S3 setup

- Keep the bucket **private** — the app never directly exposes S3 URLs.
- The backend generates short-lived signed URLs (default 15 min) after authorizing an unlock.
- The IAM user needs only `s3:GetObject` and `s3:PutObject` on the bucket.

---

## 8. Current feature blockers (won't work without credentials)

| Feature | Blocked until |
|---|---|
| Firebase login / registration | `EXPO_PUBLIC_FIREBASE_API_KEY` + `EXPO_PUBLIC_FIREBASE_APP_ID` |
| Auth persistence across restarts | same (handled via AsyncStorage once Firebase is configured) |
| Backend database connection | `DB_PASSWORD` |
| S3 media upload + signed URLs | `AWS_ACCESS_KEY_ID` + `AWS_SECRET_ACCESS_KEY` |
| Backend JWT verification | `FIREBASE_SERVICE_ACCOUNT_PATH` |
