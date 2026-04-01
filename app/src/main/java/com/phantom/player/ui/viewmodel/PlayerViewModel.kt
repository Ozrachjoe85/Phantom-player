package com.phantom.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantom.player.data.local.database.entities.Song
import com.phantom.player.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {
    
    // Current song being played
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    // Current position in milliseconds
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    // Song duration in milliseconds
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    /**
     * Play a specific song
     */
    fun playSong(song: Song) {
        _currentSong.value = song
        _isPlaying.value = true
        _currentPosition.value = 0L
        _duration.value = song.duration
        
        // TODO: Integrate with EqViewModel when ready
        // eqViewModel.setCurrentSong(song.id)
    }
    
    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }
    
    /**
     * Skip to next song
     */
    fun skipToNext() {
        // TODO: Implement playlist logic
        _currentPosition.value = 0L
    }
    
    /**
     * Skip to previous song
     */
    fun skipToPrevious() {
        // TODO: Implement playlist logic
        _currentPosition.value = 0L
    }
    
    /**
     * Seek to specific position
     */
    fun seekTo(position: Long) {
        _currentPosition.value = position
    }
    
    /**
     * Toggle favorite status
     */
    fun toggleFavorite(songId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                // TODO: Implement when MusicRepository has updateFavoriteStatus method
                // musicRepository.updateFavoriteStatus(songId, isFavorite)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
