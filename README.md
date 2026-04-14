# Baureihensammler

Android app for collecting train classes ("Baureihen"), tracking discoveries, and attaching your own snapshots.

## Features

- Search train classes by BR number (including aliases).
- Handles overlapping BR numbers with vehicle number disambiguation (for example `445`).
- Save/remove entries in your personal collection.
- Points are calculated from estimated fleet size.
- Optional photo workflow with camera capture and 16:9 crop.
- Optional Wikipedia short summaries with quick link to full article.
- Privacy switch to disable external Wikipedia summary requests.
- Session-based debug logs (in cache) with in-app log viewer and copy-to-clipboard.

## Tech Stack

- Kotlin + Jetpack Compose (Material 3)
- Android SDK:
  - `minSdk = 26`
  - `targetSdk = 36`
  - `compileSdk = 36`
- Coroutines
- `com.canhub.android:image-cropper` for crop UI

## Project Structure

- `app/src/main/java/.../ui` - Compose screens/components
- `app/src/main/java/.../data` - Train catalog and metadata
- `app/src/main/java/.../search` - BR lookup and matching logic
- `app/src/main/java/.../collection` - local persistence (SharedPreferences JSON)
- `app/src/main/java/.../util` - helper utilities (snapshots, URLs, debug logs)
- `app/src/main/java/.../CameraCaptureActivity.kt` - camera/crop flow

## Build & Run

### Prerequisites

- Android Studio (recent stable)
- JDK 11 (project is configured for Java 11 compatibility)
- Android SDK platform 36 installed

### Run from Android Studio

1. Open the project in Android Studio.
2. Sync Gradle.
3. Run the `app` configuration on an emulator/device.

### Run from command line

```bash
./gradlew assembleDebug
```

## Permissions

- `android.permission.CAMERA` - used for snapshot capture.
- `android.permission.INTERNET` - used for Wikipedia summary API requests.

## Debug Logs (In-App)

- Enable **Settings -> Debug menu**.
- A **Logs** tab appears in the drawer.
- Logs are stored as `.log` files in app cache (`runtime_debug_logs`) and reset on app start.
- Tap a log entry to view full text.
- Tap the text (or "Kopieren") to copy it to clipboard for sharing.

## Notes

- Collection data is stored locally on device (SharedPreferences + app files/cache for images/logs).
- Wikipedia content remains subject to its own licensing and attribution.
