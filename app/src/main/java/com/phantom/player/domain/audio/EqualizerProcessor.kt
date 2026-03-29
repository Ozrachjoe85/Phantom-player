package com.phantom.player.domain.audio

import android.media.audiofx.Equalizer
import com.phantom.player.data.local.database.entities.EqBand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EqualizerProcessor @Inject constructor() {
    
    private var equalizer: Equalizer? = null
    
    private val _bands = MutableStateFlow<List<EqBand>>(emptyList())
    val bands: StateFlow<List<EqBand>> = _bands.asStateFlow()
    
    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()
    
    // Standard 10-band frequencies (Hz)
    private val standardFrequencies = listOf(
        31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000
    )
    
    fun initialize(audioSessionId: Int) {
        try {
            equalizer?.release()
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
            _isEnabled.value = true
            initializeBands()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun initializeBands() {
        val eq = equalizer ?: return
        val numberOfBands = eq.numberOfBands.toInt().coerceAtMost(10)
        
        val bands = List(numberOfBands) { index ->
            val frequency = if (index < standardFrequencies.size) {
                standardFrequencies[index]
            } else {
                eq.getCenterFreq(index.toShort()) / 1000
            }
            EqBand(frequency = frequency, value = 0f)
        }
        
        _bands.value = bands
    }
    
    fun setBandValue(bandIndex: Int, value: Float) {
        val eq = equalizer ?: return
        if (bandIndex >= eq.numberOfBands) return
        
        try {
            // Convert dB (-12 to +12) to millibels
            val millibels = (value * 100).toInt().toShort()
            eq.setBandLevel(bandIndex.toShort(), millibels)
            
            // Update state
            val updatedBands = _bands.value.toMutableList()
            if (bandIndex < updatedBands.size) {
                updatedBands[bandIndex] = updatedBands[bandIndex].copy(value = value)
                _bands.value = updatedBands
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun loadPreset(bands: List<EqBand>) {
        val eq = equalizer ?: return
        bands.forEachIndexed { index, band ->
            if (index < eq.numberOfBands) {
                setBandValue(index, band.value)
            }
        }
    }
    
    fun resetAllBands() {
        val eq = equalizer ?: return
        for (i in 0 until eq.numberOfBands.toInt()) {
            setBandValue(i, 0f)
        }
    }
    
    fun setEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
        _isEnabled.value = enabled
    }
    
    fun release() {
        equalizer?.release()
        equalizer = null
        _isEnabled.value = false
    }
    
    fun getPresetNames(): List<String> {
        val eq = equalizer ?: return emptyList()
        val presets = mutableListOf<String>()
        for (i in 0 until eq.numberOfPresets) {
            presets.add(eq.getPresetName(i.toShort()))
        }
        return presets
    }
    
    fun useBuiltInPreset(presetIndex: Int) {
        val eq = equalizer ?: return
        try {
            eq.usePreset(presetIndex.toShort())
            // Update bands to reflect preset values
            val updatedBands = _bands.value.mapIndexed { index, band ->
                val level = eq.getBandLevel(index.toShort())
                band.copy(value = level / 100f)
            }
            _bands.value = updatedBands
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
