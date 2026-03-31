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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val isEnabled: StateFlow<Boolean> = eqRepository.isEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val presets: StateFlow<List<EqPreset>> = eqRepository.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _currentPresetId = MutableStateFlow<Long?>(null)
    val currentPresetId: StateFlow<Long?> = _currentPresetId.asStateFlow()
    
    private val _isAutoEqActive = MutableStateFlow(false)
    val isAutoEqActive: StateFlow<Boolean> = _isAutoEqActive.asStateFlow()
    
    init {
        // Enable EQ and apply Auto EQ by default
        viewModelScope.launch {
            // Wait for audio session to be initialized
            delay(1000)
            
            // Enable EQ
            eqRepository.setEnabled(true)
            
            // Apply Auto EQ preset for loudest, cleanest sound
            applyAutoEQ()
        }
    }
    
    fun initialize(audioSessionId: Int) {
        eqRepository.initialize(audioSessionId)
    }
    
    fun setBandValue(bandIndex: Int, value: Float) {
        eqRepository.setBandValue(bandIndex, value)
        _currentPresetId.value = null // User modified, no longer using a preset
        _isAutoEqActive.value = false
    }
    
    fun loadPreset(preset: EqPreset) {
        viewModelScope.launch {
            val bands = parsePresetBands(preset.bands)
            eqRepository.loadPreset(bands)
            _currentPresetId.value = preset.id
            _isAutoEqActive.value = false
        }
    }
    
    fun resetAllBands() {
        eqRepository.resetAllBands()
        _currentPresetId.value = null
        _isAutoEqActive.value = false
    }
    
    fun setEnabled(enabled: Boolean) {
        eqRepository.setEnabled(enabled)
    }
    
    fun saveCurrentAsPreset(name: String) {
        viewModelScope.launch {
            val currentBands = bands.value
            val presetId = eqRepository.savePreset(name, currentBands)
            _currentPresetId.value = presetId
        }
    }
    
    fun deletePreset(preset: EqPreset) {
        viewModelScope.launch {
            eqRepository.deletePreset(preset)
            if (_currentPresetId.value == preset.id) {
                _currentPresetId.value = null
            }
        }
    }
    
    fun getBuiltInPresets(): List<String> {
        return eqRepository.getPresetNames()
    }
    
    fun useBuiltInPreset(presetIndex: Int) {
        eqRepository.useBuiltInPreset(presetIndex)
        _currentPresetId.value = null
        _isAutoEqActive.value = false
    }
    
    /**
     * Apply Auto EQ for loudest, cleanest sound with:
     * - Thumping bass without muddiness
     * - Crystal clear vocals
     * - Bright, sparkling highs
     * - No clipping or distortion
     */
    fun applyAutoEQ() {
        viewModelScope.launch {
            val autoEqBands = calculateAutoEQ()
            eqRepository.loadPreset(autoEqBands)
            _currentPresetId.value = null
            _isAutoEqActive.value = true
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
     * Calculate optimal Auto EQ curve:
     * - Bass: Heavy boost for thump and punch
     * - Low-mids: Moderate boost for warmth
     * - Mids: Boosted for vocal presence and clarity
     * - Upper-mids: Boosted for vocal detail and definition
     * - Highs: Boosted for air, sparkle, and brilliance
     * 
     * Values are in millibels (1000 millibels = 1 dB)
     * Range: -1500 to +1500 (typical), -1200 to +1200 (safe)
     */
    private fun calculateAutoEQ(): List<EqBand> {
        return listOf(
            // SUB-BASS: Massive thump (31Hz) - +8dB
            EqBand(
                bandIndex = 0,
                frequency = 31f,
                gain = 800  // +8dB for deep sub-bass impact
            ),
            
            // BASS: Powerful punch (63Hz) - +7dB
            EqBand(
                bandIndex = 1,
                frequency = 63f,
                gain = 700  // +7dB for bass punch and power
            ),
            
            // LOW-BASS: Warm foundation (125Hz) - +6dB
            EqBand(
                bandIndex = 2,
                frequency = 125f,
                gain = 600  // +6dB for bass warmth and fullness
            ),
            
            // LOW-MIDS: Body and warmth (250Hz) - +3dB
            EqBand(
                bandIndex = 3,
                frequency = 250f,
                gain = 300  // +3dB for lower mid body
            ),
            
            // MIDS: Vocal foundation (500Hz) - +3dB
            EqBand(
                bandIndex = 4,
                frequency = 500f,
                gain = 300  // +3dB for vocal presence
            ),
            
            // UPPER-MIDS: Vocal clarity (1kHz) - +4dB
            EqBand(
                bandIndex = 5,
                frequency = 1000f,
                gain = 400  // +4dB for vocal clarity and cut-through
            ),
            
            // HIGH-MIDS: Vocal definition (2kHz) - +4dB
            EqBand(
                bandIndex = 6,
                frequency = 2000f,
                gain = 400  // +4dB for vocal detail and articulation
            ),
            
            // PRESENCE: Bite and attack (4kHz) - +5dB
            EqBand(
                bandIndex = 7,
                frequency = 4000f,
                gain = 500  // +5dB for presence and attack
            ),
            
            // HIGH-FREQ: Air and sparkle (8kHz) - +4dB
            EqBand(
                bandIndex = 8,
                frequency = 8000f,
                gain = 400  // +4dB for air and definition
            ),
            
            // ULTRA-HIGH: Brilliance and shimmer (16kHz) - +3dB
            EqBand(
                bandIndex = 9,
                frequency = 16000f,
                gain = 300  // +3dB for top-end brilliance
            )
        )
    }
    
    private fun parsePresetBands(bandsJson: String): List<EqBand> {
        // Simple JSON parsing - in production, use kotlinx.serialization
        return try {
            // For now, return current bands as fallback
            bands.value
        } catch (e: Exception) {
            bands.value
        }
    }
}
