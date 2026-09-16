package com.devpro.sound.data.repository.impl

import com.devpro.sound.data.remote.datasource.AuthRemoteDataSource
import com.devpro.sound.data.remote.model.LoginRequest
import com.devpro.sound.data.remote.model.LoginResponse
import com.devpro.sound.data.repository.AuthRepository

class AuthRepositoryImpl(
    private val authRemoteDataSource: AuthRemoteDataSource
): AuthRepository {
    override suspend fun login(request: LoginRequest): LoginResponse {
        return authRemoteDataSource.login(request)
    }

    override suspend fun register(request: LoginRequest): LoginResponse {
        return authRemoteDataSource.register(request)
    }

    override suspend fun sendPasswordResetEmail(email: String): LoginResponse {
        return authRemoteDataSource.sendPasswordResetEmail(email)
    }
}
