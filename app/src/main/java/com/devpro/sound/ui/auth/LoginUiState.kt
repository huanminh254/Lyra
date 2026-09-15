package com.devpro.sound.ui.auth

import android.os.Message

data class LoginUiState(
    val isSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val message: String = ""
)