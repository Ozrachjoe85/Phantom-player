# 🎵 Phantom Player

<div align="center">

![Phantom Player](https://img.shields.io/badge/Android-Music_Player-00F0FF?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-100%25-B24BF3?style=for-the-badge&logo=kotlin&logoColor=white)
![Min SDK](https://img.shields.io/badge/Min_SDK-26-FF1493?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-0A0614?style=for-the-badge)

**A next-generation Android music player with cyberpunk aesthetics and professional audio processing**

[Features](#-features) • [Screenshots](#-screenshots) • [Architecture](#-architecture) • [Installation](#-installation) • [Building](#-building) • [Tech Stack](#-tech-stack)

</div>

---

## ✨ Features

### 🎨 **Cyberpunk DJ Aesthetic**
- **Holographic UI**: Neon cyan, purple, and pink color scheme with glass morphism effects
- **Animated Visuals**: Rotating rings, particle effects, pulsing glows, and scan line animations
- **Interactive Visualizers**: Real-time waveform and 3D spectrum analyzer with dynamic animations
- **VU Meters**: Professional-grade L/R channel level indicators with peak detection

### 🎛️ **Professional Audio**
- **10-Band Equalizer**: Precise frequency control (31Hz - 16kHz)
- **Auto EQ**: Intelligent frequency optimization with visual curve morphing
- **Dual View Modes**: Toggle between frequency curve and fader interfaces
- **EQ Presets**: Bass Boost, Vocal, Treble, Flat, Rock, Pop
- **Real-time Processing**: Zero-latency audio manipulation

### 🎵 **Playback Control**
- **Media3 ExoPlayer**: Industry-standard playback engine
- **Foreground Service**: Persistent playback with notification controls
- **Lock Screen Integration**: Full media controls on lock screen and notification shade
- **Seamless Navigation**: Bottom navigation with holographic animations
- **Mini Player**: Floating player bar with album art and quick controls

### 📚 **Library Management**
- **Smart Scanning**: Automatic music file discovery via MediaStore
- **Multiple Views**: Songs, Albums, Artists, Playlists
- **Search**: Real-time filtering across title and artist
- **Album Art**: Embedded cover art extraction with fallback icons
- **Grid Layout**: Beautiful 2-column card design with glow effects

### 🎨 **UI Components**
- **Player Screen**: Full-screen experience with album art, track info, visualizers, and controls
- **Library Screen**: Organized music browser with search and view modes
- **EQ Screen**: Professional equalizer with curve/fader modes and Auto EQ
- **Settings Screen**: Complete app configuration and preferences

---

## 📱 Screenshots

> *Coming soon - Add screenshots of Player, Library, EQ, and Settings screens*

---

## 🏗️ Architecture

Phantom Player follows **Clean Architecture** principles with MVVM pattern:

```
app/
├── data/                          # Data layer
│   ├── local/
│   │   ├── database/             # Room database
│   │   │   ├── dao/              # Data Access Objects
│   │   │   ├── entities/         # Database entities
│   │   │   └── AppDatabase.kt
│   │   └── MediaScanner.kt       # MediaStore scanner
│   └── repository/               # Repository implementations
│       ├── EqRepository.kt
│       ├── MusicRepository.kt
│       └── PlaybackRepository.kt
│
├── domain/                        # Business logic
│   └── audio/
│       ├── AudioEngine.kt        # Core audio processing
│       └── EqualizerProcessor.kt # EQ implementation
│
├── di/                           # Dependency Injection
│   ├── AppModule.kt
│   ├── AudioModule.kt
│   └── DatabaseModule.kt
│
├── service/
│   └── PlaybackService.kt       # Foreground media service
│
└── ui/                           # Presentation layer
    ├── screens/                  # Screen composables
    │   ├── PlayerScreen.kt
    │   ├── LibraryScreen.kt
    │   ├── EqScreen.kt
    │   └── SettingsScreen.kt
    ├── viewmodel/                # ViewModels
    ├── theme/                    # App theming
    └── PhantomApp.kt            # Main navigation
```

### Key Patterns

- **MVVM**: ViewModels manage UI state and business logic
- **Repository Pattern**: Abstract data sources
- **Dependency Injection**: Hilt for compile-time DI
- **Reactive Streams**: Kotlin Flow for reactive data
- **Single Activity**: Jetpack Compose navigation

---

## 🚀 Installation

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 26+ (device or emulator)
- Kotlin 1.9.20+

### Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/Ozrachjoe85/phantom-player.git
   cd phantom-player
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Wait for Gradle sync to complete
   - Resolve any dependency issues

4. **Run the app**
   - Connect an Android device (SDK 26+) or start an emulator
   - Click "Run" (▶️) or press `Shift + F10`

---

## 🔨 Building

### Debug Build (Development)

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build (Production)

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### Install on Device

```bash
./gradlew installDebug
```

---

## 🛠️ Tech Stack

### Core Framework
- **Kotlin** - Modern, concise Android development language
- **Jetpack Compose** - Declarative UI framework
- **Material 3** - Latest Material Design components

### Architecture Components
- **Hilt** - Dependency injection
- **Room** - Local database persistence
- **Navigation Compose** - Type-safe navigation
- **ViewModel** - Lifecycle-aware UI state management
- **Flow** - Reactive data streams

### Media & Audio
- **Media3 ExoPlayer** - High-performance media playback
- **Media3 Session** - Media session management
- **Android AudioEffect** - Native equalizer processing
- **MediaStore** - Audio file discovery

### UI & Design
- **Coil** - Efficient image loading
- **Canvas API** - Custom visualizer graphics
- **Material Icons Extended** - Comprehensive icon library

### Build & Development
- **Gradle KTS** - Kotlin DSL build scripts
- **KSP** - Kotlin Symbol Processing (faster than kapt)
- **ProGuard** - Code shrinking and obfuscation

---

## 🎨 Design System

### Colors

```kotlin
PhantomBlack   = #0A0614  // Primary background
PhantomCyan    = #00F0FF  // Primary accent
PhantomPurple  = #B24BF3  // Secondary accent
PhantomPink    = #FF1493  // Tertiary accent
```

### Typography

- **Headers**: JetBrains Mono, Bold, 2-3sp letter spacing
- **Body**: Default system font
- **LED Displays**: Monospace for numeric values

### Spacing

- **Padding**: 8dp, 12dp, 16dp, 24dp, 32dp
- **Corners**: 8dp (small), 12dp (medium), 16dp (large), 20dp (xlarge)
- **Elevation**: Glass morphism instead of traditional shadows

---

## 📋 Roadmap

### Completed ✅
- [x] Cyberpunk UI design system
- [x] Player screen with visualizers
- [x] 10-band equalizer with Auto EQ
- [x] Library management (songs, albums, artists)
- [x] Notification and lock screen controls
- [x] Settings screen

### In Progress 🚧
- [ ] Playlist creation and management
- [ ] Favorite songs functionality
- [ ] Shuffle and repeat modes
- [ ] Queue management

### Planned 📝
- [ ] Lyrics support (lrclib.net integration)
- [ ] Crossfade between tracks
- [ ] ReplayGain normalization
- [ ] Custom EQ presets
- [ ] Dark/Light theme toggle
- [ ] Folder browsing
- [ ] Android Auto support
- [ ] Widgets

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Comment complex logic
- Run `./gradlew ktlintCheck` before committing

---

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **Material Design 3** - Design system and components
- **Media3** - Robust media playback framework
- **ExoPlayer** - High-quality audio engine
- **Jetpack Compose** - Modern UI toolkit

---

## 📞 Contact

**Developer**: Joe  
**GitHub**: [@Ozrachjoe85](https://github.com/Ozrachjoe85)  
**Project**: [Phantom Player](https://github.com/Ozrachjoe85/phantom-player)

---

<div align="center">

**Made with 💜 by an independent Android developer**

*Built entirely from a phone using GitHub's web UI and GitHub Actions*

</div>
