package com.devpro.sound.data.repositoryImpl

import com.devpro.sound.data.mapper.toSong
import com.devpro.sound.data.model.Song
import com.devpro.sound.data.remote.datasource.SongRemoteDataSource
import com.devpro.sound.data.repository.SongRepository

class SongRepositoryImpl(
    private val songRemoteDataSource: SongRemoteDataSource = SongRemoteDataSource()
) : SongRepository {

    override suspend fun getSongs(): List<Song> {
        return songRemoteDataSource.getSongs().map { songEntity ->
            songEntity.toSong()
        }
    }

    override suspend fun getCurrentSong(): Song {
        return getSongs().first()
    }
}
