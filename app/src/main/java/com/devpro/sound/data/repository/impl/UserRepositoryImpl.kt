package com.devpro.sound.data.repository.impl

import com.devpro.sound.data.remote.datasource.UserRemoteDataSource
import com.devpro.sound.data.repository.UserRepository

class UserRepositoryImpl(
    private val userRemoteDataSource: UserRemoteDataSource
) : UserRepository {

    override suspend fun getFavoriteSongIds(): List<String> {
        return userRemoteDataSource.getFavoriteSongIds()
    }

    override suspend fun addFavoriteSong(songId: String) {
        userRemoteDataSource.addFavoriteSong(songId)
    }

    override suspend fun removeFavoriteSong(songId: String) {
        userRemoteDataSource.removeFavoriteSong(songId)
    }

}
