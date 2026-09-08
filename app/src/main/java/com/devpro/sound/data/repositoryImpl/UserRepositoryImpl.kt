package com.devpro.sound.data.repositoryImpl

import com.devpro.sound.data.mapper.toUser
import com.devpro.sound.data.model.User
import com.devpro.sound.data.remote.datasource.UserRemoteDataSource
import com.devpro.sound.data.repository.UserRepository

class UserRepositoryImpl(
    private val userRemoteDataSource: UserRemoteDataSource = UserRemoteDataSource()
) : UserRepository {

    override suspend fun getCurrentUser(): User {
        return userRemoteDataSource.getCurrentUser().toUser()
    }
}
