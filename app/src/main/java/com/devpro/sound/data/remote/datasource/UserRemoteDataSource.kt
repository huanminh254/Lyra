package com.devpro.sound.data.remote.datasource

import android.net.Uri
import com.devpro.sound.data.remote.model.UserEntity
import com.devpro.sound.data.remote.storage.SupabaseStorageClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class UserRemoteDataSource(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val supabaseStorageClient: SupabaseStorageClient
) {
    suspend fun getCurrentUser(): UserEntity {
        return getUser(requireUserId())
    }

    suspend fun getUser(userId: String): UserEntity {
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
        updateFavoriteSong(songId, add = true)
    }

    suspend fun removeFavoriteSong(songId: String) {
        updateFavoriteSong(songId, add = false)
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

    suspend fun updateName(name: String) {
        val normalizedName = name.trim()
        require(normalizedName.isNotBlank()) { "Tên hiển thị không được để trống" }

        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("Người dùng chưa đăng nhập")

        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(normalizedName)
                .build()
        ).await()

        firestore
            .collection(USERS_COLLECTION)
            .document(user.uid)
            .set(mapOf("name" to normalizedName), SetOptions.merge())
            .await()
    }

    private suspend fun updateFavoriteSong(songId: String, add: Boolean) {
        val userId = requireUserId()
        val userReference = firestore
            .collection(USERS_COLLECTION)
            .document(userId)
        val songReference = firestore
            .collection(SONGS_COLLECTION)
            .document(songId)

        firestore.runTransaction { transaction ->
            val userSnapshot = transaction.get(userReference)
            val songSnapshot = transaction.get(songReference)
            val favoriteSongIds = (userSnapshot.get(FAVORITE_SONG_IDS_FIELD) as? List<*>)
                .orEmpty()
                .filterIsInstance<String>()
            val isFavorite = songId in favoriteSongIds

            if (add && !isFavorite) {
                transaction.set(
                    userReference,
                    mapOf(FAVORITE_SONG_IDS_FIELD to FieldValue.arrayUnion(songId)),
                    SetOptions.merge()
                )
                val favoriteCount = songSnapshot.getLong(FAVORITE_COUNT_FIELD) ?: 0L
                transaction.update(
                    songReference,
                    FAVORITE_COUNT_FIELD,
                    favoriteCount + 1L
                )
            } else if (!add && isFavorite) {
                transaction.set(
                    userReference,
                    mapOf(FAVORITE_SONG_IDS_FIELD to FieldValue.arrayRemove(songId)),
                    SetOptions.merge()
                )
                val favoriteCount = songSnapshot.getLong(FAVORITE_COUNT_FIELD) ?: 0L
                transaction.update(
                    songReference,
                    FAVORITE_COUNT_FIELD,
                    (favoriteCount - 1L).coerceAtLeast(0L)
                )
            }
        }.await()
    }

    private fun requireUserId(): String {
        return firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("Người dùng chưa đăng nhập")
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val SONGS_COLLECTION = "songs"
        const val FAVORITE_SONG_IDS_FIELD = "favoriteSongIds"
        const val FAVORITE_COUNT_FIELD = "favoriteCount"
    }
}
