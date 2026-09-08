package com.devpro.sound.data.repository

import com.devpro.sound.data.model.Song

interface SongRepository {
    suspend fun getSongs(): List<Song>

    suspend fun getCurrentSong(): Song
}
