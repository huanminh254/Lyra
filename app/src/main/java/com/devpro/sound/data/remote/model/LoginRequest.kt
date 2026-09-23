package com.devpro.sound.data.remote.model

data class LoginRequest(
    val email: String,
    val password: String,
    val name: String = ""
)
