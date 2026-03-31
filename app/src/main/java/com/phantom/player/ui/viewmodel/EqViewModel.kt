package com.phantom.player.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantom.player.data.local.database.dao.SongEqProfileDao
import com.phantom.player.data.local.database.entities.EqBand
import com.phantom.player.data.local.database.entities.SongEqProfile
import com.phantom.player.data.local.database.entities.getOriginalMasteringBands
import com.phantom.player.data.repository.EqRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class EqViewModel @Inject constructor(
    private val eqRepository: EqRepository,
    private val songEqProfileDao: SongEqProfileDao,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    // Current song being played
    private val _currentSongId = MutableStateFlow<String?>(null)
    val currentSongId: StateFlow<String?> = _currentSongId.asStateFlow()
    
    // Current EQ bands - updates in REAL-TIME
    private val _currentBands = MutableStateFlow(getOriginalMasteringBands())
    val currentBands: StateFlow<List<EqBand>> = _currentBands.asStateFlow()
    
    // Original mastering (flat curve) - for ghost overlay
    private val _originalMastering = MutableStateFlow(getOriginalMasteringBands())
    val originalMastering: StateFlow<List<EqBand>> = _originalMastering.asStateFlow()
    
    // Whether a song-specific profile exists
    private val _hasSongProfile = MutableStateFlow(false)
    val hasSongProfile: StateFlow<Boolean> = _hasSongProfile.asStateFlow()
    
    // EQ enabled state
    val isEnabled: StateFlow<Boolean> = eqRepository.isEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    
    // Auto EQ active state
    private val prefs = context.getSharedPreferences("phantom_eq", Context.MODE_PRIVATE)
    private val _isAutoEqActive = MutableStateFlow(prefs.getBoolean("auto_eq_active", false))
    val isAutoEqActive: StateFlow<Boolean> = _isAutoEqActive.asStateFlow()
    
    init {
        // Enable EQ by default
        viewModelScope.launch {
            eqRepository.setEnabled(true)
        }
        
        // Listen to EQ repository changes for real-time updates
        viewModelScope.launch {
            eqRepository.bands.collect { bands ->
                if (bands.isNotEmpty()) {
                    _currentBands.value = bands
                }
            }
        }
    }
    
    /**
     * Set the current song and load its EQ profile
     */
    fun setCurrentSong(songId: String?) {
        _currentSongId.value = songId
        
        if (songId != null) {
            viewModelScope.launch {
                songEqProfileDao.getProfileForSong(songId).collect { profile ->
                    if (profile != null) {
                        // Song has a custom EQ profile
                        val bands = parseBands(profile.bands)
                        _currentBands.value = bands
                        eqRepository.loadPreset(bands)
                        _hasSongProfile.value = true
                        _isAutoEqActive.value = false
                    } else {
                        // No custom profile, check if Auto EQ is active
                        _hasSongProfile.value = false
                        if (_isAutoEqActive.value) {
                            applyAutoEQ()
                        } else {
                            // Reset to flat
                            val flat = getOriginalMasteringBands()
                            _currentBands.value = flat
                            eqRepository.loadPreset(flat)
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Update a single EQ band - REAL-TIME
     */
    fun setBandValue(bandIndex: Int, value: Float) {
        viewModelScope.launch {
            try {
                eqRepository.setBandValue(bandIndex, value)
                
                // Update current bands immediately for UI
                val updated = _currentBands.value.toMutableList()
                if (bandIndex in updated.indices) {
                    updated[bandIndex] = updated[bandIndex].copy(value = value)
                    _currentBands.value = updated
                }
                
                // Disable Auto EQ when user adjusts
                if (_isAutoEqActive.value) {
                    _isAutoEqActive.value = false
                    saveAutoEqState(false)
                }
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }
    
    /**
     * Save current EQ as song-specific profile
     */
    fun saveSongProfile() {
        val songId = _currentSongId.value ?: return
        
        viewModelScope.launch {
            val bandsJson = serializeBands(_currentBands.value)
            val profile = SongEqProfile(
                songId = songId,
                bands = bandsJson,
                updatedAt = System.currentTimeMillis()
            )
            songEqProfileDao.saveProfile(profile)
            _hasSongProfile.value = true
        }
    }
    
    /**
     * Delete song-specific profile
     */
    fun deleteSongProfile() {
        val songId = _currentSongId.value ?: return
        
        viewModelScope.launch {
            songEqProfileDao.deleteProfile(songId)
            _hasSongProfile.value = false
            
            // Reset to flat or Auto EQ
            if (_isAutoEqActive.value) {
                applyAutoEQ()
            } else {
                val flat = getOriginalMasteringBands()
                _currentBands.value = flat
                eqRepository.loadPreset(flat)
            }
        }
    }
    
    /**
     * Toggle Auto EQ
     */
    fun toggleAutoEQ() {
        if (_isAutoEqActive.value) {
            // Turning off - reset to flat
            val flat = getOriginalMasteringBands()
            _currentBands.value = flat
            viewModelScope.launch {
                eqRepository.loadPreset(flat)
            }
            _isAutoEqActive.value = false
            saveAutoEqState(false)
        } else {
            // Turning on - apply Auto EQ
            applyAutoEQ()
            _isAutoEqActive.value = true
            saveAutoEqState(true)
        }
    }
    
    /**
     * Apply Auto EQ curve
     */
    private fun applyAutoEQ() {
        viewModelScope.launch {
            val autoEqBands = listOf(
                EqBand(frequency = 31, value = 6.0f),    // Deep bass
                EqBand(frequency = 63, value = 5.0f),    // Bass
                EqBand(frequency = 125, value = 4.0f),   // Low bass
                EqBand(frequency = 250, value = 2.0f),   // Low mids
                EqBand(frequency = 500, value = 1.0f),   // Mids
                EqBand(frequency = 1000, value = 2.0f),  // Upper mids
                EqBand(frequency = 2000, value = 3.0f),  // Presence
                EqBand(frequency = 4000, value = 4.0f),  // High presence
                EqBand(frequency = 8000, value = 3.0f),  // Highs
                EqBand(frequency = 16000, value = 2.0f)  // Air
            )
            _currentBands.value = autoEqBands
            eqRepository.loadPreset(autoEqBands)
        }
    }
    
    /**
     * Reset to flat EQ
     */
    fun resetToFlat() {
        val flat = getOriginalMasteringBands()
        _currentBands.value = flat
        viewModelScope.launch {
            eqRepository.loadPreset(flat)
        }
        _isAutoEqActive.value = false
        saveAutoEqState(false)
    }
    
    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch {
            eqRepository.setEnabled(enabled)
        }
    }
    
    fun initialize(audioSessionId: Int) {
        viewModelScope.launch {
            try {
                eqRepository.initialize(audioSessionId)
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }
    
    private fun saveAutoEqState(active: Boolean) {
        prefs.edit().putBoolean("auto_eq_active", active).apply()
    }
    
    private fun serializeBands(bands: List<EqBand>): String {
        val jsonArray = JSONArray()
        bands.forEach { band ->
            val jsonObject = JSONObject()
            jsonObject.put("frequency", band.frequency)
            jsonObject.put("value", band.value)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }
    
    private fun parseBands(bandsJson: String): List<EqBand> {
        return try {
            val jsonArray = JSONArray(bandsJson)
            val bands = mutableListOf<EqBand>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                bands.add(
                    EqBand(
                        frequency = jsonObject.getInt("frequency"),
                        value = jsonObject.getDouble("value").toFloat()
                    )
                )
            }
            bands
        } catch (e: Exception) {
            getOriginalMasteringBands()
        }
    }
}
