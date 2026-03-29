package com.phantom.player.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eq_presets")
data class EqPreset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val bands: String, // JSON string of band values
    val isCustom: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class EqBand(
    val frequency: Int,
    val value: Float // dB value, typically -12.0 to +12.0
)
