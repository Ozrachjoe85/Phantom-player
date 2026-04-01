# PlayerViewModel.kt - Add playSong() Method

**Location:** `app/src/main/java/com/phantom/player/ui/viewmodel/PlayerViewModel.kt`

## Add This Method to PlayerViewModel Class

Add this method anywhere inside your `PlayerViewModel` class:

```kotlin
/**
 * Play a specific song
 */
fun playSong(song: Song) {
    _currentSong.value = song
    _isPlaying.value = true
    
    // Reset position
    _currentPosition.value = 0L
    _duration.value = song.duration
    
    // Tell EQ system to load profile for this song
    // Note: You'll need to inject EqViewModel or create a way to communicate
    // For now, this is a placeholder - proper implementation needed
}
```

## ⚠️ IMPORTANT NOTE

The `playSong()` method needs access to `EqViewModel` to call `eqViewModel.setCurrentSong(song.id)`.

There are **2 ways** to fix this:

### **Option 1: Quick Fix (For Now)**
Just add the method WITHOUT the EQ line:

```kotlin
fun playSong(song: Song) {
    _currentSong.value = song
    _isPlaying.value = true
    _currentPosition.value = 0L
    _duration.value = song.duration
}
```

This will make LibraryScreen compile. You can add EQ integration later.

### **Option 2: Proper Fix (Recommended)**
1. Inject `EqViewModel` into `PlayerViewModel` constructor
2. Call `eqViewModel.setCurrentSong(song.id)` inside `playSong()`

For now, **use Option 1** to get the build working.

---

## Summary

**File:** `app/src/main/java/com/phantom/player/ui/viewmodel/PlayerViewModel.kt`

**Action:** Add the `playSong()` method shown above

**Why:** LibraryScreen calls `playerViewModel.playSong(song)` but the method doesn't exist yet
