package com.devpro.sound.ui.discover

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpro.sound.data.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val songRepository: SongRepository
): ViewModel() {
    private val _uiState = MutableLiveData(DiscoverUiState())
    val uiState : LiveData<DiscoverUiState> = _uiState
    init {
        loadSongs()
    }
    fun loadSongs() {
        _uiState.value = _uiState.value?.copy(
            isLoading = true,
            errorMessenger = null
        )
        viewModelScope.launch {
            runCatching {
                songRepository.getSongs()
            }.onSuccess { songs ->
                _uiState.value = DiscoverUiState(
                    songs = songs,
                    isLoading = false
                )
            }.onFailure { error ->
                _uiState.value = DiscoverUiState(
                    isLoading = false,
                    errorMessenger = error.message ?: "Không tải được danh sách bài hát"
                )
            }
        }
    }
}
