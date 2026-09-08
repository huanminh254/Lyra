package com.devpro.sound.data.mapper

import com.devpro.sound.data.model.User
import com.devpro.sound.data.remote.model.UserEntity

fun UserEntity.toUser(id: String = this.id): User {
    return User(
        id = id,
        name = name,
        accountSubtitle = accountSubtitle,
        audioQuality = audioQuality,
        streamOnlyOnWifi = streamOnlyOnWifi,
        darkModeEnabled = darkModeEnabled,
        cacheSubtitle = cacheSubtitle,
        appVersion = appVersion
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        accountSubtitle = accountSubtitle,
        audioQuality = audioQuality,
        streamOnlyOnWifi = streamOnlyOnWifi,
        darkModeEnabled = darkModeEnabled,
        cacheSubtitle = cacheSubtitle,
        appVersion = appVersion
    )
}
