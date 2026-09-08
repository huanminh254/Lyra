package com.devpro.sound.data.remote.model

data class SongEntity(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val currentTime: String = "00:00",
    val duration: String = "",
    val audioUrl: String = "",
    val coverUrl: String = "",
    val audioObjectPath: String = "",
    val coverObjectPath: String = "",
    val originalFileName: String = "",
    val sizeBytes: Long = 0L,
    val sortOrder: Long = 0L,
    val sourceUrl: String? = null,
    val genre: String? = null,
    val year: String? = null
)
