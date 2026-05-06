# Momento environment setup

This guide matches the current prototype stack: Expo React Native mobile app, Spring Boot backend, PostgreSQL-compatible database, Firebase Authentication, Google Maps, and private AWS S3 media storage.

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

## 2. Values still missing

These values are still blank in the environment snapshot and must be filled before the full prototype can run:

- `EXPO_PUBLIC_FIREBASE_API_KEY`
- `EXPO_PUBLIC_FIREBASE_APP_ID`
- `EXPO_PUBLIC_GOOGLE_MAPS_API_KEY`
- `DB_PASSWORD`
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `FIREBASE_SERVICE_ACCOUNT_PATH`

Also fix the backend Firebase project id typo: use `momento-project-app` consistently, not `momento-project-ap`.

## 3. Database note

The design documents require PostgreSQL with PostGIS for geospatial queries. If your Supabase database is PostgreSQL, it can work for the prototype as long as PostGIS is enabled and the schema is applied.

Run the schema from `database/schema.sql` after confirming PostGIS is available.

## 4. Backend run

From the backend folder:

```bash
export $(grep -v '^#' .env | xargs)
./gradlew bootRun
```

On Windows PowerShell:

```powershell
Get-Content .env | ForEach-Object {
  if ($_ -match '^[^#].+=') {
    $name, $value = $_ -split '=', 2
    [System.Environment]::SetEnvironmentVariable($name, $value)
  }
}
./gradlew bootRun
```

The Spring Boot application is already wired to read all required values from environment variables through `application.yml`.

## 5. Mobile run

From the mobile folder:

```bash
npm install
npx expo start
```

Expo will load the `EXPO_PUBLIC_*` values from `mobile/.env`. Use the LAN URL because the phone must reach the backend on your computer over the local network.

## 6. Google Maps and Firebase

- Enable Email/Password in Firebase Authentication.
- Download a Firebase Admin SDK service account JSON and place its absolute path in `FIREBASE_SERVICE_ACCOUNT_PATH`.
- Create a Google Maps API key and enable the mobile Maps SDK you need.

## 7. S3 storage

Keep the bucket private. The backend should use the AWS credentials, and the mobile app should never contain the AWS secret key.

## 8. Current blockers

Until the blank values are filled, the app can compile but the following areas will not work fully:

- Firebase login and session handling
- Google Maps display
- Backend database connection
- S3 media upload and signed URL generation
- Firebase Admin JWT verification
