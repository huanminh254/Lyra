package com.devpro.sound.data.mapper

import com.devpro.sound.data.model.Song
import com.devpro.sound.data.remote.model.SongEntity

fun SongEntity.toSong(id: String = this.id): Song {
    return Song(
        id = id,
        title = title,
        artist = artist,
        currentTime = currentTime,
        duration = duration,
        coverResId = null,
        audioUrl = audioUrl,
        coverUrl = coverUrl,
        ownerId = ownerId,
        sourceUrl = sourceUrl,
        genre = genre,
        year = year,
        viewCount = viewCount
    )
}

fun Song.toEntity(): SongEntity {
    return SongEntity(
        id = id,
        title = title,
        artist = artist,
        currentTime = currentTime,
        duration = duration,
        audioUrl = audioUrl.orEmpty(),
        coverUrl = coverUrl.orEmpty(),
        ownerId = ownerId,
        sourceUrl = sourceUrl,
        genre = genre,
        year = year,
        viewCount = viewCount
    )
}
