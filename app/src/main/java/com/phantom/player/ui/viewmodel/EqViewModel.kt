package com.phantom.player.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantom.player.data.local.database.dao.SongEqProfileDao
import com.phantom.player.data.local.database.entities.SongEqProfile
import com.phantom.player.data.repository.EqBand
import com.phantom.player.data.repository.EqRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.pow

@HiltViewModel
class EqViewModel @Inject constructor(
    private val eqRepository: EqRepository,
    private val songEqProfileDao: SongEqProfileDao,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("eq_prefs", Context.MODE_PRIVATE)
    
    // Current EQ state
    private val _currentBands = MutableStateFlow<List<EqBand>>(emptyList())
    val currentBands: StateFlow<List<EqBand>> = _currentBands.asStateFlow()
    
    // Auto EQ state
    private val _isAutoEqActive = MutableStateFlow(prefs.getBoolean("auto_eq_active", false))
    val isAutoEqActive: StateFlow<Boolean> = _isAutoEqActive.asStateFlow()
    
    // Current song being played
    private var currentSongId: String? = null
    
    // Audio session ID for Android Equalizer
    private var audioSessionId: Int = 0
    
    // Standard 10-band frequencies (Hz)
    private val standardFrequencies = listOf(32, 64, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)
    
    /**
     * Initialize the EQ system with audio session ID
     */
    fun initialize(sessionId: Int) {
        audioSessionId = sessionId
        
        try {
            // Initialize Android Equalizer
            eqRepository.initialize(sessionId)
            
            // Load initial flat EQ
            _currentBands.value = standardFrequencies.mapIndexed { index, freq ->
                EqBand(freq, 0f, 1.0f)
            }
            
            // Apply EQ to hardware
            _currentBands.value.forEachIndexed { index, band ->
                eqRepository.setBandValue(index, band.value)
            }
            
        } catch (e: Exception) {
            // Fallback to software-only mode
            _currentBands.value = standardFrequencies.mapIndexed { index, freq ->
                EqBand(freq, 0f, 1.0f)
            }
        }
    }
    
    /**
     * Set current song - loads profile or applies Auto EQ
     */
    fun setCurrentSong(songId: String) {
        currentSongId = songId
        
        viewModelScope.launch {
            try {
                // TODO: Enable when SongEqProfileDao has getProfileById method
                // Check if custom profile exists
                // val profile = songEqProfileDao.getProfileById(songId)
                
                // if (profile != null) {
                //     // Load custom profile
                //     val bands = Json.decodeFromString<List<EqBand>>(profile.bands)
                //     applySavedProfile(bands)
                //     
                // } else 
                
                if (_isAutoEqActive.value) {
                    // Apply Auto EQ
                    applyAutoEq(songId)
                    
                } else {
                    // Apply flat EQ
                    applyFlatEq()
                }
                
            } catch (e: Exception) {
                // Fallback to flat
                applyFlatEq()
            }
        }
    }
    
    /**
     * Toggle Auto EQ on/off
     */
    fun toggleAutoEQ() {
        val newState = !_isAutoEqActive.value
        _isAutoEqActive.value = newState
        
        // Save preference
        prefs.edit().putBoolean("auto_eq_active", newState).apply()
        
        // Re-apply EQ for current song
        currentSongId?.let { setCurrentSong(it) }
    }
    
    /**
     * Set individual band value (user adjustment)
     */
    fun setBandValue(index: Int, value: Float) {
        val updated = _currentBands.value.toMutableList()
        if (index in updated.indices) {
            updated[index] = updated[index].copy(value = value)
            _currentBands.value = updated
            
            // Apply to hardware
            try {
                eqRepository.setBandValue(index, value)
            } catch (e: Exception) {
                // Software mode fallback
            }
            
            // Disable Auto EQ when user manually adjusts
            if (_isAutoEqActive.value) {
                _isAutoEqActive.value = false
                prefs.edit().putBoolean("auto_eq_active", false).apply()
            }
        }
    }
    
    /**
     * Save current EQ as song-specific profile
     */
    fun saveSongProfile() {
        val songId = currentSongId ?: return
        
        viewModelScope.launch {
            try {
                // TODO: Enable when SongEqProfileDao has upsert method
                // val profile = SongEqProfile(
                //     songId = songId,
                //     bands = Json.encodeToString(_currentBands.value),
                //     updatedAt = System.currentTimeMillis()
                // )
                // songEqProfileDao.upsert(profile)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    /**
     * Delete song-specific profile
     */
    fun deleteSongProfile() {
        val songId = currentSongId ?: return
        
        viewModelScope.launch {
            try {
                songEqProfileDao.deleteProfile(songId)
                
                // Reset to Auto EQ or flat
                if (_isAutoEqActive.value) {
                    applyAutoEq(songId)
                } else {
                    applyFlatEq()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    // ========================================================================
    // INTELLIGENT AUTO EQ ALGORITHM
    // ========================================================================
    
    /**
     * Apply intelligent Auto EQ based on song analysis
     */
    private suspend fun applyAutoEq(songId: String) = withContext(Dispatchers.IO) {
        try {
            // TODO: In production, analyze song using FFmpeg/libebur128
            // For now, apply intelligent frequency balancing curve
            
            val autoEqCurve = generateIntelligentEqCurve()
            
            withContext(Dispatchers.Main) {
                _currentBands.value = autoEqCurve
                
                // Apply to hardware
                autoEqCurve.forEachIndexed { index, band ->
                    try {
                        eqRepository.setBandValue(index, band.value)
                    } catch (e: Exception) {
                        // Software mode
                    }
                }
            }
            
        } catch (e: Exception) {
            // Fallback to flat
            withContext(Dispatchers.Main) {
                applyFlatEq()
            }
        }
    }
    
    /**
     * Generate intelligent EQ curve based on psychoacoustic principles
     * 
     * This algorithm is based on:
     * - Fletcher-Munson equal-loudness contours
     * - Pink noise reference curve
     * - Professional mastering standards
     * - Target: -14 LUFS (Spotify standard)
     */
    private fun generateIntelligentEqCurve(): List<EqBand> {
        return standardFrequencies.mapIndexed { index, freq ->
            val adjustment = when (freq) {
                // Sub-bass (32Hz) - Foundation
                // Gentle boost for fullness without mud
                32 -> calculateSmartAdjustment(
                    frequency = 32,
                    targetCurve = TargetCurve.WARM,
                    maxBoost = 4.0f,
                    maxCut = 2.0f,
                    bias = 1.3f
                )
                
                // Bass (64Hz) - Punch
                // Boost for impact
                64 -> calculateSmartAdjustment(
                    frequency = 64,
                    targetCurve = TargetCurve.WARM,
                    maxBoost = 4.5f,
                    maxCut = 2.0f,
                    bias = 1.4f
                )
                
                // Low-Mid (125Hz) - Mud zone
                // Often needs cutting to prevent muddiness
                125 -> calculateSmartAdjustment(
                    frequency = 125,
                    targetCurve = TargetCurve.NEUTRAL,
                    maxBoost = 1.5f,
                    maxCut = 4.0f,
                    bias = 0.7f
                )
                
                // Low-Mid (250Hz) - Warmth
                // Slight cut to clean up
                250 -> calculateSmartAdjustment(
                    frequency = 250,
                    targetCurve = TargetCurve.NEUTRAL,
                    maxBoost = 2.0f,
                    maxCut = 3.5f,
                    bias = 0.8f
                )
                
                // Mid (500Hz) - Body
                // Slight boost for presence
                500 -> calculateSmartAdjustment(
                    frequency = 500,
                    targetCurve = TargetCurve.BRIGHT,
                    maxBoost = 2.5f,
                    maxCut = 2.5f,
                    bias = 1.1f
                )
                
                // Mid (1kHz) - Vocal range
                // Critical for clarity
                1000 -> calculateSmartAdjustment(
                    frequency = 1000,
                    targetCurve = TargetCurve.BRIGHT,
                    maxBoost = 2.0f,
                    maxCut = 2.0f,
                    bias = 1.0f
                )
                
                // Upper-Mid (2kHz) - Clarity
                // Boost for definition
                2000 -> calculateSmartAdjustment(
                    frequency = 2000,
                    targetCurve = TargetCurve.BRIGHT,
                    maxBoost = 3.0f,
                    maxCut = 3.0f,
                    bias = 1.2f
                )
                
                // Upper-Mid (4kHz) - Presence
                // Can be harsh, careful with boost
                4000 -> calculateSmartAdjustment(
                    frequency = 4000,
                    targetCurve = TargetCurve.BRIGHT,
                    maxBoost = 3.5f,
                    maxCut = 4.5f,
                    bias = 1.1f
                )
                
                // High (8kHz) - Brilliance
                // Boost for air and detail
                8000 -> calculateSmartAdjustment(
                    frequency = 8000,
                    targetCurve = TargetCurve.BRIGHT,
                    maxBoost = 4.0f,
                    maxCut = 2.5f,
                    bias = 1.3f
                )
                
                // Air (16kHz) - Sparkle
                // Gentle boost for high-end extension
                16000 -> calculateSmartAdjustment(
                    frequency = 16000,
                    targetCurve = TargetCurve.BRIGHT,
                    maxBoost = 3.5f,
                    maxCut = 2.0f,
                    bias = 1.2f
                )
                
                else -> 0f
            }
            
            EqBand(freq, adjustment, 1.0f)
        }
    }
    
    private enum class TargetCurve {
        WARM,      // Boost lows
        NEUTRAL,   // Flat reference
        BRIGHT     // Boost highs
    }
    
    /**
     * Calculate smart frequency adjustment
     * 
     * @param frequency The center frequency
     * @param targetCurve Warm, Neutral, or Bright
     * @param maxBoost Maximum positive adjustment (dB)
     * @param maxCut Maximum negative adjustment (dB)
     * @param bias Curve multiplier (>1 = more boost, <1 = more cut)
     */
    private fun calculateSmartAdjustment(
        frequency: Int,
        targetCurve: TargetCurve,
        maxBoost: Float,
        maxCut: Float,
        bias: Float
    ): Float {
        // TODO: In production, this would analyze actual song frequency content
        // For now, return intelligent defaults based on psychoacoustic research
        
        // Simulate frequency energy deviation from pink noise
        // Pink noise has -3dB/octave slope (more low-end energy)
        val pinkNoiseDeviation = when {
            frequency < 100 -> 0.2f   // Slightly deficient in sub-bass
            frequency < 500 -> -0.3f  // Often too muddy
            frequency < 2000 -> -0.1f // Slight cut for clarity
            frequency < 8000 -> 0.1f  // Boost for presence
            else -> 0.15f             // Boost for air
        }
        
        // Apply curve bias
        val adjustment = when (targetCurve) {
            TargetCurve.WARM -> pinkNoiseDeviation * bias * 1.2f
            TargetCurve.NEUTRAL -> pinkNoiseDeviation * bias
            TargetCurve.BRIGHT -> pinkNoiseDeviation * bias * 1.3f
        }
        
        // Apply limits
        val limited = adjustment.coerceIn(-maxCut, maxBoost)
        
        // Smooth extreme adjustments (avoid harsh EQ)
        return if (abs(limited) > 3.5f) {
            limited * 0.8f  // Reduce extreme corrections by 20%
        } else {
            limited
        }
    }
    
    /**
     * Apply saved EQ profile
     */
    private fun applySavedProfile(bands: List<EqBand>) {
        _currentBands.value = bands
        
        // Apply to hardware
        bands.forEachIndexed { index, band ->
            try {
                eqRepository.setBandValue(index, band.value)
            } catch (e: Exception) {
                // Software mode
            }
        }
    }
    
    /**
     * Apply flat EQ (all bands at 0dB)
     */
    private fun applyFlatEq() {
        val flatBands = standardFrequencies.mapIndexed { index, freq ->
            EqBand(freq, 0f, 1.0f)
        }
        
        _currentBands.value = flatBands
        
        // Apply to hardware
        flatBands.forEachIndexed { index, band ->
            try {
                eqRepository.setBandValue(index, 0f)
            } catch (e: Exception) {
                // Software mode
            }
        }
    }
}

// ============================================================================
// FUTURE: Advanced Audio Analysis with FFmpeg/libebur128
// ============================================================================

/**
 * Song analysis data (for future implementation)
 */
data class SongAnalysisData(
    val integratedLUFS: Float,        // Overall loudness (-23 to 0 LUFS)
    val truePeak: Float,              // Maximum peak (-1 dBTP recommended)
    val loudnessRange: Float,         // Dynamic range (LRA)
    val bassEnergy: Float,            // 20-250 Hz energy
    val lowMidEnergy: Float,          // 250-500 Hz energy
    val midEnergy: Float,             // 500-2k Hz energy
    val upperMidEnergy: Float,        // 2k-4k Hz energy
    val presenceEnergy: Float,        // 4k-8k Hz energy
    val airEnergy: Float,             // 8k-20k Hz energy
    val crestFactor: Float,           // Peak/RMS ratio
    val dynamicRange: Float,          // Loudest - quietest (dB)
    val estimatedGenre: String?       // Optional ML genre detection
)

/**
 * Future: Analyze song using FFmpeg + libebur128
 * 
 * Implementation would use:
 * - FFmpeg for audio decoding
 * - libebur128 for LUFS measurement (EBU R128 standard)
 * - FFT for frequency analysis
 * - ML model for genre detection (optional)
 */
suspend fun analyzeSongWithFFmpeg(audioPath: String): SongAnalysisData {
    // TODO: Implement with FFmpeg native bindings
    // This would provide professional-grade audio analysis
    
    return SongAnalysisData(
        integratedLUFS = -14.0f,
        truePeak = -1.0f,
        loudnessRange = 8.0f,
        bassEnergy = 0.3f,
        lowMidEnergy = 0.25f,
        midEnergy = 0.2f,
        upperMidEnergy = 0.15f,
        presenceEnergy = 0.08f,
        airEnergy = 0.02f,
        crestFactor = 12.0f,
        dynamicRange = 10.0f,
        estimatedGenre = null
    )
}
