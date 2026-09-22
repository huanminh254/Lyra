package com.devpro.sound.data.remote.datasource

import android.net.Uri
import com.devpro.sound.data.remote.model.UserEntity
import com.devpro.sound.data.remote.storage.SupabaseStorageClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class UserRemoteDataSource(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val supabaseStorageClient: SupabaseStorageClient
) {
    suspend fun getCurrentUser(): UserEntity {
        val userId = requireUserId()
        return firestore
            .collection("users")
            .document(userId)
            .get()
            .await()
            .toObject(UserEntity::class.java)
            ?.copy(id = userId)
            ?: UserEntity(id = userId)
    }

    suspend fun getFavoriteSongIds(): List<String> {
        return getCurrentUser().favoriteSongIds
    }

    suspend fun addFavoriteSong(songId: String) {
        updateSongList("favoriteSongIds", FieldValue.arrayUnion(songId))
    }

    suspend fun removeFavoriteSong(songId: String) {
        updateSongList("favoriteSongIds", FieldValue.arrayRemove(songId))
    }

    suspend fun updateAvatar(uri: Uri): String {
        val userId = requireUserId()
        val avatarUrl = supabaseStorageClient.uploadAvatar(userId, uri)
        firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .set(mapOf("avatarUrl" to avatarUrl), SetOptions.merge())
            .await()
        return avatarUrl
    }

    private suspend fun updateSongList(field: String, value: Any) {
        firestore
            .collection(USERS_COLLECTION)
            .document(requireUserId())
            .set(mapOf(field to value), SetOptions.merge())
            .await()
    }

    private fun requireUserId(): String {
        return firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("Người dùng chưa đăng nhập")
    }

    private companion object {
        const val USERS_COLLECTION = "users"
    }
}
