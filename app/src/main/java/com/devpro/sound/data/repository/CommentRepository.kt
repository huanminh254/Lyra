package com.devpro.sound.data.repository

import com.devpro.sound.data.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun observeComments(songId: String): Flow<List<Comment>>

    suspend fun addComment(
        songId: String,
        content: String,
        timestampMs: Long
    )
}
