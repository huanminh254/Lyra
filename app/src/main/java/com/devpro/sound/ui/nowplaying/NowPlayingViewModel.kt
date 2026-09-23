package com.devpro.sound.ui.nowplaying

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.devpro.sound.data.model.Comment
import com.devpro.sound.data.model.Song
import com.devpro.sound.data.repository.CommentRepository
import com.devpro.sound.data.repository.SongRepository
import com.devpro.sound.data.repository.UserRepository
import com.devpro.sound.player.AudioPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val audioPlayer: AudioPlayer,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(NowPlayingUiState(isLoading = true))
    val uiState: LiveData<NowPlayingUiState> = _uiState

    private val _likeCount = MutableLiveData(0)
    val likeCount: LiveData<Int> = _likeCount

    private val _commentCount = MutableLiveData(0)
    val commentCount: LiveData<Int> = _commentCount

    private val _currentUserAvatarUrl = MutableLiveData("")
    val currentUserAvatarUrl: LiveData<String> = _currentUserAvatarUrl

    private val _playbackProgress = MutableLiveData(0f)
    val playbackProgress: LiveData<Float> = _playbackProgress

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _currentComments = MutableStateFlow<List<Comment>>(emptyList())
    val currentComments: StateFlow<List<Comment>> = _currentComments.asStateFlow()

    private var playableSongs: List<Song> = emptyList()
    private var songsJob: Job? = null
    private var progressJob: Job? = null
    private var commentsJob: Job? = null
    private var currentCommentSongId: String? = null
    private var currentCommentSecond: Long? = null
    private var lastCommentPositionMs = 0L
    private val listenedMsBySong = mutableMapOf<String, Long>()
    private val recordedViewSongIds = mutableSetOf<String>()
    private val pendingViewSongIds = mutableSetOf<String>()

    init {
        observePlayerState()
        loadSongs(preservePlayback = false)
    }

    private fun loadSongs(preservePlayback: Boolean) {
        songsJob?.cancel()
        updateState {
            it.copy(
                isLoading = !preservePlayback,
                isRefreshing = preservePlayback,
                errorMessage = null
            )
        }

        songsJob = viewModelScope.launch {
            val previousState = _uiState.value ?: NowPlayingUiState()
            val previousSongId = previousState.song?.id
            val hadCurrentMedia = audioPlayer.hasCurrentSong()
            val wasPlaying = audioPlayer.isPlaying()
            val previousPositionMs = audioPlayer.getCurrentPosition().coerceAtLeast(0L)
            val previousDurationMs = audioPlayer.getDuration().coerceAtLeast(0L)

            runCatching {
                songRepository.getSongs()
            }.onSuccess { songs ->
                val favoriteSongIds = runCatching {
                    userRepository.getFavoriteSongIds()
                }.getOrDefault(emptyList())

                val refreshedPlayableSongs = songs.filter {
                    !it.audioUrl.isNullOrBlank()
                }
                val previousAudioUrls = playableSongs.mapNotNull { it.audioUrl }
                val refreshedAudioUrls = refreshedPlayableSongs.mapNotNull { it.audioUrl }
                val playlistChanged = previousAudioUrls != refreshedAudioUrls
                playableSongs = refreshedPlayableSongs

                if (!preservePlayback || playlistChanged) {
                    audioPlayer.setPlayList(refreshedAudioUrls)
                }

                val selectedSong = (if (preservePlayback && previousSongId != null) {
                    songs.firstOrNull { it.id == previousSongId }
                } else {
                    null
                }) ?: playableSongs.firstOrNull()
                val restoredSongIndex = playableSongs.indexOfFirst { it.id == previousSongId }
                val shouldRestorePlayback = preservePlayback &&
                    playlistChanged &&
                    hadCurrentMedia &&
                    restoredSongIndex >= 0
                val canKeepCurrentPlayback = preservePlayback &&
                    !playlistChanged &&
                    hadCurrentMedia &&
                    previousSongId == selectedSong?.id

                if (shouldRestorePlayback) {
                    audioPlayer.playAt(restoredSongIndex)
                    audioPlayer.seekTo(previousPositionMs)
                    if (!wasPlaying) audioPlayer.pause()
                }

                _likeCount.value = if (selectedSong != null && selectedSong.id in favoriteSongIds) {
                    1
                } else {
                    0
                }
                val restoredProgress = if (shouldRestorePlayback && previousDurationMs > 0L) {
                    (previousPositionMs.toFloat() / previousDurationMs.toFloat()).coerceIn(0f, 1f)
                } else if (canKeepCurrentPlayback) {
                    previousState.progress
                } else {
                    0f
                }
                _playbackProgress.value = restoredProgress

                updateState {
                    it.copy(
                        songs = songs,
                        song = selectedSong,
                        favoriteSongs = songs.filter { it.id in favoriteSongIds },
                        isLoading = false,
                        isRefreshing = false,
                        isPlaying = when {
                            shouldRestorePlayback -> wasPlaying
                            canKeepCurrentPlayback -> previousState.isPlaying
                            else -> false
                        },
                        currentPositionMs = when {
                            shouldRestorePlayback -> previousPositionMs
                            canKeepCurrentPlayback -> previousState.currentPositionMs
                            else -> 0L
                        },
                        durationMs = when {
                            shouldRestorePlayback -> previousDurationMs
                            canKeepCurrentPlayback -> previousState.durationMs
                            else -> 0L
                        },
                        progress = restoredProgress,
                        errorMessage = null
                    )
                }
                if (!preservePlayback || previousSongId != selectedSong?.id) {
                    observeCommentsForSong(selectedSong?.id)
                }
            }.onFailure { error ->
                updateState {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = error.message
                            ?: "Không tải được bài hát"
                    )
                }
            }
        }
    }

    fun refreshSongs() {
        loadSongs(preservePlayback = true)
    }

    fun refreshCurrentUserAvatar() {
        viewModelScope.launch {
            _currentUserAvatarUrl.value = runCatching {
                userRepository.getCurrentUser().avatarUrl
            }.getOrDefault("")
        }
    }

    fun onPlayPauseClick() {
        val state = _uiState.value ?: return

        if (audioPlayer.hasCurrentSong()) {
            if (audioPlayer.isPlaying()) {
                audioPlayer.pause()
            } else {
                audioPlayer.resume()
            }
        } else {
            val audioUrl = state.song?.audioUrl?.takeIf { it.isNotBlank() }
                ?: return
            audioPlayer.play(audioUrl)
        }

        updateState { it.copy(isPlaying = audioPlayer.isPlaying()) }
    }

    fun onSongClick(song: Song) {
        val index = playableSongs.indexOfFirst { it.id == song.id }
        if (index == -1) return
        audioPlayer.playAt(index)
        _playbackProgress.value = 0f
        _likeCount.value = if (_uiState.value?.favoriteSongs?.any { it.id == song.id } == true) {
            1
        } else {
            0
        }
        updateState { it.copy(song = song, isPlaying = audioPlayer.isPlaying()) }
        observeCommentsForSong(song.id)
    }

    fun onSongPlayClick(song: Song) {
        if (_uiState.value?.song?.id == song.id) {
            onPlayPauseClick()
        } else {
            onSongClick(song)
        }
    }

    fun toggleFavorite() {
        val previousState = _uiState.value ?: return
        val currentSong = previousState.song ?: return
        val wasFavorite = previousState.favoriteSongs
            ?.any { it.id == currentSong.id } == true
        val previousLikeCount = _likeCount.value ?: 0
        val previousFavoriteSongs = previousState.favoriteSongs
        val updatedSong = currentSong
        val updatedSongs = previousState.songs.map { song ->
            if (song.id == currentSong.id) updatedSong else song
        }
        val updatedFavorites = if (wasFavorite) {
            previousFavoriteSongs
                .filterNot { it.id == currentSong.id }
        } else {
            previousFavoriteSongs + updatedSong
        }

        updateState { state ->
            state.copy(
                song = updatedSong,
                songs = updatedSongs,
                favoriteSongs = updatedFavorites,
                errorMessage = null
            )
        }
        _likeCount.value = if (wasFavorite) {
            (previousLikeCount - 1).coerceAtLeast(0)
        } else {
            previousLikeCount + 1
        }

        viewModelScope.launch {
            runCatching {
                if (wasFavorite) {
                    userRepository.removeFavoriteSong(currentSong.id)
                } else {
                    userRepository.addFavoriteSong(currentSong.id)
                }
            }.onFailure { exception ->
                _likeCount.value = previousLikeCount
                updateState { state ->
                    state.copy(
                        song = if (state.song?.id == currentSong.id) currentSong else state.song,
                        songs = state.songs.map { song ->
                            if (song.id == currentSong.id) currentSong else song
                        },
                        favoriteSongs = previousFavoriteSongs,
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
        _playbackProgress.value = safeProgress
        updateState { it.copy(progress = safeProgress, currentPositionMs = seekPosition) }
        refreshCurrentCommentGroup(seekPosition, force = true)
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
        commentsJob?.cancel()
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
            var lastSongId = _uiState.value?.song?.id
            var lastPositionMs = audioPlayer.getCurrentPosition()

            while (isActive && audioPlayer.isPlaying()) {
                val currentPosition = audioPlayer.getCurrentPosition()
                val duration = audioPlayer.getDuration()
                val currentSongId = _uiState.value?.song?.id

                if (currentSongId != null && currentSongId == lastSongId) {
                    val positionDeltaMs = currentPosition - lastPositionMs
                    val listenedMs = if (positionDeltaMs in 0L..1_500L) {
                        (listenedMsBySong[currentSongId] ?: 0L) + positionDeltaMs
                    } else {
                        listenedMsBySong[currentSongId] ?: 0L
                    }
                    listenedMsBySong[currentSongId] = listenedMs
                    if (listenedMs >= LISTENED_MS_FOR_VIEW) {
                        recordViewIfNeeded(currentSongId)
                    }
                } else {
                    lastSongId = currentSongId
                }
                lastPositionMs = currentPosition

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
                _playbackProgress.value = progress
                refreshCurrentCommentGroup(currentPosition)
                delay(PROGRESS_UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun recordViewIfNeeded(songId: String) {
        if (songId in recordedViewSongIds || !pendingViewSongIds.add(songId)) return

        viewModelScope.launch {
            runCatching {
                songRepository.recordView(songId)
            }.onSuccess { wasCounted ->
                recordedViewSongIds.add(songId)
                if (wasCounted) {
                    updateSongViewCount(songId)
                }
            }.also {
                pendingViewSongIds.remove(songId)
            }
        }
    }

    private fun updateSongViewCount(songId: String) {
        updateState { state ->
            val updatedSong = state.song
                ?.takeIf { it.id == songId }
                ?.let { song -> song.copy(viewCount = song.viewCount + 1) }
            val updatedSongs = state.songs.map { song ->
                if (song.id == songId) song.copy(viewCount = song.viewCount + 1) else song
            }
            val updatedFavorites = state.favoriteSongs.map { song ->
                if (song.id == songId) song.copy(viewCount = song.viewCount + 1) else song
            }
            state.copy(
                song = updatedSong ?: state.song,
                songs = updatedSongs,
                favoriteSongs = updatedFavorites
            )
        }
    }

    private fun syncCurrentSong() {
        val song = playableSongs.getOrNull(audioPlayer.getCurrentSongIndex()) ?: return
        _likeCount.value = if (_uiState.value?.favoriteSongs?.any { it.id == song.id } == true) {
            1
        } else {
            0
        }
        updateState { it.copy(song = song, isPlaying = audioPlayer.isPlaying()) }
        observeCommentsForSong(song.id)
    }

    private fun observeCommentsForSong(songId: String?) {
        commentsJob?.cancel()
        _comments.value = emptyList()
        _currentComments.value = emptyList()
        _commentCount.value = 0
        currentCommentSongId = songId
        currentCommentSecond = null
        lastCommentPositionMs = 0L

        if (songId.isNullOrBlank()) return

        commentsJob = viewModelScope.launch {
            commentRepository
                .observeComments(songId)
                .catch {
                    _comments.value = emptyList()
                    _currentComments.value = emptyList()
                }
                .collect { commentList ->
                    _comments.value = commentList
                    _commentCount.value = commentList.size
                    if (currentCommentSecond == null) {
                        refreshCurrentCommentGroup(
                            positionMs = _uiState.value?.currentPositionMs ?: 0L,
                            force = true
                        )
                    }
                }
        }
    }

    fun addComment(content: String) {
        val cleanContent = content.trim()
        val currentSong = _uiState.value?.song ?: return
        if (cleanContent.isBlank()) return

        viewModelScope.launch {
            runCatching {
                commentRepository.addComment(
                    songId = currentSong.id,
                    content = cleanContent,
                    timestampMs = _uiState.value?.currentPositionMs
                        ?: audioPlayer.getCurrentPosition()
                )
            }.onFailure { exception ->
                updateState { state ->
                    state.copy(
                        errorMessage = exception.message
                            ?: "Không thể gửi bình luận"
                    )
                }
            }
        }
    }

    private fun refreshCurrentCommentGroup(
        positionMs: Long,
        force: Boolean = false
    ) {
        val safePositionMs = positionMs.coerceAtLeast(0L)
        val second = safePositionMs / 1_000L
        val movedBackward = safePositionMs < lastCommentPositionMs
        val shouldRefresh = force ||
            currentCommentSecond == null ||
            currentCommentSecond != second ||
            movedBackward

        if (!shouldRefresh) {
            lastCommentPositionMs = safePositionMs
            return
        }

        currentCommentSecond = second
        lastCommentPositionMs = safePositionMs
        _currentComments.value = _comments.value
            .filter { comment ->
                comment.timestampMs / 1_000L == second &&
                    comment.timestampMs >= safePositionMs
            }
            .sortedWith(
                compareBy<Comment> { it.timestampMs }
                    .thenBy { it.createdAtMillis }
            )
    }

    private fun updateState(transform: (NowPlayingUiState) -> NowPlayingUiState) {
        _uiState.value = _uiState.value?.let(transform)
    }

    private companion object {
        const val LISTENED_MS_FOR_VIEW = 15_000L
        const val PROGRESS_UPDATE_INTERVAL_MS = 100L
    }

}
