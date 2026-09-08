package com.devpro.sound.data.remote.model

data class UserEntity(
    val id: String = "",
    val name: String = "",
    val accountSubtitle: String = "",
    val audioQuality: String = "",
    val streamOnlyOnWifi: Boolean = false,
    val darkModeEnabled: Boolean = false,
    val cacheSubtitle: String = "",
    val appVersion: String = ""
)
