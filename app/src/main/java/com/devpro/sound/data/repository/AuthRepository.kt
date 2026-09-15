package com.devpro.sound.data.repository

import com.devpro.sound.data.remote.model.LoginRequest
import com.devpro.sound.data.remote.model.LoginResponse

interface AuthRepository {
    suspend fun login(request: LoginRequest): LoginResponse
}