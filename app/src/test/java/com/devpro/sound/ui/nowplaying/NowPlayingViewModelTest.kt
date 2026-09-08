package com.devpro.sound.ui.nowplaying

import com.devpro.sound.data.model.Song
import com.devpro.sound.data.repository.SongRepository
import com.devpro.sound.MainDispatcherRule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NowPlayingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun onPlayPauseClickTogglesPlaybackState() = runTest {
        val viewModel = NowPlayingViewModel(FakeSongRepository())
        advanceUntilIdle()

        assertTrue(viewModel.uiState.isPlaying)

        viewModel.onPlayPauseClick()

        assertFalse(viewModel.uiState.isPlaying)

        viewModel.onPlayPauseClick()

        assertTrue(viewModel.uiState.isPlaying)
    }

    @Test
    fun uiStateUsesSongFromRepository() = runTest {
        val song = Song(
            title = "Test Song",
            artist = "Test Artist",
            currentTime = "00:10",
            duration = "02:30",
            coverResId = null,
            audioPath = "music/vpop/test_song.mp3",
            coverPath = "music/vpop/test_song.jpg",
            sourceUrl = "https://soundcloud.com/test/test-song",
            genre = "lofi",
            year = "2026"
        )
        val viewModel = NowPlayingViewModel(FakeSongRepository(song))
        advanceUntilIdle()

        val actualSong = viewModel.uiState.song!!

        assertEquals(song, actualSong)
        assertEquals("music/vpop/test_song.mp3", actualSong.audioPath)
        assertEquals("music/vpop/test_song.jpg", actualSong.coverPath)
        assertEquals("https://soundcloud.com/test/test-song", actualSong.sourceUrl)
        assertEquals("lofi", actualSong.genre)
        assertEquals("2026", actualSong.year)
    }

    private class FakeSongRepository(
        private val song: Song = Song(
            title = "Default Song",
            artist = "Default Artist",
            currentTime = "01:32",
            duration = "03:20",
            coverResId = null,
            audioPath = "music/vpop/default_song.mp3"
        )
    ) : SongRepository {

        override suspend fun getSongs(): List<Song> {
            return listOf(song)
        }

        override suspend fun getCurrentSong(): Song {
            return song
        }
    }
}
