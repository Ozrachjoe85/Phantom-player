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
    
    private val prefs = context.getSharedPreferences("phantom_eq", Context.MODE_PRIVATE)
    
    // REAL-TIME EQ bands - updates immediately when changed
    val bands: StateFlow<List<EqBand>> = eqRepository.bands
        .stateIn(viewModelScope, SharingStarted.Eagerly, getDefaultBands())
    
    val isEnabled: StateFlow<Boolean> = eqRepository.isEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    
    val presets: StateFlow<List<EqPreset>> = eqRepository.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _currentPresetId = MutableStateFlow<Long?>(null)
    val currentPresetId: StateFlow<Long?> = _currentPresetId.asStateFlow()
    
    // PERSISTENT Auto EQ state - NEVER resets
    private val _isAutoEqActive = MutableStateFlow(prefs.getBoolean("auto_eq_active", true))
    val isAutoEqActive: StateFlow<Boolean> = _isAutoEqActive.asStateFlow()
    
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    init {
        // Apply Auto EQ on startup if it was active
        viewModelScope.launch {
            try {
                // Short delay for audio session
                delay(1000)
                
                // ALWAYS enable EQ
                eqRepository.setEnabled(true)
                
                // Apply Auto EQ if it's active (persisted state)
                if (_isAutoEqActive.value) {
                    val autoEqBands = calculateAutoEQ()
                    eqRepository.loadPreset(autoEqBands)
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
                
                // Reapply Auto EQ after audio session init
                if (_isAutoEqActive.value) {
                    val autoEqBands = calculateAutoEQ()
                    eqRepository.loadPreset(autoEqBands)
                }
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }
    
    fun setBandValue(bandIndex: Int, value: Float) {
        try {
            eqRepository.setBandValue(bandIndex, value)
            _currentPresetId.value = null
            
            // User adjusted - disable Auto EQ and PERSIST that choice
            saveAutoEqState(false)
        } catch (e: Exception) {
            // Silent fail
        }
    }
    
    fun loadPreset(preset: EqPreset) {
        viewModelScope.launch {
            try {
                val bands = parsePresetBands(preset.bands)
                eqRepository.loadPreset(bands)
                _currentPresetId.value = preset.id
                saveAutoEqState(false)
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }
    
    fun resetAllBands() {
        try {
            eqRepository.resetAllBands()
            _currentPresetId.value = null
            saveAutoEqState(false)
        } catch (e: Exception) {
            // Silent fail
        }
    }
    
    fun setEnabled(enabled: Boolean) {
        try {
            eqRepository.setEnabled(enabled)
        } catch (e: Exception) {
            // Silent fail
        }
    }
    
    fun saveCurrentAsPreset(name: String) {
        viewModelScope.launch {
            try {
                val currentBands = bands.value
                val presetId = eqRepository.savePreset(name, currentBands)
                _currentPresetId.value = presetId
            } catch (e: Exception) {
                // Silent fail
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
                // Silent fail
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
            saveAutoEqState(false)
        } catch (e: Exception) {
            // Silent fail
        }
    }
    
    /**
     * Apply Auto EQ and SAVE the state
     */
    fun applyAutoEQ() {
        viewModelScope.launch {
            try {
                val autoEqBands = calculateAutoEQ()
                eqRepository.loadPreset(autoEqBands)
                _currentPresetId.value = null
                saveAutoEqState(true)
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }
    
    fun toggleAutoEQ() {
        if (_isAutoEqActive.value) {
            resetAllBands()
            saveAutoEqState(false)
        } else {
            applyAutoEQ()
        }
    }
    
    /**
     * CRITICAL: Save Auto EQ state to SharedPreferences
     * This ensures it persists across screen changes and app restarts
     */
    private fun saveAutoEqState(active: Boolean) {
        _isAutoEqActive.value = active
        prefs.edit().putBoolean("auto_eq_active", active).apply()
    }
    
    /**
     * Optimized Auto EQ curve
     * Bass: +8/+7/+6 dB (massive thump)
     * Mids: +3/+4 dB (vocal clarity)
     * Highs: +4/+5 dB (sparkle and air)
     */
    private fun calculateAutoEQ(): List<EqBand> {
        return listOf(
            EqBand(frequency = 31, value = 8.0f),    // SUB-BASS
            EqBand(frequency = 63, value = 7.0f),    // BASS
            EqBand(frequency = 125, value = 6.0f),   // LOW-BASS
            EqBand(frequency = 250, value = 3.0f),   // LOW-MIDS
            EqBand(frequency = 500, value = 3.0f),   // MIDS
            EqBand(frequency = 1000, value = 4.0f),  // UPPER-MIDS
            EqBand(frequency = 2000, value = 4.0f),  // HIGH-MIDS
            EqBand(frequency = 4000, value = 5.0f),  // PRESENCE
            EqBand(frequency = 8000, value = 4.0f),  // HIGH-FREQ
            EqBand(frequency = 16000, value = 3.0f)  // ULTRA-HIGH
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
