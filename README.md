# Lyra

Lyra is a modern Android music player built with Kotlin and Jetpack Compose. It combines a clean dark interface with Firebase-backed song data and Media3 playback so listeners can discover music, search their library, and control playback from a focused now-playing experience.

> This project is under active development. Some screens and settings are currently UI-first and will continue to receive persistence and account functionality.

## Features

- Discover screen with featured tracks, top songs, cover art, and a compact mini-player.
- Search songs by title or artist.
- Full-screen Now Playing experience with:
  - play and pause controls;
  - previous and next track navigation;
  - seek/progress control;
  - swipe gestures for track navigation;
  - buffering and playback state handling.
- Favorites and Downloads views for organizing music in the app experience.
- Settings screen with a Firebase-backed user profile and playback/app preferences.
- Remote cover art loading with Coil and fallback artwork.
- Firebase Firestore integration for songs and user data.
- Media3 ExoPlayer integration for streaming audio URLs.

## Tech stack

- Kotlin 2.2.21
- Android Gradle Plugin 9.1.1
- Jetpack Compose with Material 3
- AndroidX Navigation Compose
- AndroidX Lifecycle ViewModel and Compose runtime
- AndroidX Media3 ExoPlayer and Media3 UI
- Firebase Firestore and Firebase Storage
- Coil 3 for image loading
- Kotlin Coroutines
- JUnit and kotlinx-coroutines-test

## Architecture

Lyra follows a lightweight MVVM and repository-based structure:

```text
app/src/main/java/com/devpro/sound/
├── data/
│   ├── mapper/              # Remote/model transformations
│   ├── model/               # Domain models such as Song and User
│   ├── remote/              # Firestore data sources and DTOs
│   ├── repository/          # Repository contracts
│   └── repositoryImpl/      # Repository implementations
├── player/                  # Media3/ExoPlayer playback management
└── ui/
    ├── components/          # Shared Compose components
    ├── discover/
    ├── downloads/
    ├── favorites/
    ├── navigation/
    ├── nowplaying/
    ├── search/
    └── settings/
```

The main flow is:

```text
Firestore → RemoteDataSource → Repository → ViewModel → Compose UI
                                                   ↓
                                             Media3 ExoPlayer
```

## Requirements

- Android Studio with Android SDK 37 available.
- JDK 11.
- A Firebase project with a registered Android app using the application ID `com.devpro.sound`.
- A device or emulator running Android 7.0 (API 24) or newer.

## Setup

1. Clone the repository:

   ```bash
   git clone https://github.com/huanminh254/Lyra.git
   cd Lyra
   ```

2. Open the project in Android Studio and allow Gradle to sync.

3. Create or select a Firebase project, register an Android app with package name `com.devpro.sound`, and download `google-services.json`.

4. Place the downloaded file here:

   ```text
   app/google-services.json
   ```

   The file is intentionally ignored by Git because it is environment-specific. Firebase client configuration should still be protected with correct Firestore and Storage security rules.

5. Configure the Firestore collections described below, then run the `app` configuration on an emulator or connected Android device.

## Firestore data model

### `songs` collection

Lyra loads songs from the `songs` collection ordered by the numeric `sortOrder` field. A document can contain:

```json
{
  "title": "Example Song",
  "artist": "Example Artist",
  "currentTime": "00:00",
  "duration": "03:42",
  "audioUrl": "https://example.com/audio.mp3",
  "coverUrl": "https://example.com/cover.jpg",
  "audioObjectPath": "audio/example-song.mp3",
  "coverObjectPath": "covers/example-song.jpg",
  "originalFileName": "example-song.mp3",
  "sizeBytes": 1234567,
  "sortOrder": 1,
  "sourceUrl": "https://example.com/source",
  "genre": "V-Pop",
  "year": "2026"
}
```

`audioUrl` must be playable by Media3. `coverUrl` is used by Coil for artwork.

### `users` collection

The current implementation reads the `user_001` document by default:

```json
{
  "name": "Lyra Listener",
  "accountSubtitle": "Music lover",
  "audioQuality": "High",
  "streamOnlyOnWifi": false,
  "darkModeEnabled": true,
  "cacheSubtitle": "0 MB used",
  "appVersion": "1.0"
}
```

## Build and test

Build the debug APK:

```bash
./gradlew assembleDebug
```

Run unit tests:

```bash
./gradlew test
```

Run instrumented tests on a connected device or emulator:

```bash
./gradlew connectedAndroidTest
```

## Project status

The core browsing, search, remote song loading, and playback flows are implemented. Favorites, downloads, authentication, and preference persistence are part of the ongoing product roadmap.

## Media and licensing

The repository does not include locally prepared audio or cover-art upload folders. Add only media that you own or are licensed to distribute, and review the license and redistribution terms of every external source before publishing or deploying content through Firebase Storage.

## Contributing

Contributions are welcome. Before opening a pull request:

1. Create a focused branch from `main`.
2. Keep UI, data, and playback changes separated where practical.
3. Add or update tests for behavior changes.
4. Run `./gradlew test` and verify the app on an emulator or device.
5. Describe the user-facing change and any Firebase schema changes in the pull request.

## License

No license has been selected for this project yet. Until a license is added, all rights are reserved by the copyright holder.
