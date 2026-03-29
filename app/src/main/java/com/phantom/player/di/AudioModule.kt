package com.phantom.player.di

import android.content.Context
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
object AudioModule {
    
    @Provides
    @Singleton
    fun provideAudioEngine(
        @ApplicationContext context: Context
    ): AudioEngine {
        return AudioEngine(context)
    }
    
    @Provides
    @Singleton
    fun provideEqualizerProcessor(): EqualizerProcessor {
        return EqualizerProcessor()
    }
}
