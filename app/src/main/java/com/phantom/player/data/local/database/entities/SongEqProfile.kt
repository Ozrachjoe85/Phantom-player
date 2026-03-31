package com.phantom.player.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores EQ profiles for individual songs
 * Each song can have its own custom EQ settings
 */
@Entity(tableName = "song_eq_profiles")
data class SongEqProfile(
    @PrimaryKey val songId: String,  // Links to Song.id
    val bands: String,  // JSON string of EqBand values
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Represents the "original mastering" - a flat EQ curve
 * Used as the ghost overlay baseline
 */
fun getOriginalMasteringBands(): List<EqBand> {
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
