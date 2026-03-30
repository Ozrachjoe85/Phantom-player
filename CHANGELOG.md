# Changelog

All notable changes to Phantom Player will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Playlist creation and management
- Favorite songs functionality
- Shuffle and repeat modes
- Queue management UI
- Lyrics support (lrclib.net)
- Crossfade between tracks
- ReplayGain normalization
- Custom EQ preset saving
- Theme toggle (dark/light)
- Folder browsing
- Android Auto support
- Home screen widgets

## [1.0.0] - 2025-03-30

### Added
- **Player Screen**
  - Full-screen playback interface with holographic album art
  - Animated background particles and pulsing radial glows
  - Real-time waveform visualizer (100 points)
  - 3D spectrum analyzer (24 bars with perspective)
  - Enhanced VU meters with L/R channels and peak detection
  - Holographic playback controls with rotating ring animations
  - Secondary controls (shuffle, favorite, repeat, queue)
  - Neon progress bar with glow effects
  - Track info with glitch text effects
  
- **Library Screen**
  - Music scanning via MediaStore
  - Multiple view modes: Songs, Albums, Artists, Playlists
  - Real-time search filtering
  - 2-column grid layout with animated cards
  - Album art extraction from audio files
  - Play All functionality
  - Empty state with scan prompt
  
- **Equalizer Screen**
  - 10-band equalizer (31Hz - 16kHz)
  - Dual view modes: Frequency Curve and Faders
  - Auto EQ with animated curve morphing
  - Interactive frequency curve with draggable control points
  - Visual EQ presets: Bass Boost, Vocal, Treble, Flat, Rock, Pop
  - Real-time audio processing
  - Power toggle with glow animation
  
- **Settings Screen**
  - Playback settings (gapless, fade in/out, replay gain)
  - Audio settings (global EQ, headphone mode, buffer size)
  - Library settings (rescan, folder management, cache)
  - Interface settings (theme, animations, view preferences)
  - Advanced settings (cache management, network, developer options)
  - About section (version, licenses, privacy policy)
  
- **Navigation & UI**
  - Bottom navigation with holographic animations
  - Mini player bar with album art and quick controls
  - Ambient background effects (animated radial gradients)
  - Glass morphism panels throughout
  - Cyberpunk color scheme (cyan, purple, pink on black)
  
- **Audio System**
  - Media3 ExoPlayer integration
  - Foreground media service with notification controls
  - Lock screen integration
  - 5-button notification (Previous, Play/Pause, Next, Stop)
  - Position tracking and seek support
  - Audio session management for EQ
  
- **Architecture**
  - Clean Architecture with MVVM pattern
  - Hilt dependency injection
  - Room database for persistence
  - Kotlin Flow for reactive data
  - Repository pattern for data abstraction
  - ViewModels for UI state management
  
- **Tech Stack**
  - Kotlin 100%
  - Jetpack Compose
  - Material 3
  - Media3/ExoPlayer
  - Room Database
  - Hilt
  - Coil (image loading)
  - Coroutines & Flow

### Technical Details
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 34 (Android 14)
- **Package**: com.phantom.player
- **Build Tools**: Gradle 8.2, KSP
- **Kotlin Version**: 1.9.20

### Known Issues
- Playlist functionality not yet implemented
- Shuffle and repeat buttons are placeholders
- Queue screen not implemented
- Album art extraction may fail for some audio formats
- No lyrics support yet

---

## Version History

### Legend
- `Added` - New features
- `Changed` - Changes to existing functionality
- `Deprecated` - Features that will be removed
- `Removed` - Features that have been removed
- `Fixed` - Bug fixes
- `Security` - Security vulnerability fixes

---

**Note**: This is version 1.0.0 - the initial release. Future versions will document all changes here.
