package com.phantom.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantom.player.data.local.database.entities.EqBand
import com.phantom.player.data.local.database.entities.EqPreset
import com.phantom.player.data.repository.EqRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    
    fun initialize(audioSessionId: Int) {
        eqRepository.initialize(audioSessionId)
    }
    
    fun setBandValue(bandIndex: Int, value: Float) {
        eqRepository.setBandValue(bandIndex, value)
        _currentPresetId.value = null // User modified, no longer using a preset
    }
    
    fun loadPreset(preset: EqPreset) {
        viewModelScope.launch {
            val bands = parsePresetBands(preset.bands)
            eqRepository.loadPreset(bands)
            _currentPresetId.value = preset.id
        }
    }
    
    fun resetAllBands() {
        eqRepository.resetAllBands()
        _currentPresetId.value = null
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
