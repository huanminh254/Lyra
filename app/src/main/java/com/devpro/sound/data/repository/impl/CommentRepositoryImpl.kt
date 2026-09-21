package com.devpro.sound.data.repository.impl

import com.devpro.sound.data.model.Comment
import com.devpro.sound.data.remote.datasource.CommentRemoteDataSource
import com.devpro.sound.data.repository.CommentRepository
import kotlinx.coroutines.flow.Flow

class CommentRepositoryImpl(
    private val commentRemoteDataSource: CommentRemoteDataSource
) : CommentRepository {
    override fun observeComments(songId: String): Flow<List<Comment>> {
        return commentRemoteDataSource.observeComments(songId)
    }

    override suspend fun addComment(
        songId: String,
        content: String,
        timestampMs: Long
    ) {
        commentRemoteDataSource.addComment(songId, content, timestampMs)
    }
}
