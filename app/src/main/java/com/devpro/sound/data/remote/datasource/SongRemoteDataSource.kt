package com.devpro.sound.data.remote.datasource

import com.devpro.sound.data.remote.model.SongEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SongRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
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

    private companion object {
        const val SONGS_COLLECTION = "songs"
        const val SORT_ORDER_FIELD = "sortOrder"
    }
}
