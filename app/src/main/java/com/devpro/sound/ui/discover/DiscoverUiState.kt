package com.devpro.sound.ui.discover

import com.devpro.sound.data.model.Song

data class DiscoverUiState(
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessenger: String? = null
)
