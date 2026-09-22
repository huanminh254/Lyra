package com.devpro.sound.ui.account

import com.devpro.sound.data.model.Song

data class AccountUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val playlists: List<AccountPlaylistUiModel> = emptyList(),
    val likedSongs: List<Song> = emptyList(),
    val errorMessage: String? = null,
    val avatarErrorMessage: String? = null
)

data class AccountPlaylistUiModel(
    val id: String,
    val title: String,
    val ownerName: String,
    val coverSong: Song?
)
