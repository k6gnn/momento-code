# MOMENTO – LOCATION-BASED TIME CAPSULE APPLICATION
## TEST REPORT

**Version 1.0**

---

## Contents

1. [Introduction](#1-introduction)
   - 1.1 [Definitions of the abbreviations](#11-definitions-of-the-abbreviations)
   - 1.2 [Document purpose](#12-document-purpose)
   - 1.3 [Related Documents](#13-related-documents)
   - 1.4 [General requirements for test execution](#14-general-requirements-for-test-execution)
2. [Functions that need testing](#2-functions-that-need-testing)
3. [Test Scenarios](#3-test-scenarios)
   - 3.1 [User Authentication](#31-user-authentication)
   - 3.2 [Capsule Creation](#32-capsule-creation)
   - 3.3 [Map Discovery](#33-map-discovery)
   - 3.4 [Capsule Unlock](#34-capsule-unlock)
   - 3.5 [Discovery History](#35-discovery-history)
   - 3.6 [Dropped Capsules Management](#36-dropped-capsules-management)
   - 3.7 [User Profile](#37-user-profile)

---

## 1. Introduction

### 1.1 Definitions of the abbreviations

| Abbreviation | Explanation |
|---|---|
| Momento | Location-based time capsule mobile application |
| API | Application Programming Interface |
| GPS | Global Positioning System |
| JWT | JSON Web Token |
| FCM | Firebase Cloud Messaging |
| S3 | Amazon Simple Storage Service |
| PostGIS | Spatial database extension for PostgreSQL |
| UUID | Universally Unique Identifier |
| UI | User Interface |
| iOS | Apple mobile operating system |
| Android | Google mobile operating system |

### 1.2 Document purpose

This document describes the test report for the Momento location-based time capsule application (hereinafter "Momento").

The document is intended for the parties involved in testing and maintenance of the software:

- Instructors and evaluators responsible for accepting and evaluating the project deliverables.
- Technical members of the development team responsible for implementation, design, and maintenance.

Momento allows users to "drop" digital time capsules — containing text messages and photos — at a specific GPS location. Other users who physically travel to that location can then discover and unlock the capsule, earning points in the process.

### 1.3 Related Documents

This test report has been developed based on the following documents:

- [1] Software Architecture Description (`ARCHITECTURE.md`)
- [2] Environment Setup Guide (`ENVIRONMENT_SETUP.md`)
- [3] Project README (`README.md`)
- [4] Database Schema (`database/schema.sql`)

### 1.4 General requirements for test execution

In order to test the operation of the system, the tester needs:

- A physical or virtual mobile device (iOS or Android) with the Expo Go application installed, or a built Expo development client.
- A running backend instance (Spring Boot) accessible over the network.
- A configured PostgreSQL database with the PostGIS extension and the applied schema.
- A valid Firebase project with Email/Password authentication enabled.
- A configured AWS S3 bucket for media storage.
- At least two registered user accounts for testing self-discovery blocking scenarios.
- A device with GPS / location services enabled.

---

## 2. Functions that need testing

| No. | Description | Requirement |
|---|---|---|
| 1 | User Authentication | MOMENTO-1 |
| 2 | Capsule Creation | MOMENTO-2 |
| 3 | Map Discovery (Nearby Capsules) | MOMENTO-3 |
| 4 | Capsule Unlock (Proximity Gate) | MOMENTO-4 |
| 5 | Discovery History | MOMENTO-5 |
| 6 | Dropped Capsules Management | MOMENTO-6 |
| 7 | User Profile | MOMENTO-7 |

---

## 3. Test Scenarios

### 3.1 User Authentication

#### 3.1.1 Checking that required fields are validated on login

- Open the Login screen.
- Leave the Email and Password fields empty.
- Press the "Sign in" button.

Expected result: An alert is displayed with the message "Missing fields – Please enter your email and password."

Test result: **Passed**

---

#### 3.1.2 Checking that an incorrect email or password is rejected

- Open the Login screen.
- Enter the value "wrong@test.com" in the Email field.
- Enter the value "wrongpassword" in the Password field.
- Press the "Sign in" button.

Expected result: An alert is displayed with the message "Login failed" and the Firebase error reason.

Test result: **Passed**

---

#### 3.1.3 Successful user login

- Open the Login screen.
- Enter a valid registered email address in the Email field.
- Enter the correct password in the Password field.
- Press the "Sign in" button.

Expected result: The user is authenticated and automatically navigated to the main application screen (Map tab). The session persists after closing and reopening the app.

Test result: **Passed**

---

#### 3.1.4 Checking that required fields are validated on registration

- Open the Register screen via the "Create account" button.
- Leave the Email and Password fields empty.
- Press the "Register" button.

Expected result: An alert is displayed with the message "Missing fields – Please fill in all fields."

Test result: **Passed**

---

#### 3.1.5 Checking password length validation on registration

- Open the Register screen.
- Enter a valid email address.
- Enter a password shorter than 6 characters (e.g. "abc").
- Press the "Register" button.

Expected result: An alert is displayed with the message "Weak password – Password must be at least 6 characters."

Test result: **Passed**

---

#### 3.1.6 Successful user registration

- Open the Register screen.
- Enter a new, unused email address.
- Enter a valid password of at least 6 characters.
- Press the "Register" button.

Expected result: The account is created in Firebase and the user is automatically navigated to the main application screen.

Test result: **Passed**

---

#### 3.1.7 User sign-out with confirmation

- Navigate to the Profile tab.
- Press the "Sign out" button.
- A confirmation dialog appears — press "Sign out" to confirm.

Expected result: The user is signed out and the Login screen is displayed.

Test result: **Passed**

---

#### 3.1.8 Cancelling sign-out

- Navigate to the Profile tab.
- Press the "Sign out" button.
- A confirmation dialog appears — press "Cancel".

Expected result: The dialog dismisses and the user remains signed in on the Profile screen.

Test result: **Passed**

---

### 3.2 Capsule Creation

Sign in with a valid user account. Navigate to the Create tab.

#### 3.2.1 Blocking capsule creation without GPS fix

- Open the Create screen before location has been acquired.
- Press the "Drop capsule" button immediately.

Expected result: An alert is displayed with the message "Location required – Waiting for GPS fix. Try again in a moment."

Test result: **Passed**

---

#### 3.2.2 Blocking creation of an empty capsule

- Open the Create screen after a GPS fix has been obtained.
- Leave the Message field empty and do not select a photo.
- Press the "Drop capsule" button.

Expected result: An alert is displayed with the message "Empty capsule – Add a message or photo before dropping."

Test result: **Passed**

---

#### 3.2.3 Creating a text-only capsule

- Open the Create screen.
- Wait for the GPS coordinates to appear at the top of the screen.
- Enter a message in the Message field (e.g. "Hello from this spot!").
- Press the "Drop capsule" button.

Expected result: An alert is displayed confirming "Capsule dropped! Your capsule was saved at your current location." The text fields are cleared after successful creation.

Test result: **Passed**

---

#### 3.2.4 Selecting a photo from the library

- Open the Create screen.
- Press the "Choose photo" button.
- Grant media library permissions when prompted.
- Select a photo from the library.

Expected result: The button label changes to "Photo selected — tap to change" indicating a photo has been attached.

Test result: **Passed**

---

#### 3.2.5 Creating a capsule with a photo

- Open the Create screen and wait for GPS.
- Select a photo using the "Choose photo" button.
- Optionally add a text message.
- Press the "Drop capsule" button.

Expected result: The capsule is created with the attached photo uploaded to S3. A success alert is displayed and both fields are cleared.

Test result: **Passed**

---

#### 3.2.6 Blocking duplicate capsule at the same location on the same day

- Drop a capsule at the current location.
- Without moving, attempt to drop another capsule at the exact same coordinates.

Expected result: The server returns an error and an alert is displayed indicating a duplicate drop constraint was violated.

Test result: **Passed**

---

### 3.3 Map Discovery

Sign in with a valid user account. Navigate to the Map tab.

#### 3.3.1 Displaying nearby capsules

- Navigate to the Map tab.
- Wait for the GPS fix and the capsule list to load.

Expected result: A list of nearby capsules is displayed. Each entry shows the distance in metres and the GPS coordinates of the capsule.

Test result: **Passed**

---

#### 3.3.2 Displaying empty state when no capsules are nearby

- Navigate to the Map tab from a location with no dropped capsules within 50 metres.

Expected result: An empty state is shown with the message "No capsules nearby yet. Drop one and come back!"

Test result: **Passed**

---

#### 3.3.3 Displaying GPS error when location is denied

- Deny location permissions on the device.
- Open the Map tab.

Expected result: An error message is shown explaining that location access was denied.

Test result: **Passed**

---

#### 3.3.4 Refreshing the capsule list

- Navigate to the Map tab with at least one nearby capsule.
- Pull down on the capsule list to trigger a refresh, or press the "Refresh" button.

Expected result: The list reloads and displays the current nearby capsules. The refresh indicator is visible during loading and disappears when complete.

Test result: **Passed**

---

#### 3.3.5 Navigating to capsule detail

- Navigate to the Map tab.
- Tap on a capsule entry in the list.

Expected result: The Capsule Detail screen opens showing the distance of the selected capsule and an "Try unlock" button.

Test result: **Passed**

---

### 3.4 Capsule Unlock

Sign in with a valid user account. Navigate to the Map tab and open a nearby capsule.

#### 3.4.1 Successful proximity unlock

- Navigate to the Capsule Detail screen for a capsule within the unlock radius (50 metres).
- Press the "Try unlock" button.

Expected result: The capsule is unlocked. The screen shows the text content and any attached media. Points are awarded and the discovery is recorded.

Test result: **Passed**

---

#### 3.4.2 Unlock blocked outside proximity radius

- Navigate to the Capsule Detail screen for a capsule that is more than 50 metres away.
- Press the "Try unlock" button.

Expected result: An alert is displayed with the message "Unlock failed" indicating the user is not close enough to unlock.

Test result: **Passed**

---

#### 3.4.3 Unlock blocked without GPS fix

- Open the Capsule Detail screen when location is unavailable.
- Press the "Try unlock" button.

Expected result: An alert is displayed with the message "Location unavailable – Cannot unlock without a GPS fix."

Test result: **Passed**

---

#### 3.4.4 Self-discovery blocked

- Drop a capsule as User A at a specific location.
- While still signed in as User A, navigate to the Map tab and open that same capsule.
- Press the "Try unlock" button.

Expected result: An alert is displayed indicating the server blocked the unlock because users cannot discover their own capsules.

Test result: **Passed**

---

#### 3.4.5 Duplicate unlock blocked

- Unlock a capsule as User B.
- Without navigating away, attempt to press "Try unlock" again, or return to the same capsule and try again.

Expected result: The server rejects the second unlock attempt. An alert is displayed indicating the capsule was already discovered by this user.

Test result: **Passed**

---

#### 3.4.6 Viewing media after unlock

- Successfully unlock a capsule that contains a photo.
- The unlocked view shows "View media ›" link.
- Tap the link.

Expected result: The device opens the pre-signed S3 URL in the browser or media viewer, displaying the attached photo.

Test result: **Passed**

---

### 3.5 Discovery History

Sign in with a valid user account. Navigate to the History tab.

#### 3.5.1 Displaying discovery history

- Navigate to the History tab after having previously unlocked at least one capsule.

Expected result: A list of past discoveries is displayed. Each entry shows the discovery date and the points awarded for that discovery.

Test result: **Passed**

---

#### 3.5.2 Displaying empty state when no discoveries exist

- Sign in with a new user account that has not yet unlocked any capsules.
- Navigate to the History tab.

Expected result: An empty state is shown with the message "No discoveries yet. Find capsules near you on the Map tab!"

Test result: **Passed**

---

#### 3.5.3 Refreshing the history list

- Navigate to the History tab.
- Pull down on the list to trigger a refresh.

Expected result: The list reloads and displays the latest discovery history. The refresh indicator is visible during loading.

Test result: **Passed**

---

### 3.6 Dropped Capsules Management

Sign in with a valid user account. Navigate to the My Capsules tab.

#### 3.6.1 Displaying dropped capsules list

- Navigate to the My Capsules tab after having previously dropped at least one capsule.

Expected result: A list of the user's own capsules is displayed. Each entry shows the capsule status (ACTIVE / EXPIRED) and its expiry date.

Test result: **Passed**

---

#### 3.6.2 Displaying empty state when no capsules have been dropped

- Sign in with a new user account that has not yet dropped any capsules.
- Navigate to the My Capsules tab.

Expected result: An empty state is shown with the message "No capsules dropped yet."

Test result: **Passed**

---

#### 3.6.3 Delete confirmation dialog

- Navigate to the My Capsules tab with at least one capsule.
- Press the "Delete" button next to a capsule.

Expected result: A confirmation dialog is shown with the message "Delete capsule – This cannot be undone. Are you sure?" with Cancel and Delete options.

Test result: **Passed**

---

#### 3.6.4 Cancelling a deletion

- Press the "Delete" button next to a capsule.
- In the confirmation dialog, press "Cancel".

Expected result: The dialog dismisses and the capsule remains in the list unchanged.

Test result: **Passed**

---

#### 3.6.5 Successfully deleting a capsule

- Press the "Delete" button next to a capsule.
- In the confirmation dialog, press "Delete" to confirm.

Expected result: The capsule is permanently removed from the database (and from S3 if it contained media). The item disappears from the list without requiring a manual refresh.

Test result: **Passed**

---

#### 3.6.6 Refreshing the capsules list

- Navigate to the My Capsules tab.
- Pull down on the list to trigger a refresh.

Expected result: The list reloads with the current state of the user's dropped capsules.

Test result: **Passed**

---

### 3.7 User Profile

Sign in with a valid user account. Navigate to the Profile tab.

#### 3.7.1 Displaying profile statistics

- Navigate to the Profile tab.

Expected result: The profile card displays the user's username (or email if no username is set), total points, number of capsules dropped, and number of capsules discovered.

Test result: **Passed**

---

#### 3.7.2 Profile statistics accuracy after dropping a capsule

- Note the current "Dropped" count on the Profile tab.
- Navigate to the Create tab and drop a new capsule.
- Return to the Profile tab and pull to refresh.

Expected result: The "Dropped" count is incremented by 1 compared to the value noted before.

Test result: **Passed**

---

#### 3.7.3 Profile statistics accuracy after discovering a capsule

- Note the current "Discovered" count and "Points" total on the Profile tab.
- Unlock a nearby capsule as a different user (User B discovering User A's capsule).
- Navigate to the Profile tab and pull to refresh.

Expected result: The "Discovered" count is incremented by 1 and the "Points" total increases by the awarded points.

Test result: **Passed**

---

#### 3.7.4 Refreshing the profile

- Navigate to the Profile tab.
- Pull down on the screen to trigger a refresh.

Expected result: The profile statistics reload and display up-to-date values.

Test result: **Passed**

---

*Version: 1.0 — 2026-05-06*
