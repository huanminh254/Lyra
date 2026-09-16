package com.devpro.sound.data.remote.datasource

import com.devpro.sound.data.remote.model.SongEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SongRemoteDataSource(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun getSongs(): List<SongEntity> {
        return firestore
            .collection(SONGS_COLLECTION)
            .orderBy(SORT_ORDER_FIELD)
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(SongEntity::class.java)?.copy(id = document.id)
            }
    }

    suspend fun recordView(songId: String): Boolean {
        val userId = firebaseAuth.currentUser?.uid ?: return false
        val songReference = firestore
            .collection(SONGS_COLLECTION)
            .document(songId)
        val viewerReference = songReference
            .collection(VIEWERS_COLLECTION)
            .document(userId)

        return firestore.runTransaction { transaction ->
            val viewerSnapshot = transaction.get(viewerReference)
            if (viewerSnapshot.exists()) {
                false
            } else {
                transaction.set(
                    viewerReference,
                    mapOf(
                        "userId" to userId,
                        "viewedAt" to FieldValue.serverTimestamp()
                    )
                )
                transaction.update(
                    songReference,
                    VIEW_COUNT_FIELD,
                    FieldValue.increment(1)
                )
                true
            }
        }.await()
    }

    private companion object {
        const val SONGS_COLLECTION = "songs"
        const val SORT_ORDER_FIELD = "sortOrder"
        const val VIEWERS_COLLECTION = "viewers"
        const val VIEW_COUNT_FIELD = "viewCount"
    }
}
