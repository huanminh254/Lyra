package com.devpro.sound.data.remote.datasource

import com.devpro.sound.data.remote.model.UserEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getCurrentUser(userId: String = DEFAULT_USER_ID): UserEntity {
        return firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .get()
            .await()
            .toObject(UserEntity::class.java)
            ?.copy(id = userId)
            ?: UserEntity(id = userId)
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val DEFAULT_USER_ID = "user_001"
    }
}
