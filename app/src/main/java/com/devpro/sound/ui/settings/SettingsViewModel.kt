package com.devpro.sound.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devpro.sound.data.model.User
import com.devpro.sound.data.repository.UserRepository
import com.devpro.sound.data.repositoryImpl.UserRepositoryImpl
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user
    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            runCatching { userRepository.getCurrentUser() }
                .onSuccess {
                    _user.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    _isLoading.value = false
                    _errorMessage.value = it.message ?: "Không tải được thông tin người dùng"
                }
        }
    }

    class Factory(private val repository: UserRepository = UserRepositoryImpl()) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(repository) as T
    }
}
