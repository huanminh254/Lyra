package com.devpro.sound.data.remote.model


data class LoginResponse(
    val userId: String?,
    val email: String?,
    val isSuccess: Boolean,
    val message: String? = null
)