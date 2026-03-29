package com.phantom.player.data.local.database.dao

import androidx.room.*
import com.phantom.player.data.local.database.entities.EqPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface EqPresetDao {
    @Query("SELECT * FROM eq_presets ORDER BY isCustom DESC, name ASC")
    fun getAllPresets(): Flow<List<EqPreset>>
    
    @Query("SELECT * FROM eq_presets WHERE id = :presetId")
    suspend fun getPresetById(presetId: Long): EqPreset?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: EqPreset): Long
    
    @Update
    suspend fun updatePreset(preset: EqPreset)
    
    @Delete
    suspend fun deletePreset(preset: EqPreset)
    
    @Query("DELETE FROM eq_presets WHERE isCustom = 1")
    suspend fun deleteCustomPresets()
}
