package com.phantom.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantom.player.data.local.database.entities.Song
import com.phantom.player.data.repository.EqRepository
import com.phantom.player.data.repository.MusicRepository
import com.phantom.player.data.repository.PlaybackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackRepository: PlaybackRepository,
    private val musicRepository: MusicRepository,
    private val eqRepository: EqRepository
) : ViewModel() {
    
    val isPlaying: StateFlow<Boolean> = playbackRepository.isPlaying
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val currentSong: StateFlow<Song?> = playbackRepository.currentSong
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    val currentPosition: StateFlow<Long> = playbackRepository.currentPosition
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    
    val duration: StateFlow<Long> = playbackRepository.duration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    
    init {
        // Initialize EQ with audio session ID from player
        viewModelScope.launch {
            val audioSessionId = playbackRepository.getAudioSessionId()
            eqRepository.initialize(audioSessionId)
        }
    }
    
    fun play(song: Song) {
        viewModelScope.launch {
            playbackRepository.playSong(song)
            musicRepository.incrementPlayCount(song.id)
        }
    }
    
    fun togglePlayPause() {
        if (isPlaying.value) {
            pause()
        } else {
            resume()
        }
    }
    
    fun resume() {
        playbackRepository.play()
    }
    
    fun pause() {
        playbackRepository.pause()
    }
    
    fun stop() {
        playbackRepository.stop()
    }
    
    fun seekTo(positionMs: Long) {
        playbackRepository.seekTo(positionMs)
    }
    
    fun skipToNext() {
        playbackRepository.skipToNext()
    }
    
    fun skipToPrevious() {
        playbackRepository.skipToPrevious()
    }
    
    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        playbackRepository.setPlaylist(songs, startIndex)
        if (songs.isNotEmpty()) {
            viewModelScope.launch {
                musicRepository.incrementPlayCount(songs[startIndex].id)
            }
        }
    }
    
    fun toggleFavorite(songId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(songId, isFavorite)
        }
    }
    
    fun setRepeatMode(mode: Int) {
        playbackRepository.setRepeatMode(mode)
    }
    
    fun setShuffleMode(enabled: Boolean) {
        playbackRepository.setShuffleMode(enabled)
    }
}
