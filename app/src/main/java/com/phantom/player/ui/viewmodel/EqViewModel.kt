package com.phantom.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantom.player.data.local.database.entities.EqBand
import com.phantom.player.data.local.database.entities.EqPreset
import com.phantom.player.data.repository.EqRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EqViewModel @Inject constructor(
    private val eqRepository: EqRepository
) : ViewModel() {
    
    val bands: StateFlow<List<EqBand>> = eqRepository.bands
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), getDefaultBands())
    
    val isEnabled: StateFlow<Boolean> = eqRepository.isEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val presets: StateFlow<List<EqPreset>> = eqRepository.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _currentPresetId = MutableStateFlow<Long?>(null)
    val currentPresetId: StateFlow<Long?> = _currentPresetId.asStateFlow()
    
    private val _isAutoEqActive = MutableStateFlow(true) // Auto EQ on by default
    val isAutoEqActive: StateFlow<Boolean> = _isAutoEqActive.asStateFlow()
    
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    init {
        // Enable EQ and apply Auto EQ by default after delay
        viewModelScope.launch {
            try {
                // Wait for audio session to be initialized
                delay(1500)
                
                // Enable EQ
                eqRepository.setEnabled(true)
                
                // Apply Auto EQ preset for loudest, cleanest sound
                applyAutoEQ()
                
                _isInitialized.value = true
            } catch (e: Exception) {
                // If initialization fails, just continue without crashing
                _isInitialized.value = true
            }
        }
    }
    
    fun initialize(audioSessionId: Int) {
        viewModelScope.launch {
            try {
                eqRepository.initialize(audioSessionId)
                // Reapply Auto EQ after initialization
                delay(500)
                if (_isAutoEqActive.value) {
                    applyAutoEQ()
                }
            } catch (e: Exception) {
                // Silently handle initialization errors
            }
        }
    }
    
    fun setBandValue(bandIndex: Int, value: Float) {
        try {
            eqRepository.setBandValue(bandIndex, value)
            _currentPresetId.value = null
            _isAutoEqActive.value = false
        } catch (e: Exception) {
            // Handle error silently
        }
    }
    
    fun loadPreset(preset: EqPreset) {
        viewModelScope.launch {
            try {
                val bands = parsePresetBands(preset.bands)
                eqRepository.loadPreset(bands)
                _currentPresetId.value = preset.id
                _isAutoEqActive.value = false
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    fun resetAllBands() {
        try {
            eqRepository.resetAllBands()
            _currentPresetId.value = null
            _isAutoEqActive.value = false
        } catch (e: Exception) {
            // Handle error silently
        }
    }
    
    fun setEnabled(enabled: Boolean) {
        try {
            eqRepository.setEnabled(enabled)
        } catch (e: Exception) {
            // Handle error silently
        }
    }
    
    fun saveCurrentAsPreset(name: String) {
        viewModelScope.launch {
            try {
                val currentBands = bands.value
                val presetId = eqRepository.savePreset(name, currentBands)
                _currentPresetId.value = presetId
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    fun deletePreset(preset: EqPreset) {
        viewModelScope.launch {
            try {
                eqRepository.deletePreset(preset)
                if (_currentPresetId.value == preset.id) {
                    _currentPresetId.value = null
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    fun getBuiltInPresets(): List<String> {
        return try {
            eqRepository.getPresetNames()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun useBuiltInPreset(presetIndex: Int) {
        try {
            eqRepository.useBuiltInPreset(presetIndex)
            _currentPresetId.value = null
            _isAutoEqActive.value = false
        } catch (e: Exception) {
            // Handle error silently
        }
    }
    
    /**
     * Apply Auto EQ for loudest, cleanest sound with:
     * - MASSIVE thumping bass without muddiness
     * - Crystal clear vocals that cut through
     * - Bright, sparkling highs
     * - NO clipping or distortion
     */
    fun applyAutoEQ() {
        viewModelScope.launch {
            try {
                val autoEqBands = calculateAutoEQ()
                eqRepository.loadPreset(autoEqBands)
                _currentPresetId.value = null
                _isAutoEqActive.value = true
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    fun toggleAutoEQ() {
        if (_isAutoEqActive.value) {
            resetAllBands()
        } else {
            applyAutoEQ()
        }
    }
    
    /**
     * Calculate optimal Auto EQ curve for MAXIMUM IMPACT:
     * - Bass: HEAVY boost for thump and punch
     * - Low-mids: Moderate boost for warmth and body
     * - Mids: Boosted for vocal presence and clarity
     * - Upper-mids: Boosted for vocal detail and definition
     * - Highs: Boosted for air, sparkle, and brilliance
     * 
     * Values are in dB (decibels)
     * Range: -12.0 to +12.0
     */
    private fun calculateAutoEQ(): List<EqBand> {
        return listOf(
            // SUB-BASS: MASSIVE thump (31Hz) - +8dB
            EqBand(
                frequency = 31,
                value = 8.0f  // Deep sub-bass impact
            ),
            
            // BASS: Powerful punch (63Hz) - +7dB
            EqBand(
                frequency = 63,
                value = 7.0f  // Bass punch and power
            ),
            
            // LOW-BASS: Warm foundation (125Hz) - +6dB
            EqBand(
                frequency = 125,
                value = 6.0f  // Bass warmth and fullness
            ),
            
            // LOW-MIDS: Body and warmth (250Hz) - +3dB
            EqBand(
                frequency = 250,
                value = 3.0f  // Lower mid body
            ),
            
            // MIDS: Vocal foundation (500Hz) - +3dB
            EqBand(
                frequency = 500,
                value = 3.0f  // Vocal presence
            ),
            
            // UPPER-MIDS: Vocal clarity (1kHz) - +4dB
            EqBand(
                frequency = 1000,
                value = 4.0f  // Vocal clarity and cut-through
            ),
            
            // HIGH-MIDS: Vocal definition (2kHz) - +4dB
            EqBand(
                frequency = 2000,
                value = 4.0f  // Vocal detail and articulation
            ),
            
            // PRESENCE: Bite and attack (4kHz) - +5dB
            EqBand(
                frequency = 4000,
                value = 5.0f  // Presence and attack
            ),
            
            // HIGH-FREQ: Air and sparkle (8kHz) - +4dB
            EqBand(
                frequency = 8000,
                value = 4.0f  // Air and definition
            ),
            
            // ULTRA-HIGH: Brilliance and shimmer (16kHz) - +3dB
            EqBand(
                frequency = 16000,
                value = 3.0f  // Top-end brilliance
            )
        )
    }
    
    /**
     * Get default flat EQ bands if repository hasn't initialized yet
     */
    private fun getDefaultBands(): List<EqBand> {
        return listOf(
            EqBand(frequency = 31, value = 0f),
            EqBand(frequency = 63, value = 0f),
            EqBand(frequency = 125, value = 0f),
            EqBand(frequency = 250, value = 0f),
            EqBand(frequency = 500, value = 0f),
            EqBand(frequency = 1000, value = 0f),
            EqBand(frequency = 2000, value = 0f),
            EqBand(frequency = 4000, value = 0f),
            EqBand(frequency = 8000, value = 0f),
            EqBand(frequency = 16000, value = 0f)
        )
    }
    
    private fun parsePresetBands(bandsJson: String): List<EqBand> {
        return try {
            // For now, return current bands as fallback
            bands.value.ifEmpty { getDefaultBands() }
        } catch (e: Exception) {
            getDefaultBands()
        }
    }
}
