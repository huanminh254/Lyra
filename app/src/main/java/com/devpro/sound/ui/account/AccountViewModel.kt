package com.devpro.sound.ui.account

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpro.sound.data.model.Song
import com.devpro.sound.data.repository.SongRepository
import com.devpro.sound.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val songRepository: SongRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableLiveData(AccountUiState(isLoading = true))
    val uiState: LiveData<AccountUiState> = _uiState
    private var hasLoaded = false
    private var loadedProfileId: String? = null

    fun loadProfile(profileId: String?) {
        val currentUserId = firebaseAuth.currentUser?.uid
        val normalizedProfileId = profileId
            ?.takeIf { it.isNotBlank() && it != currentUserId }
        loadedProfileId = normalizedProfileId
        load(normalizedProfileId)
    }

    fun refresh() {
        load(loadedProfileId)
    }

    private fun load(profileId: String?) {
        val isRefreshing = hasLoaded
        _uiState.value = (_uiState.value ?: AccountUiState()).copy(
            isLoading = !isRefreshing,
            isRefreshing = isRefreshing,
            errorMessage = null
        )

        viewModelScope.launch {
            runCatching {
                val user = if (profileId != null) {
                    userRepository.getUser(profileId)
                } else {
                    userRepository.getCurrentUser()
                }
                val songs = songRepository.getSongs()
                user to songs
            }.onSuccess { (user, songs) ->
                val accountName = user.name
                    .ifBlank {
                        if (profileId == null) {
                            firebaseAuth.currentUser?.displayName.orEmpty()
                        } else {
                            "Người dùng"
                        }
                    }
                    .ifBlank { firebaseAuth.currentUser?.email?.substringBefore("@").orEmpty() }
                    .ifBlank { "Tài khoản" }
                val songsById = songs.associateBy(Song::id)
                val likedSongs = user.favoriteSongIds.mapNotNull(songsById::get)
                val uploadedSongs = songs.filter { song ->
                    song.ownerId == user.id
                }

                val playlists = buildList {
                    if (uploadedSongs.isNotEmpty()) {
                        add(
                            AccountPlaylistUiModel(
                                id = "uploads",
                                title = "Bài hát đã đăng",
                                ownerName = accountName,
                                coverSong = uploadedSongs.firstOrNull()
                            )
                        )
                    }
                    if (likedSongs.isNotEmpty()) {
                        add(
                            AccountPlaylistUiModel(
                                id = "likes",
                                title = "Likes",
                                ownerName = accountName,
                                coverSong = likedSongs.firstOrNull()
                            )
                        )
                    }
                }

                _uiState.value = AccountUiState(
                    isPublicProfile = profileId != null,
                    displayName = accountName,
                    email = if (profileId == null) {
                        firebaseAuth.currentUser?.email.orEmpty()
                    } else {
                        user.accountSubtitle.ifBlank { "@${user.id.take(8)}" }
                    },
                    bio = user.bio,
                    avatarUrl = user.avatarUrl,
                    followersCount = user.followers.size,
                    followingCount = user.following.size,
                    playlists = playlists,
                    uploadedSongs = uploadedSongs,
                    likedSongs = likedSongs,
                    isRefreshing = false
                )
                hasLoaded = true
            }.onFailure { error ->
                _uiState.value = (_uiState.value ?: AccountUiState()).copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = error.message ?: "Không tải được dữ liệu tài khoản"
                )
            }
        }
    }

    fun updateAvatar(uri: Uri) {
        val currentState = _uiState.value ?: AccountUiState()
        _uiState.value = currentState.copy(
            isUploadingAvatar = true,
            avatarErrorMessage = null
        )

        viewModelScope.launch {
            runCatching {
                userRepository.updateAvatar(uri)
            }.onSuccess { avatarUrl ->
                _uiState.value = (_uiState.value ?: currentState).copy(
                    isUploadingAvatar = false,
                    avatarUrl = avatarUrl,
                    avatarErrorMessage = null
                )
            }.onFailure { error ->
                _uiState.value = (_uiState.value ?: currentState).copy(
                    isUploadingAvatar = false,
                    avatarErrorMessage = error.message ?: "Không thể cập nhật ảnh đại diện"
                )
            }
        }
    }

    fun updateName(name: String) {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) {
            _uiState.value = (_uiState.value ?: AccountUiState()).copy(
                nameErrorMessage = "Vui lòng nhập tên hiển thị"
            )
            return
        }

        val currentState = _uiState.value ?: AccountUiState()
        _uiState.value = currentState.copy(
            displayName = normalizedName,
            isUpdatingName = true,
            nameErrorMessage = null
        )

        viewModelScope.launch {
            runCatching {
                userRepository.updateName(normalizedName)
            }.onSuccess {
                _uiState.value = (_uiState.value ?: currentState).copy(
                    displayName = normalizedName,
                    isUpdatingName = false,
                    nameErrorMessage = null
                )
            }.onFailure { error ->
                _uiState.value = (_uiState.value ?: currentState).copy(
                    displayName = currentState.displayName,
                    isUpdatingName = false,
                    nameErrorMessage = error.message ?: "Không thể cập nhật tên"
                )
            }
        }
    }
}
