package com.devpro.sound.data.model

data class Song(
    val id: String = "",
    val title: String,
    val artist: String,
    val currentTime: String,
    val duration: String,
    val coverResId: Int?,
    val audioPath: String? = null,
    val coverPath: String? = null,
    val audioUrl: String? = null,
    val coverUrl: String? = null,
    val ownerId: String = "",
    val sourceUrl: String? = null,
    val genre: String? = null,
    val year: String? = null,
    val viewCount: Long = 0L,
    val waveform: List<Float> = emptyList()
)
