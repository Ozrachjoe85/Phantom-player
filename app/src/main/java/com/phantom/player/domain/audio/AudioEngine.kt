package com.phantom.player.domain.audio

import android.content.Context
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.phantom.player.data.local.database.entities.Song
import com.phantom.player.service.PlaybackService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _player = ExoPlayer.Builder(context)
        .setAudioAttributes(
            androidx.media3.common.AudioAttributes.Builder()
                .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                .build(),
            true
        )
        .build()
    val player: ExoPlayer = _player

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var positionUpdateJob: Job? = null

    init {
        _player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
                updateNotification()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        _duration.value = _player.duration
                    }
                    Player.STATE_ENDED -> {
                        skipToNext()
                    }
                }
            }
        })
    }
    
    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (isActive) {
                _currentPosition.value = _player.currentPosition
                delay(100)
            }
        }
    }
    
    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
    }

    fun playSong(song: Song) {
        _currentSong.value = song
        val mediaItem = MediaItem.fromUri(song.filePath)
        _player.setMediaItem(mediaItem)
        _player.prepare()
        _player.play()
        startService()
        updateNotification()
    }

    fun play() {
        _player.play()
        updateNotification()
    }

    fun pause() {
        _player.pause()
        updateNotification()
    }

    fun stop() {
        _player.stop()
        _currentSong.value = null
        stopService()
    }

    fun seekTo(positionMs: Long) {
        _player.seekTo(positionMs)
    }

    fun skipToNext() {
        _player.seekToNext()
        updateNotification()
    }

    fun skipToPrevious() {
        _player.seekToPrevious()
        updateNotification()
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        val mediaItems = songs.map { song ->
            MediaItem.fromUri(song.filePath)
        }
        _player.setMediaItems(mediaItems, startIndex, 0)
        _player.prepare()
        if (songs.isNotEmpty()) {
            _currentSong.value = songs[startIndex]
        }
        startService()
        updateNotification()
    }

    fun getCurrentPosition(): Long {
        return _player.currentPosition
    }

    fun getDuration(): Long {
        return _player.duration
    }

    fun release() {
        stopPositionUpdates()
        _player.release()
        stopService()
    }

    fun setRepeatMode(repeatMode: Int) {
        _player.repeatMode = repeatMode
    }

    fun setShuffleMode(enabled: Boolean) {
        _player.shuffleModeEnabled = enabled
    }
    
    fun getAudioSessionId(): Int {
        return _player.audioSessionId
    }
    
    private fun startService() {
        val intent = Intent(context, PlaybackService::class.java)
        context.startForegroundService(intent)
    }
    
    private fun stopService() {
        val intent = Intent(context, PlaybackService::class.java)
        intent.action = PlaybackService.ACTION_STOP
        context.startService(intent)
    }
    
    private fun updateNotification() {
        _currentSong.value?.let { song ->
            val intent = Intent(context, PlaybackService::class.java).apply {
                action = "UPDATE_NOTIFICATION"
                putExtra("song_title", song.title)
                putExtra("song_artist", song.artist)
                putExtra("song_album", song.album)
                putExtra("album_art_path", song.albumArtPath)
                putExtra("is_playing", _isPlaying.value)
            }
            context.startService(intent)
        }
    }
}
