package com.phantom.player.data.repository

/**
 * Represents a single EQ band with frequency, gain value, and Q factor
 */
data class EqBand(
    val frequency: Int,      // Frequency in Hz (32, 64, 125, 250, 500, 1k, 2k, 4k, 8k, 16k)
    val value: Float,        // Gain in dB (-12 to +12)
    val q: Float            // Q factor (bandwidth) - typically 1.0
)
