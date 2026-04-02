package com.phantom.player.data.model

data class EqBand(
    val frequency: Int,
    val value: Float,
    val q: Float = 1.0f
)
