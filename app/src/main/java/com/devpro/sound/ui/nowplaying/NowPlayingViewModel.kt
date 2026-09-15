package com.devpro.sound.ui.nowplaying

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.devpro.sound.data.model.Song
import com.devpro.sound.data.repository.SongRepository
import com.devpro.sound.data.repository.UserRepository
import com.devpro.sound.player.AudioPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val audioPlayer: AudioPlayer,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(NowPlayingUiState(isLoading = true))
    val uiState: LiveData<NowPlayingUiState> = _uiState

    private var playableSongs: List<Song> = emptyList()
    private var progressJob: Job? = null

    init {
        observePlayerState()
        loadSongs()
    }
    private fun loadSongs() {
        viewModelScope.launch {
            runCatching {
                songRepository.getSongs()
            }.onSuccess { songs ->
                val favoriteSongIds = runCatching {
                    userRepository.getFavoriteSongIds()
                }.getOrDefault(emptyList())

                playableSongs = songs.filter {
                    !it.audioUrl.isNullOrBlank()
                }

                audioPlayer.setPlayList(
                    playableSongs.mapNotNull { it.audioUrl }
                )

                updateState {
                    it.copy(
                        songs = songs,
                        song = playableSongs.firstOrNull(),
                        favoriteSongs = songs.filter { it.id in favoriteSongIds },
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                updateState {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message
                            ?: "Không tải được bài hát"
                    )
                }
            }
        }
    }

    fun refreshSongs() {
        loadSongs()
    }
    fun onPlayPauseClick() {
        val currentSong = _uiState.value?.song ?: return
        val audioUrl = currentSong.audioUrl?.takeIf { it.isNotBlank() } ?: return

        if (!audioPlayer.hasCurrentSong()) {
            audioPlayer.play(audioUrl)
        } else if (audioPlayer.isPlaying()) {
            audioPlayer.pause()
        } else {
            audioPlayer.resume()
        }
        updateState { it.copy(isPlaying = audioPlayer.isPlaying()) }
    }

    fun onSongClick(song: Song) {
        val index = playableSongs.indexOfFirst { it.id == song.id }
        if (index == -1) return
        audioPlayer.playAt(index)
        updateState { it.copy(song = song, isPlaying = audioPlayer.isPlaying()) }
    }

    fun toggleFavorite() {
        val currentSong = _uiState.value?.song ?: return
        val wasFavorite = _uiState.value?.favoriteSongs
            ?.any { it.id == currentSong.id } == true
        val updatedFavorites = if (wasFavorite) {
            _uiState.value?.favoriteSongs.orEmpty()
                .filterNot { it.id == currentSong.id }
        } else {
            _uiState.value?.favoriteSongs.orEmpty() + currentSong
        }

        updateState { state ->
            state.copy(favoriteSongs = updatedFavorites, errorMessage = null)
        }

        viewModelScope.launch {
            runCatching {
                if (wasFavorite) {
                    userRepository.removeFavoriteSong(currentSong.id)
                } else {
                    userRepository.addFavoriteSong(currentSong.id)
                }
            }.onFailure { exception ->
                updateState { state ->
                    state.copy(
                        favoriteSongs = if (wasFavorite) {
                            state.favoriteSongs + currentSong
                        } else {
                            state.favoriteSongs.filterNot { it.id == currentSong.id }
                        },
                        errorMessage = exception.message
                            ?: "Không thể cập nhật yêu thích"
                    )
                }
            }
        }
    }

    fun onSeek(progress: Float) {
        val duration = audioPlayer.getDuration()
        if (duration <= 0L) return
        val safeProgress = progress.coerceIn(0f, 1f)
        val seekPosition = (duration * safeProgress).toLong()
        audioPlayer.seekTo(seekPosition)
        updateState { it.copy(progress = safeProgress, currentPositionMs = seekPosition) }
    }

    fun onNextClick() {
        if (!audioPlayer.hasNext()) return
        audioPlayer.playNext()
        syncCurrentSong()
    }

    fun onPreviousClick() {
        if (!audioPlayer.hasPrevious()) return
        audioPlayer.playPrevious()
        syncCurrentSong()
    }

    override fun onCleared() {
        audioPlayer.release()
    }

    private fun observePlayerState() {
        audioPlayer.addListener(
            onIsPlayingChanged = { isPlaying ->
                updateState { it.copy(isPlaying = isPlaying) }
                if (isPlaying) startProgressUpdates() else progressJob?.cancel()
            },
            onPlaybackStateChanged = { playbackState ->
                updateState { it.copy(isBuffering = playbackState == Player.STATE_BUFFERING) }
            },
            onMediaItemTransition = { syncCurrentSong() }
        )
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive && audioPlayer.isPlaying()) {
                val currentPosition = audioPlayer.getCurrentPosition()
                val duration = audioPlayer.getDuration()
                val progress = if (duration > 0) {
                    (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
                updateState {
                    it.copy(
                        currentPositionMs = currentPosition.coerceAtLeast(0L),
                        durationMs = duration.coerceAtLeast(0L),
                        progress = progress
                    )
                }
                delay(500)
            }
        }
    }

    private fun syncCurrentSong() {
        val song = playableSongs.getOrNull(audioPlayer.getCurrentSongIndex()) ?: return
        updateState { it.copy(song = song, isPlaying = audioPlayer.isPlaying()) }
    }

    private fun updateState(transform: (NowPlayingUiState) -> NowPlayingUiState) {
        _uiState.value = _uiState.value?.let(transform)
    }

}
