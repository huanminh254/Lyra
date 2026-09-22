package com.devpro.sound.data.repository

import android.net.Uri
import com.devpro.sound.data.remote.model.UserEntity

interface UserRepository {
    suspend fun getCurrentUser(): UserEntity
    suspend fun getFavoriteSongIds(): List<String>
    suspend fun addFavoriteSong(songId: String)
    suspend fun removeFavoriteSong(songId: String)
    suspend fun updateAvatar(uri: Uri): String
}
