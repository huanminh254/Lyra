package com.devpro.sound.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpro.sound.data.model.Song
import com.devpro.sound.data.remote.model.UserEntity
import com.devpro.sound.data.repository.SongRepository
import com.devpro.sound.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

data class AccountListUiState(
    val isLoading: Boolean = false,
    val title: String = "",
    val users: List<UserEntity> = emptyList(),
    val songs: List<Song> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class AccountListViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val songRepository: SongRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(AccountListUiState())
    val uiState: LiveData<AccountListUiState> = _uiState

    fun load(profileId: String?, mode: String) {
        val title = when (mode) {
            AccountListFragment.MODE_FOLLOWING -> "Following"
            AccountListFragment.MODE_FOLLOWERS -> "Followers"
            else -> "Likes"
        }
        _uiState.value = AccountListUiState(isLoading = true, title = title)

        viewModelScope.launch {
            runCatching {
                val targetUser = if (profileId.isNullOrBlank()) {
                    userRepository.getCurrentUser()
                } else {
                    userRepository.getUser(profileId)
                }

                when (mode) {
                    AccountListFragment.MODE_FOLLOWING -> {
                        targetUser to loadUsers(targetUser.following)
                    }
                    AccountListFragment.MODE_FOLLOWERS -> {
                        targetUser to loadUsers(targetUser.followers)
                    }
                    else -> {
                        val songsById = songRepository.getSongs().associateBy(Song::id)
                        targetUser to targetUser.favoriteSongIds.mapNotNull(songsById::get)
                    }
                }
            }.onSuccess { (_, content) ->
                when (content) {
                    is List<*> -> {
                        val users = content.filterIsInstance<UserEntity>()
                        val songs = content.filterIsInstance<Song>()
                        _uiState.value = AccountListUiState(
                            title = title,
                            users = users,
                            songs = songs
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.value = AccountListUiState(
                    title = title,
                    errorMessage = error.message ?: "Không tải được danh sách"
                )
            }
        }
    }

    private suspend fun loadUsers(userIds: List<String>): List<UserEntity> = coroutineScope {
        userIds.map { userId ->
            async {
                runCatching { userRepository.getUser(userId) }
                    .getOrNull()
            }
        }.awaitAll().filterNotNull()
    }
}
