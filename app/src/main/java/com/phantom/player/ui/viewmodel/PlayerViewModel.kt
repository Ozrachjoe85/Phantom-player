package com.phantom.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.phantom.player.data.model.Song
import com.phantom.player.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val player: ExoPlayer
) : ViewModel() {
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    init {
        // Listen to player state changes
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
            }
            
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    _duration.value = player.duration
                }
            }
        })
        
        // Update position every second
        viewModelScope.launch {
            while (true) {
                if (player.isPlaying) {
                    _currentPosition.value = player.currentPosition
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }
    
    fun playSong(song: Song) {
        _currentSong.value = song
        
        val mediaItem = MediaItem.Builder()
            .setUri(song.data)
            .setMediaId(song.id)
            .build()
        
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }
    
    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }
    
    fun skipToNext() {
        // TODO: Implement queue/playlist
        player.seekToNext()
    }
    
    fun skipToPrevious() {
        if (player.currentPosition > 3000) {
            player.seekTo(0)
        } else {
            player.seekToPrevious()
        }
    }
    
    fun seekTo(position: Long) {
        player.seekTo(position)
    }
    
    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
