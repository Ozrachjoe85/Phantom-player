package com.phantom.player.data.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val data: String,        // File path
    val duration: Long,
    val albumArt: String? = null
)
