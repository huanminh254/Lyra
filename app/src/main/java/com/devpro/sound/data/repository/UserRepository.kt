package com.devpro.sound.data.repository

interface UserRepository {
    suspend fun getFavoriteSongIds(): List<String>
    suspend fun addFavoriteSong(songId: String)
    suspend fun removeFavoriteSong(songId: String)
}
