package com.devpro.sound.data.remote.datasource

import com.devpro.sound.data.audio.AudioWaveformExtractor
import com.devpro.sound.data.remote.model.SongEntity
import com.devpro.sound.data.remote.model.UploadSongRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class SongUploadRemoteDataSource(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val firebaseAuth: FirebaseAuth,
    private val audioWaveformExtractor: AudioWaveformExtractor
) {
    suspend fun uploadSong(request: UploadSongRequest): SongEntity {
        val ownerId = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("Người dùng chưa đăng nhập")

        val waveform = audioWaveformExtractor.extract(request.audioUri)

        val songReference = firestore.collection(SONGS_COLLECTION).document()
        val songId = songReference.id
        val songStoragePath = "users/$ownerId/songs/$songId"

        val audioReference = storage.reference.child("$songStoragePath/audio")
        audioReference.putFile(request.audioUri).await()
        val audioUrl = audioReference.downloadUrl.await().toString()

        val coverUrl = request.coverUri?.let { coverUri ->
            val coverReference = storage.reference.child("$songStoragePath/cover")
            coverReference.putFile(coverUri).await()
            coverReference.downloadUrl.await().toString()
        }.orEmpty()

        val song = SongEntity(
            id = songId,
            title = request.title,
            artist = request.artist,
            audioUrl = audioUrl,
            coverUrl = coverUrl,
            ownerId = ownerId,
            waveform = waveform.map(Float::toDouble)
        )

        songReference.set(song).await()

        firestore
            .collection(USERS_COLLECTION)
            .document(ownerId)
            .set(
                mapOf("uploadedSongIds" to FieldValue.arrayUnion(songId)),
                SetOptions.merge()
            )
            .await()

        return song
    }

    private companion object {
        const val SONGS_COLLECTION = "songs"
        const val USERS_COLLECTION = "users"
    }
}
