package com.phantom.player.di

import android.content.Context
import com.phantom.player.data.local.MediaScanner
import com.phantom.player.domain.audio.AudioEngine
import com.phantom.player.domain.audio.EqualizerProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideMediaScanner(
        @ApplicationContext context: Context
    ): MediaScanner {
        return MediaScanner(context)
    }
}
