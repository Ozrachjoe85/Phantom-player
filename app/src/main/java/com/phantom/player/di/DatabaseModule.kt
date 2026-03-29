package com.phantom.player.di

import android.content.Context
import androidx.room.Room
import com.phantom.player.data.local.database.AppDatabase
import com.phantom.player.data.local.database.dao.EqPresetDao
import com.phantom.player.data.local.database.dao.PlaylistDao
import com.phantom.player.data.local.database.dao.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "phantom_player_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideSongDao(database: AppDatabase): SongDao {
        return database.songDao()
    }
    
    @Provides
    @Singleton
    fun providePlaylistDao(database: AppDatabase): PlaylistDao {
        return database.playlistDao()
    }
    
    @Provides
    @Singleton
    fun provideEqPresetDao(database: AppDatabase): EqPresetDao {
        return database.eqPresetDao()
    }
}
