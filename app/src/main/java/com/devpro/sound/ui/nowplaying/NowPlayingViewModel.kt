package com.devpro.sound.ui.nowplaying

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.devpro.sound.data.model.Song
import com.devpro.sound.data.repository.SongRepository
import com.devpro.sound.data.repositoryImpl.SongRepositoryImpl
import com.devpro.sound.player.AudioPlayerManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NowPlayingViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val songRepository: SongRepository = SongRepositoryImpl()

    private val audioPlayerManager = AudioPlayerManager(
        context = application.applicationContext
    )

    var uiState by mutableStateOf(NowPlayingUiState(isLoading = true))
        private set

    init {
        observePlayerState()
        startProgressUpdates()
        loadSongs()
    }

    fun loadSongs() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            runCatching {
                songRepository.getSongs()
            }.onSuccess { songs ->
                audioPlayerManager.setPlayList(
                    songs.mapNotNull { song->
                        song.audioUrl
                    }
                )
                uiState = uiState.copy(
                    song = songs.firstOrNull(),
                    songs = songs,
                    isLoading = false,
                    errorMessage = null
                )
            }.onFailure { throwable ->
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Không tải được danh sách bài hát"
                )
            }
        }
    }

    fun onPlayPauseClick() {
        val currentSong = uiState.song ?: return
        val audioUrl = currentSong.audioUrl ?: return

        if (!audioPlayerManager.hasCurrentSong()) {
            audioPlayerManager.play(audioUrl)
            uiState = uiState.copy(isPlaying = true)
            return
        }
        if (audioPlayerManager.isPlaying()) {
            audioPlayerManager.pause()
            uiState = uiState.copy(isPlaying = false)
        } else {
            audioPlayerManager.resume()
            uiState = uiState.copy(isPlaying = true)
        }
    }
    fun onSongClick(song: Song){
        val index = uiState.songs.indexOfFirst { currentSong->
            currentSong.id == song.id
        }
        if(index == -1) return
        audioPlayerManager.playAt(index)
        uiState = uiState.copy(
            song = song,
            isPlaying = true
        )
    }
    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.release()
    }
    private fun observePlayerState(){
        audioPlayerManager.addListener(
            onIsPlayingChanged = {isPlaying ->
                uiState = uiState.copy(isPlaying = isPlaying)
            },
            onPlaybackStateChanged = { playbackState ->
                uiState = uiState.copy(
                    isBuffering = playbackState == Player.STATE_BUFFERING
                )
            }
        )
    }
    private fun startProgressUpdates(){
        viewModelScope.launch {
            while (true){
                val currentPosition = audioPlayerManager.getCurrentPosition()
                val duration = audioPlayerManager.getDuration()
                val progress = if (duration > 0){
                    currentPosition.toFloat() / duration.toFloat()
                }else{ 0f}
                uiState = uiState.copy(
                    currentPositionMs = currentPosition,
                    durationMs = duration,
                    progress = progress
                )
                delay(500)
            }
        }
    }
    private fun onSeek(progress: Float){
        val duration = audioPlayerManager.getDuration()
        if(duration<0) return
        val safeProgress = progress.coerceIn(0f,1f)
        val seekPosition = (duration * safeProgress).toLong()
        audioPlayerManager.seekTo(seekPosition)
        uiState = uiState.copy(progress = safeProgress, currentPositionMs = seekPosition)
    }
    fun onNextClick(){
        if(!audioPlayerManager.hasNext()) return
        audioPlayerManager.playNext()
        uiState = uiState.copy(
            song = uiState.songs.getOrNull(audioPlayerManager.getCurrentSongIndex()),
            isPlaying = audioPlayerManager.isPlaying()
        )
    }
    fun onPreviousClick(){
        if(!audioPlayerManager.hasPrevious()) return
        audioPlayerManager.playPrevious()
        uiState = uiState.copy(
            song = uiState.songs.getOrNull(audioPlayerManager.getCurrentSongIndex()),
            isPlaying = audioPlayerManager.isPlaying()
        )
    }

}
