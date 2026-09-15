package com.devpro.sound.data.repository

import com.devpro.sound.data.model.Song
import com.devpro.sound.data.remote.model.UploadSongRequest

interface UploadSongRepository {
    suspend fun uploadSong(request: UploadSongRequest): Song
}
