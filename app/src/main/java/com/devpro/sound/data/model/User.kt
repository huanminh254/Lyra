package com.devpro.sound.data.model

data class User(
    val id: String,
    val name: String,
    val accountSubtitle: String,
    val audioQuality: String,
    val streamOnlyOnWifi: Boolean,
    val darkModeEnabled: Boolean,
    val cacheSubtitle: String,
    val appVersion: String,
    val uploadedSongIds: List<String> = emptyList(),
    val favoriteSongIds: List<String> = emptyList()
)
