package com.phantom.player.data.local.database.dao

import androidx.room.*
import com.phantom.player.data.local.database.entities.SongEqProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface SongEqProfileDao {
    
    @Query("SELECT * FROM song_eq_profiles WHERE songId = :songId")
    fun getProfileForSong(songId: String): Flow<SongEqProfile?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: SongEqProfile)
    
    @Query("DELETE FROM song_eq_profiles WHERE songId = :songId")
    suspend fun deleteProfile(songId: String)
    
    @Query("SELECT COUNT(*) FROM song_eq_profiles")
    suspend fun getProfileCount(): Int
    
    @Query("SELECT * FROM song_eq_profiles")
    fun getAllProfiles(): Flow<List<SongEqProfile>>
}
