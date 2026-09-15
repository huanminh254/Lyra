package com.devpro.sound.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpro.sound.data.remote.model.LoginRequest
import com.devpro.sound.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
): ViewModel() {
    private val _uiState = MutableLiveData(LoginUiState())
    val uiState: LiveData<LoginUiState> = _uiState
    fun login(request: LoginRequest){
        _uiState.value = LoginUiState(isLoading = true)
        viewModelScope.launch {
            val response = authRepository.login(request)
            _uiState.value = LoginUiState(
                isSuccess = response.isSuccess,
                isLoading = false,
                message = response.message.orEmpty()
            )
        }
    }
}
