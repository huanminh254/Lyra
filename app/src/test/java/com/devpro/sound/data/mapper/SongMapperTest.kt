package com.devpro.sound.data.mapper

import com.devpro.sound.data.model.Song
import com.devpro.sound.data.remote.model.SongEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SongMapperTest {

    @Test
    fun songEntityMapsToSong() {
        val entity = SongEntity(
            title = "Em Dung Khoc",
            artist = "Chillies",
            currentTime = "00:00",
            duration = "04:18",
            audioUrl = "https://example.com/audio.mp3",
            coverUrl = "https://example.com/cover.jpg",
            sourceUrl = "https://soundcloud.com/example",
            genre = "V-Pop",
            year = "2023"
        )

        val song = entity.toSong(id = "song_001")

        assertEquals("song_001", song.id)
        assertEquals("Em Dung Khoc", song.title)
        assertEquals("Chillies", song.artist)
        assertEquals("00:00", song.currentTime)
        assertEquals("04:18", song.duration)
        assertEquals("https://example.com/audio.mp3", song.audioUrl)
        assertEquals("https://example.com/cover.jpg", song.coverUrl)
        assertEquals("https://soundcloud.com/example", song.sourceUrl)
        assertEquals("V-Pop", song.genre)
        assertEquals("2023", song.year)
        assertNull(song.coverResId)
        assertNull(song.audioPath)
        assertNull(song.coverPath)
    }

    @Test
    fun songMapsToSongEntity() {
        val song = Song(
            id = "song_001",
            title = "Em Dung Khoc",
            artist = "Chillies",
            currentTime = "00:00",
            duration = "04:18",
            coverResId = null,
            audioUrl = "https://example.com/audio.mp3",
            coverUrl = "https://example.com/cover.jpg",
            sourceUrl = "https://soundcloud.com/example",
            genre = "V-Pop",
            year = "2023"
        )

        val entity = song.toEntity()

        assertEquals("Em Dung Khoc", entity.title)
        assertEquals("Chillies", entity.artist)
        assertEquals("00:00", entity.currentTime)
        assertEquals("04:18", entity.duration)
        assertEquals("https://example.com/audio.mp3", entity.audioUrl)
        assertEquals("https://example.com/cover.jpg", entity.coverUrl)
        assertEquals("https://soundcloud.com/example", entity.sourceUrl)
        assertEquals("V-Pop", entity.genre)
        assertEquals("2023", entity.year)
    }
}
