package com.phantom.player.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantom.player.data.local.database.entities.EqBand
import com.phantom.player.data.local.database.entities.EqPreset
import com.phantom.player.data.repository.EqRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val eqRepository: EqRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val prefs = context.getSharedPreferences("phantom_prefs", Context.MODE_PRIVATE)
    
    val bands: StateFlow<List<EqBand>> = eqRepository.bands
        .stateIn(viewModelScope, SharingStarted.Eagerly, getDefaultBands())
    
    val isEnabled: StateFlow<Boolean> = eqRepository.isEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true) // Default enabled
    
    val presets: StateFlow<List<EqPreset>> = eqRepository.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _currentPresetId = MutableStateFlow<Long?>(null)
    val currentPresetId: StateFlow<Long?> = _currentPresetId.asStateFlow()
    
    // Auto EQ state persisted across screen navigations
    private val _isAutoEqActive = MutableStateFlow(prefs.getBoolean("auto_eq_enabled", true))
    val isAutoEqActive: StateFlow<Boolean> = _isAutoEqActive.asStateFlow()
    
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    init {
        // Enable EQ and apply Auto EQ by default
        viewModelScope.launch {
            try {
                // Wait for audio session
                delay(1500)
                
                // Always enable EQ
                eqRepository.setEnabled(true)
                
                // Apply Auto EQ if it was enabled (default true)
                if (_isAutoEqActive.value) {
                    applyAutoEQ()
                }
                
                _isInitialized.value = true
            } catch (e: Exception) {
                _isInitialized.value = true
            }
        }
    }
    
    fun initialize(audioSessionId: Int) {
        viewModelScope.launch {
            try {
                eqRepository.initialize(audioSessionId)
                delay(500)
                
                // Reapply Auto EQ after initialization if it's active
                if (_isAutoEqActive.value) {
                    applyAutoEQ()
                }
            } catch (e: Exception) {
                // Handle silently
            }
        }
    }
    
    fun setBandValue(bandIndex: Int, value: Float) {
        try {
            eqRepository.setBandValue(bandIndex, value)
            _currentPresetId.value = null
            
            // User manually adjusted - turn off Auto EQ
            setAutoEqActive(false)
        } catch (e: Exception) {
            // Handle silently
        }
    }
    
    fun loadPreset(preset: EqPreset) {
        viewModelScope.launch {
            try {
                val bands = parsePresetBands(preset.bands)
                eqRepository.loadPreset(bands)
                _currentPresetId.value = preset.id
                setAutoEqActive(false)
            } catch (e: Exception) {
                // Handle silently
            }
        }
    }
    
    fun resetAllBands() {
        try {
            eqRepository.resetAllBands()
            _currentPresetId.value = null
            setAutoEqActive(false)
        } catch (e: Exception) {
            // Handle silently
        }
    }
    
    fun setEnabled(enabled: Boolean) {
        try {
            eqRepository.setEnabled(enabled)
        } catch (e: Exception) {
            // Handle silently
        }
    }
    
    fun saveCurrentAsPreset(name: String) {
        viewModelScope.launch {
            try {
                val currentBands = bands.value
                val presetId = eqRepository.savePreset(name, currentBands)
                _currentPresetId.value = presetId
            } catch (e: Exception) {
                // Handle silently
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
                // Handle silently
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
            setAutoEqActive(false)
        } catch (e: Exception) {
            // Handle silently
        }
    }
    
    /**
     * Apply Auto EQ - PERSISTED across app sessions and screen navigations
     */
    fun applyAutoEQ() {
        viewModelScope.launch {
            try {
                val autoEqBands = calculateAutoEQ()
                eqRepository.loadPreset(autoEqBands)
                _currentPresetId.value = null
                setAutoEqActive(true)
            } catch (e: Exception) {
                // Handle silently
            }
        }
    }
    
    fun toggleAutoEQ() {
        if (_isAutoEqActive.value) {
            resetAllBands()
            setAutoEqActive(false)
        } else {
            applyAutoEQ()
        }
    }
    
    /**
     * Persist Auto EQ state to SharedPreferences
     */
    private fun setAutoEqActive(active: Boolean) {
        _isAutoEqActive.value = active
        prefs.edit().putBoolean("auto_eq_enabled", active).apply()
    }
    
    /**
     * Calculate optimal Auto EQ curve for MAXIMUM IMPACT
     */
    private fun calculateAutoEQ(): List<EqBand> {
        return listOf(
            EqBand(frequency = 31, value = 8.0f),    // SUB-BASS: Massive thump
            EqBand(frequency = 63, value = 7.0f),    // BASS: Powerful punch
            EqBand(frequency = 125, value = 6.0f),   // LOW-BASS: Warm foundation
            EqBand(frequency = 250, value = 3.0f),   // LOW-MIDS: Body
            EqBand(frequency = 500, value = 3.0f),   // MIDS: Vocal presence
            EqBand(frequency = 1000, value = 4.0f),  // UPPER-MIDS: Vocal clarity
            EqBand(frequency = 2000, value = 4.0f),  // HIGH-MIDS: Definition
            EqBand(frequency = 4000, value = 5.0f),  // PRESENCE: Attack
            EqBand(frequency = 8000, value = 4.0f),  // HIGH-FREQ: Air
            EqBand(frequency = 16000, value = 3.0f)  // ULTRA-HIGH: Brilliance
        )
    }
    
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
            bands.value.ifEmpty { getDefaultBands() }
        } catch (e: Exception) {
            getDefaultBands()
        }
    }
}
