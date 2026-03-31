package com.phantom.player.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.phantom.player.data.local.database.dao.EqPresetDao
import com.phantom.player.data.local.database.dao.SongDao
import com.phantom.player.data.local.database.dao.SongEqProfileDao
import com.phantom.player.data.local.database.entities.EqPreset
import com.phantom.player.data.local.database.entities.Song
import com.phantom.player.data.local.database.entities.SongEqProfile

@Database(
    entities = [
        Song::class,
        EqPreset::class,
        SongEqProfile::class  // ADDED FOR SONG-SPECIFIC EQ
    ],
    version = 2,  // INCREMENTED FROM 1 TO 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun eqPresetDao(): EqPresetDao
    abstract fun songEqProfileDao(): SongEqProfileDao  // ADDED
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // MIGRATION FROM VERSION 1 TO 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create song_eq_profiles table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS song_eq_profiles (
                        songId TEXT PRIMARY KEY NOT NULL,
                        bands TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
            }
        }
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "phantom_player_db"
                )
                    .addMigrations(MIGRATION_1_2)  // ADDED MIGRATION
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
