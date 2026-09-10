package com.devpro.sound.ui.nowplaying

import com.devpro.sound.data.model.Song
import com.devpro.sound.data.repository.SongRepository
import com.devpro.sound.player.AudioPlayer
import com.devpro.sound.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import androidx.arch.core.executor.testing.InstantTaskExecutorRule

@OptIn(ExperimentalCoroutinesApi::class)
class NowPlayingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun onPlayPauseClickTogglesPlaybackState() = runTest {
        val viewModel = createViewModel()
        runCurrent()

        assertFalse(viewModel.uiState.value!!.isPlaying)
        viewModel.onPlayPauseClick()
        assertTrue(viewModel.uiState.value!!.isPlaying)
        viewModel.onPlayPauseClick()
        assertFalse(viewModel.uiState.value!!.isPlaying)
    }

    @Test
    fun uiStateUsesSongFromRepository() = runTest {
        val song = Song(
            title = "Test Song",
            artist = "Test Artist",
            currentTime = "00:10",
            duration = "02:30",
            coverResId = null,
            audioUrl = "https://example.com/audio.mp3",
            coverUrl = "https://example.com/cover.jpg",
            sourceUrl = "https://soundcloud.com/test/test-song",
            genre = "lofi",
            year = "2026"
        )
        val viewModel = createViewModel(song)
        runCurrent()

        assertEquals(song, viewModel.uiState.value!!.song)
        assertEquals(listOf(song), viewModel.uiState.value!!.songs)
    }

    private fun createViewModel(song: Song = defaultSong()): NowPlayingViewModel {
        return NowPlayingViewModel(
            songRepository = FakeSongRepository(song),
            audioPlayer = FakeAudioPlayer()
        )
    }

    private fun defaultSong() = Song(
        title = "Default Song",
        artist = "Default Artist",
        currentTime = "01:32",
        duration = "03:20",
        coverResId = null,
        audioUrl = "https://example.com/default.mp3"
    )

    private class FakeSongRepository(private val song: Song) : SongRepository {
        override suspend fun getSongs(): List<Song> = listOf(song)
        override suspend fun getCurrentSong(): Song = song
    }

    private class FakeAudioPlayer : AudioPlayer {
        private var songs = emptyList<String>()
        private var currentIndex = 0
        private var playing = false
        private var listener: ((Boolean) -> Unit)? = null

        override fun hasCurrentSong() = songs.isNotEmpty()
        override fun play(audioUrl: String) { playing = true; listener?.invoke(true) }
        override fun setPlayList(audioUrls: List<String>) { songs = audioUrls; currentIndex = 0 }
        override fun playAt(index: Int) { currentIndex = index; playing = true; listener?.invoke(true) }
        override fun playNext() { currentIndex++; listener?.invoke(playing) }
        override fun playPrevious() { currentIndex--; listener?.invoke(playing) }
        override fun hasNext() = currentIndex < songs.lastIndex
        override fun hasPrevious() = currentIndex > 0
        override fun getCurrentSongIndex() = currentIndex
        override fun pause() { playing = false; listener?.invoke(false) }
        override fun resume() { playing = true; listener?.invoke(true) }
        override fun isPlaying() = playing
        override fun seekTo(positionMs: Long) = Unit
        override fun getCurrentPosition() = 0L
        override fun getDuration() = 180_000L
        override fun addListener(
            onIsPlayingChanged: (Boolean) -> Unit,
            onPlaybackStateChanged: (Int) -> Unit,
            onMediaItemTransition: (Int) -> Unit
        ) { listener = onIsPlayingChanged }
        override fun release() = Unit
    }
}
