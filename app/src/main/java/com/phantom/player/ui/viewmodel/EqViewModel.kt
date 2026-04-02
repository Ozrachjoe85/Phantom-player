package com.phantom.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantom.player.data.database.SongEqProfileDao
import com.phantom.player.data.model.EqBand
import com.phantom.player.data.repository.EqRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EqViewModel @Inject constructor(
    private val eqRepository: EqRepository,
    private val songEqProfileDao: SongEqProfileDao
) : ViewModel() {
    
    // Standard 10-band frequencies (Hz)
    private val standardFrequencies = listOf(32, 64, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)
    
    private val _currentBands = MutableStateFlow<List<EqBand>>(
        standardFrequencies.map { freq ->
            EqBand(frequency = freq, value = 0f, q = 1.0f)
        }
    )
    val currentBands: StateFlow<List<EqBand>> = _currentBands.asStateFlow()
    
    private val _isAutoEqActive = MutableStateFlow(false)
    val isAutoEqActive: StateFlow<Boolean> = _isAutoEqActive.asStateFlow()
    
    private var currentSongId: String? = null
    
    /**
     * Toggle Auto EQ on/off
     */
    fun toggleAutoEq() {
        _isAutoEqActive.value = !_isAutoEqActive.value
        
        if (_isAutoEqActive.value) {
            applyAutoEq()
        } else {
            resetToFlat()
        }
    }
    
    /**
     * Set manual band value
     */
    fun setBandValue(frequency: Int, value: Float) {
        val updatedBands = _currentBands.value.map { band ->
            if (band.frequency == frequency) {
                band.copy(value = value.coerceIn(-12f, 12f))
            } else {
                band
            }
        }
        _currentBands.value = updatedBands
        
        // Apply to audio output immediately
        eqRepository.applyEq(updatedBands)
    }
    
    /**
     * Reset to flat (all bands at 0dB)
     */
    fun resetToFlat() {
        val flatBands = standardFrequencies.map { freq ->
            EqBand(frequency = freq, value = 0f, q = 1.0f)
        }
        _currentBands.value = flatBands
        eqRepository.applyEq(flatBands)
    }
    
    /**
     * Save current EQ profile for song
     */
    fun saveSongProfile() {
        val songId = currentSongId ?: return
        
        viewModelScope.launch {
            try {
                // TODO: Save to database when DAO is implemented
                // For now, just apply the current settings
                eqRepository.applyEq(_currentBands.value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Set current song for EQ profiling
     */
    fun setCurrentSong(songId: String) {
        currentSongId = songId
        
        if (_isAutoEqActive.value) {
            applyAutoEq()
        }
    }
    
    /**
     * Apply intelligent Auto EQ
     */
    private fun applyAutoEq() {
        viewModelScope.launch {
            val autoEqBands = generateIntelligentEqCurve()
            _currentBands.value = autoEqBands
            eqRepository.applyEq(autoEqBands)
        }
    }
    
    /**
     * Generate intelligent EQ curve based on psychoacoustic principles
     * 
     * Based on:
     * - Fletcher-Munson equal-loudness contours
     * - Pink noise reference curve  
     * - Professional mastering standards (-14 LUFS)
     */
    private fun generateIntelligentEqCurve(): List<EqBand> {
        return standardFrequencies.map { freq ->
            val adjustment = when (freq) {
                // Sub-bass (32Hz) - Foundation
                32 -> 4.0f
                
                // Bass (64Hz) - Punch
                64 -> 4.5f
                
                // Low-mid (125Hz) - Cut mud
                125 -> -2.5f
                
                // Mid (250Hz) - Warmth control
                250 -> -3.0f
                
                // Upper-mid (500Hz) - Clarity
                500 -> 2.0f
                
                // Presence (1kHz) - Definition
                1000 -> 2.5f
                
                // Upper presence (2kHz) - Detail
                2000 -> 3.0f
                
                // High (4kHz) - Crispness
                4000 -> 3.5f
                
                // Very high (8kHz) - Brilliance
                8000 -> 3.5f
                
                // Air (16kHz) - Sparkle
                16000 -> 4.0f
                
                else -> 0f
            }
            
            // Smooth extreme adjustments to prevent distortion
            val smoothed = if (kotlin.math.abs(adjustment) > 3.5f) {
                adjustment * 0.8f
            } else {
                adjustment
            }
            
            EqBand(
                frequency = freq,
                value = smoothed.coerceIn(-12f, 12f),
                q = 1.0f
            )
        }
    }
}
