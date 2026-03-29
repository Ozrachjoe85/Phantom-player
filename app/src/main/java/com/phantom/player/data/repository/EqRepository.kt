package com.phantom.player.data.repository

import com.phantom.player.data.local.database.dao.EqPresetDao
import com.phantom.player.data.local.database.entities.EqBand
import com.phantom.player.data.local.database.entities.EqPreset
import com.phantom.player.domain.audio.EqualizerProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EqRepository @Inject constructor(
    private val eqPresetDao: EqPresetDao,
    private val equalizerProcessor: EqualizerProcessor
) {
    val bands: StateFlow<List<EqBand>> = equalizerProcessor.bands
    val isEnabled: StateFlow<Boolean> = equalizerProcessor.isEnabled
    
    fun initialize(audioSessionId: Int) {
        equalizerProcessor.initialize(audioSessionId)
    }
    
    fun setBandValue(bandIndex: Int, value: Float) {
        equalizerProcessor.setBandValue(bandIndex, value)
    }
    
    fun loadPreset(bands: List<EqBand>) {
        equalizerProcessor.loadPreset(bands)
    }
    
    fun resetAllBands() {
        equalizerProcessor.resetAllBands()
    }
    
    fun setEnabled(enabled: Boolean) {
        equalizerProcessor.setEnabled(enabled)
    }
    
    fun getPresetNames(): List<String> {
        return equalizerProcessor.getPresetNames()
    }
    
    fun useBuiltInPreset(presetIndex: Int) {
        equalizerProcessor.useBuiltInPreset(presetIndex)
    }
    
    // Database operations
    fun getAllPresets(): Flow<List<EqPreset>> = eqPresetDao.getAllPresets()
    
    suspend fun getPresetById(presetId: Long): EqPreset? = 
        eqPresetDao.getPresetById(presetId)
    
    suspend fun savePreset(name: String, bands: List<EqBand>): Long {
        val bandsJson = Json.encodeToString(bands)
        val preset = EqPreset(name = name, bands = bandsJson, isCustom = true)
        return eqPresetDao.insertPreset(preset)
    }
    
    suspend fun updatePreset(preset: EqPreset) = eqPresetDao.updatePreset(preset)
    
    suspend fun deletePreset(preset: EqPreset) = eqPresetDao.deletePreset(preset)
    
    suspend fun deleteCustomPresets() = eqPresetDao.deleteCustomPresets()
}
