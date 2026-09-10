# Lyra

Lyra is a music streaming Android application built with Kotlin and XML layouts. It allows listeners to browse a music catalog, search for songs and artists, and control playback through a focused now-playing experience.

> This is a personal Android development project. The current build focuses on catalog browsing and streaming playback; favorites, downloads, authentication, and persisted settings are still being completed.

## Features

- Browse songs from a music catalog.
- Search by song title or artist.
- Open a dedicated now-playing screen.
- Play, pause, seek, and switch between tracks.
- Continue controlling playback from the mini player.
- Swipe through the now-playing screen.
- Display cover artwork and loading, error, and empty states.
- Load song metadata from Firebase Firestore.
- Stream audio and load media from remote URLs.

The Favorites and Downloads screens are currently included as work-in-progress UI screens.

## Tech stack

- Kotlin
- XML layouts, ViewBinding, Fragments, and RecyclerView
- Material Components for Android
- MVVM with ViewModel and LiveData
- Repository-based data layer
- Firebase Firestore and Firebase Storage
- AndroidX Media3 ExoPlayer
- Coil 3 with OkHttp
- Kotlin Coroutines
- JUnit, AndroidX Test, and Espresso

## Architecture

The application follows a lightweight MVVM architecture:

```text
Firebase Firestore
        │
        ▼
Remote data source
        │
        ▼
Repository
        │
        ▼
ViewModel ───────────────► XML UI
        │
        ▼
Media3 ExoPlayer
```

Main source packages:

```text
app/src/main/java/com/devpro/sound/
├── data/
│   ├── mapper/
│   ├── model/
│   ├── remote/
│   ├── repository/
│   └── repositoryImpl/
├── player/
└── ui/
    ├── components/
    ├── discover/
    ├── downloads/
    ├── favorites/
    ├── nowplaying/
    ├── search/
    └── settings/
```

## Requirements

- Android Studio
- JDK 11
- Android SDK 37
- Android API 24 or higher
- A Firebase project with Firestore enabled

## Getting started

Clone the repository:

```bash
git clone https://github.com/huanminh254/Lyra.git
cd Lyra
```

Open the project in Android Studio, add your Firebase configuration file at:

```text
app/google-services.json
```

Then sync Gradle and run the `app` configuration on an emulator or Android device.

Do not commit `google-services.json` or other private Firebase credentials.

## Firestore data

The app reads songs from the `songs` collection. A song document can contain:

```json
{
  "title": "Example Song",
  "artist": "Example Artist",
  "audioUrl": "https://example.com/audio.mp3",
  "coverUrl": "https://example.com/cover.jpg",
  "sourceUrl": "https://example.com/source",
  "genre": "Pop",
  "year": 2026,
  "sortOrder": 1
}
```

The current development configuration also uses the `users/user_001` document for user-related data.

## Build and test

Run unit tests:

```bash
./gradlew test
```

Run lint checks:

```bash
./gradlew lint
```

Build a debug APK:

```bash
./gradlew assembleDebug
```

Run instrumented tests on a connected device or emulator:

```bash
./gradlew connectedAndroidTest
```

## Project status

Completed:

- XML-based Android UI with ViewBinding.
- Discover, search, settings, and now-playing screens.
- Firestore-backed song catalog.
- Media3-based audio playback.
- Mini-player controls and shared song list components.

Planned improvements:

- Persist favorites and downloads locally.
- Implement real offline audio downloads.
- Add user authentication and per-user data.
- Persist settings preferences.
- Add background playback and media session integration.

## Documentation

- [Use cases](docs/use-cases.md)
- [Architecture diagram](docs/diagrams/lyra-architecture.svg)

## Media and copyright

Lyra does not bundle copyrighted music. Audio files, cover images, and metadata should be provided through content that you own or have permission to use.

## License

No open-source license has been added yet. Contact the repository owner before redistributing or reusing the project.
