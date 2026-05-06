# Momento Prototype

A full-stack prototype for the Momento location-based time capsule application.

## Stack
- Mobile: React Native with Expo
- Backend: Java 21 + Spring Boot
- Database: PostgreSQL + PostGIS
- Auth: Firebase Authentication
- Media: AWS S3 with pre-signed URLs
- Notifications: Firebase Cloud Messaging hooks
- Maps: react-native-maps / Google Maps provider

## Included
- Expo mobile app structure with authentication, map, create capsule, profile, history, and dropped capsules screens
- Spring Boot backend with Firebase JWT validation, capsule APIs, S3 upload/signing flow, PostGIS queries, expiry job, and notification hooks
- PostgreSQL/PostGIS schema and seed notes
- Environment templates and setup instructions

## Prototype decisions
- Email/password auth first; Google sign-in can be added later through Firebase
- Private S3 bucket only; media never returned as a permanent public URL
- Capsule media is unlocked through backend-authorized short-lived signed URLs
- Duplicate same-coordinate rule enforced server-side with a rounded coordinate constraint window
- Push notifications use opaque payloads only

## Run order
1. Create Firebase project and enable Email/Password auth
2. Create PostgreSQL database with PostGIS enabled
3. Create private S3 bucket and IAM user with limited object permissions
4. Configure backend `.env` values from `backend/src/main/resources/application-example.yml`
5. Start backend
6. Configure mobile `.env` from `mobile/.env.example`
7. Start Expo app

## What still needs real deployment secrets
- Firebase project credentials
- Google Maps API key
- AWS S3 bucket + IAM credentials
- FCM service account or server credentials

## Core flows implemented in code structure
- Register/login with Firebase on mobile
- Exchange Firebase ID token with backend-protected APIs
- Create profile row lazily on first authenticated backend call
- Drop capsule with text/photo/voice metadata and current GPS
- Request nearby capsules using PostGIS distance filtering
- Unlock only when within configured radius and not owner
- Return signed media URLs only after authorization check
- Record discoveries, extend expiry, award points, notify creator
- View dropped capsules, history, profile stats, delete own capsule


## Environment setup

Use `ENVIRONMENT_SETUP.md` for the current env template, run order, and missing-value checklist. The backend now reads its configuration from environment variables through `src/main/resources/application.yml`.
