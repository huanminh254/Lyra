package com.devpro.sound.ui.nowplaying

import com.devpro.sound.data.model.Song

data class NowPlayingUiState(
    val song: Song? = null,
    val songs: List<Song> = emptyList(),
    val isBuffering: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPlaying: Boolean = true,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val progress: Float = 0f
)
