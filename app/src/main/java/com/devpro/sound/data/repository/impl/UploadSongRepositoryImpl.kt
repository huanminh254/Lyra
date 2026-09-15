package com.devpro.sound.data.repository.impl

import com.devpro.sound.data.mapper.toSong
import com.devpro.sound.data.model.Song
import com.devpro.sound.data.remote.datasource.SongUploadRemoteDataSource
import com.devpro.sound.data.remote.model.UploadSongRequest
import com.devpro.sound.data.repository.UploadSongRepository

class UploadSongRepositoryImpl(
    private val dataSource: SongUploadRemoteDataSource
) : UploadSongRepository {
    override suspend fun uploadSong(request: UploadSongRequest): Song {
        return dataSource.uploadSong(request).toSong()
    }
}
