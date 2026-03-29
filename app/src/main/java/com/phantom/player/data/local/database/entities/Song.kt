package com.phantom.player.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val filePath: String,
    val albumArtPath: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val genre: String? = null,
    val year: Int? = null,
    val track: Int? = null,
    val bitrate: Int? = null,
    val sampleRate: Int? = null,
    val playCount: Int = 0,
    val lastPlayed: Long? = null,
    val isFavorite: Boolean = false
)
