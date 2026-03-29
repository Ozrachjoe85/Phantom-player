package com.phantom.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.phantom.player.MainActivity
import com.phantom.player.R
import com.phantom.player.data.local.database.entities.Song
import com.phantom.player.domain.audio.AudioEngine
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    
    @Inject
    lateinit var audioEngine: AudioEngine
    
    private var mediaSession: MediaSession? = null
    private var currentSong: Song? = null
    
    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "phantom_player_playback"
        
        const val ACTION_PLAY = "com.phantom.player.PLAY"
        const val ACTION_PAUSE = "com.phantom.player.PAUSE"
        const val ACTION_NEXT = "com.phantom.player.NEXT"
        const val ACTION_PREVIOUS = "com.phantom.player.PREVIOUS"
        const val ACTION_STOP = "com.phantom.player.STOP"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        mediaSession = MediaSession.Builder(this, audioEngine.player)
            .setCallback(MediaSessionCallback())
            .build()
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> audioEngine.play()
            ACTION_PAUSE -> audioEngine.pause()
            ACTION_NEXT -> audioEngine.skipToNext()
            ACTION_PREVIOUS -> audioEngine.skipToPrevious()
            ACTION_STOP -> {
                audioEngine.stop()
                stopSelf()
            }
        }
        return START_STICKY
    }
    
    fun updateNotification(song: Song, isPlaying: Boolean) {
        currentSong = song
        val notification = createNotification(song, isPlaying)
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun createNotification(song: Song, isPlaying: Boolean): Notification {
        val albumArt = loadAlbumArt(song.albumArtPath)
        
        // Intent to open app
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Media actions
        val playPauseIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, PlaybackService::class.java).apply {
                action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val previousIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, PlaybackService::class.java).apply {
                action = ACTION_PREVIOUS
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val nextIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, PlaybackService::class.java).apply {
                action = ACTION_NEXT
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, PlaybackService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSubText(song.album)
            .setLargeIcon(albumArt)
            .setContentIntent(contentIntent)
            .setDeleteIntent(stopIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            // Compact view actions (3 buttons)
            .addAction(
                R.drawable.ic_previous,
                "Previous",
                previousIntent
            )
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (isPlaying) "Pause" else "Play",
                playPauseIntent
            )
            .addAction(
                R.drawable.ic_next,
                "Next",
                nextIntent
            )
            // Expanded view additional actions
            .addAction(
                R.drawable.ic_close,
                "Stop",
                stopIntent
            )
            // Media style
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession?.sessionCompatToken)
                    .setShowActionsInCompactView(0, 1, 2)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(stopIntent)
            )
            .setColor(0x00E5FF) // PhantomCyan
            .build()
    }
    
    private fun loadAlbumArt(artPath: String?): Bitmap?
