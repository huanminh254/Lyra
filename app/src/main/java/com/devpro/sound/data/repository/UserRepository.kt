package com.devpro.sound.data.repository

import com.devpro.sound.data.model.User

interface UserRepository {
    suspend fun getCurrentUser(): User
}
