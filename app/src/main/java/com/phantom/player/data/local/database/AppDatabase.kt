package com.phantom.player.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.phantom.player.data.local.database.dao.EqPresetDao
import com.phantom.player.data.local.database.dao.PlaylistDao
import com.phantom.player.data.local.database.dao.SongDao
import com.phantom.player.data.local.database.entities.EqPreset
import com.phantom.player.data.local.database.entities.Playlist
import com.phantom.player.data.local.database.entities.PlaylistSong
import com.phantom.player.data.local.database.entities.Song

@Database(
    entities = [
        Song::class,
        Playlist::class,
        PlaylistSong::class,
        EqPreset::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun eqPresetDao(): EqPresetDao
}
