package com.devpro.sound.ui.auth


data class LoginUiState(
    val isSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val message: String = ""
)