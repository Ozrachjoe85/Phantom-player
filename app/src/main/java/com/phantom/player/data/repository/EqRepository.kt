package com.phantom.player.data.repository

import androidx.media3.common.audio.AudioProcessor
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.AudioSink
import com.phantom.player.data.model.EqBand
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

@Singleton
class EqRepository @Inject constructor(
    private val player: ExoPlayer
) {
    
    // Standard 10-band EQ frequencies
    val frequencies = listOf(32, 64, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)
    
    // Current EQ settings (in dB)
    private val currentGains = mutableMapOf<Int, Float>().apply {
        frequencies.forEach { freq -> put(freq, 0f) }
    }
    
    /**
     * Apply EQ bands to the audio output
     */
    fun applyEq(bands: List<EqBand>) {
        bands.forEach { band ->
            currentGains[band.frequency] = band.value
        }
        
        // Apply to ExoPlayer
        // Note: ExoPlayer doesn't have built-in EQ, so we'll need to use Android's Equalizer
        applyToSystemEqualizer()
    }
    
    /**
     * Reset all bands to 0dB (flat)
     */
    fun resetEq() {
        frequencies.forEach { freq ->
            currentGains[freq] = 0f
        }
        applyToSystemEqualizer()
    }
    
    /**
     * Get current EQ bands
     */
    fun getCurrentBands(): List<EqBand> {
        return frequencies.map { freq ->
            EqBand(
                frequency = freq,
                value = currentGains[freq] ?: 0f,
                q = 1.0f
            )
        }
    }
    
    /**
     * Apply EQ using Android's system Equalizer
     * This requires audio session ID from the player
     */
    private fun applyToSystemEqualizer() {
        try {
            val audioSessionId = player.audioSessionId
            
            if (audioSessionId != 0) {
                // Create or get existing equalizer for this session
                val equalizer = android.media.audiofx.Equalizer(0, audioSessionId)
                equalizer.enabled = true
                
                // Map our frequencies to the closest available bands
                val numBands = equalizer.numberOfBands.toInt()
                
                frequencies.forEach { targetFreq ->
                    val gain = currentGains[targetFreq] ?: 0f
                    
                    // Find closest band to our target frequency
                    var closestBand = 0
                    var closestDiff = Int.MAX_VALUE
                    
                    for (band in 0 until numBands) {
                        val bandFreq = equalizer.getCenterFreq(band.toShort()) / 1000 // Convert mHz to Hz
                        val diff = kotlin.math.abs(bandFreq - targetFreq)
                        if (diff < closestDiff) {
                            closestDiff = diff
                            closestBand = band
                        }
                    }
                    
                    // Convert dB to millibels (Android uses millibels)
                    val gainInMillibels = (gain * 100).toInt().toShort()
                    
                    // Clamp to valid range
                    val minGain = equalizer.bandLevelRange[0]
                    val maxGain = equalizer.bandLevelRange[1]
                    val clampedGain = gainInMillibels.coerceIn(minGain, maxGain)
                    
                    equalizer.setBandLevel(closestBand.toShort(), clampedGain)
                }
            }
        } catch (e: Exception) {
            // Equalizer not available on this device
            e.printStackTrace()
        }
    }
}
