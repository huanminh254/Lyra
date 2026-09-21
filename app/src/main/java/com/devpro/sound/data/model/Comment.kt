package com.devpro.sound.data.model

/**
 * Comment attached to a specific position in a song.
 *
 * @property timestampMs playback position where the comment should appear.
 * @property createdAtMillis creation time used to order comments that share
 * the same playback second.
 */
data class Comment(
    val id: String = "",
    val songId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String? = null,
    val content: String = "",
    val timestampMs: Long = 0L,
    val createdAtMillis: Long = 0L
)
