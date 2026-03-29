package com.phantom.player.data.repository

import com.phantom.player.data.local.database.entities.Song
import com.phantom.player.domain.audio.AudioEngine
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackRepository @Inject constructor(
    private val audioEngine: AudioEngine
) {
    val isPlaying: StateFlow<Boolean> = audioEngine.isPlaying
    val currentSong: StateFlow<Song?> = audioEngine.currentSong
    val currentPosition: StateFlow<Long> = audioEngine.currentPosition
    val duration: StateFlow<Long> = audioEngine.duration
    
    fun playSong(song: Song) {
        audioEngine.playSong(song)
    }
    
    fun play() {
        audioEngine.play()
    }
    
    fun pause() {
        audioEngine.pause()
    }
    
    fun stop() {
        audioEngine.stop()
    }
    
    fun seekTo(positionMs: Long) {
        audioEngine.seekTo(positionMs)
    }
    
    fun skipToNext() {
        audioEngine.skipToNext()
    }
    
    fun skipToPrevious() {
        audioEngine.skipToPrevious()
    }
    
    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        audioEngine.setPlaylist(songs, startIndex)
    }
    
    fun setRepeatMode(mode: Int) {
        audioEngine.setRepeatMode(mode)
    }
    
    fun setShuffleMode(enabled: Boolean) {
        audioEngine.setShuffleMode(enabled)
    }
    
    fun getAudioSessionId(): Int {
        return audioEngine.getAudioSessionId()
    }
}
