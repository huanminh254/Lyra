package com.devpro.sound.ui.settings

data class UserUiState(
    val id: String,
    val name: String,
    val accountSubtitle: String,
    val audioQuality: String,
    val streamOnlyOnWifi: Boolean,
    val darkModeEnabled: Boolean,
    val cacheSubtitle: String,
)