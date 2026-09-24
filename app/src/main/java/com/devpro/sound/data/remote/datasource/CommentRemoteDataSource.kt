package com.devpro.sound.data.remote.datasource

import com.devpro.sound.data.model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class CommentRemoteDataSource(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    fun observeComments(songId: String): Flow<List<Comment>> = callbackFlow {
        val registration = firestore
            .collection(SONGS_COLLECTION)
            .document(songId)
            .collection(COMMENTS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val comments = snapshot
                    ?.documents
                    .orEmpty()
                    .mapNotNull { document ->
                        val timestampMs = document.getLong(TIMESTAMP_MS_FIELD)
                            ?: return@mapNotNull null

                        Comment(
                            id = document.id,
                            songId = document.getString(SONG_ID_FIELD) ?: songId,
                            userId = document.getString(USER_ID_FIELD).orEmpty(),
                            userName = document.getString(USER_NAME_FIELD).orEmpty(),
                            userAvatarUrl = document.getString(USER_AVATAR_URL_FIELD),
                            content = document.getString(CONTENT_FIELD).orEmpty(),
                            timestampMs = timestampMs,
                            createdAtMillis = document.getTimestamp(CREATED_AT_FIELD)
                                ?.toDate()
                                ?.time
                                ?: document.getLong(CREATED_AT_MILLIS_FIELD)
                                ?: 0L
                        )
                    }
                    .sortedWith(
                        compareBy<Comment> { it.timestampMs }
                            .thenBy { it.createdAtMillis }
                    )

                trySend(comments)
            }

        awaitClose { registration.remove() }
    }

    suspend fun addComment(
        songId: String,
        content: String,
        timestampMs: Long
    ) {
        val currentUser = firebaseAuth.currentUser
            ?: throw IllegalStateException("Người dùng chưa đăng nhập")
        val userSnapshot = firestore
            .collection(USERS_COLLECTION)
            .document(currentUser.uid)
            .get()
            .await()
        val userName = userSnapshot.getString(USER_NAME_FIELD)
            ?.trim()
            .orEmpty()
        val userAvatarUrl = userSnapshot.getString(USER_AVATAR_URL_FIELD).orEmpty()

        require(userName.isNotBlank()) {
            "Hãy cập nhật tên hiển thị trước khi bình luận"
        }

        firestore
            .collection(SONGS_COLLECTION)
            .document(songId)
            .collection(COMMENTS_COLLECTION)
            .add(
                mapOf(
                    SONG_ID_FIELD to songId,
                    USER_ID_FIELD to currentUser.uid,
                    USER_NAME_FIELD to userName,
                    USER_AVATAR_URL_FIELD to userAvatarUrl,
                    CONTENT_FIELD to content,
                    TIMESTAMP_MS_FIELD to timestampMs,
                    CREATED_AT_FIELD to FieldValue.serverTimestamp()
                )
            )
            .await()
    }

    private companion object {
        const val SONGS_COLLECTION = "songs"
        const val COMMENTS_COLLECTION = "comments"
        const val USERS_COLLECTION = "users"
        const val SONG_ID_FIELD = "songId"
        const val USER_ID_FIELD = "userId"
        const val USER_NAME_FIELD = "userName"
        const val USER_AVATAR_URL_FIELD = "userAvatarUrl"
        const val CONTENT_FIELD = "content"
        const val TIMESTAMP_MS_FIELD = "timestampMs"
        const val CREATED_AT_FIELD = "createdAt"
        const val CREATED_AT_MILLIS_FIELD = "createdAtMillis"
    }
}
