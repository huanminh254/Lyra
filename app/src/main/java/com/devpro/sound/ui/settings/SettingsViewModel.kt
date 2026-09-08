package com.devpro.sound.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpro.sound.data.model.User
import com.devpro.sound.data.repository.UserRepository
import com.devpro.sound.data.repositoryImpl.UserRepositoryImpl
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepository: UserRepository = UserRepositoryImpl()
) : ViewModel() {

    var user by mutableStateOf<User?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            runCatching {
                userRepository.getCurrentUser()
            }.onSuccess { currentUser ->
                user = currentUser
                isLoading = false
            }.onFailure { throwable ->
                isLoading = false
                errorMessage = throwable.message ?: "Không tải được thông tin người dùng"
            }
        }
    }
}
