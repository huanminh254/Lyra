package com.devpro.sound.ui.downloads

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpro.sound.data.remote.model.UploadSongRequest
import com.devpro.sound.data.repository.UploadSongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class UploadSongViewModel @Inject constructor(
    private val uploadSongRepository: UploadSongRepository
) : ViewModel() {
    private val _uiState = MutableLiveData(UploadSongUiState())
    val uiState: LiveData<UploadSongUiState> = _uiState

    fun upload(request: UploadSongRequest) {
        _uiState.value = UploadSongUiState(isLoading = true)

        viewModelScope.launch {
            runCatching {
                uploadSongRepository.uploadSong(request)
            }.onSuccess {
                _uiState.value = UploadSongUiState(
                    isSuccess = true,
                    message = "Đăng bài hát thành công"
                )
            }.onFailure { exception ->
                _uiState.value = UploadSongUiState(
                    message = exception.message ?: "Không thể đăng bài hát"
                )
            }
        }
    }
}
