package com.devpro.sound.data.remote.datasource

import com.devpro.sound.data.audio.AudioWaveformExtractor
import com.devpro.sound.data.remote.model.SongEntity
import com.devpro.sound.data.remote.model.UploadSongRequest
import com.devpro.sound.data.remote.storage.SupabaseStorageClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class SongUploadRemoteDataSource(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val audioWaveformExtractor: AudioWaveformExtractor,
    private val supabaseStorageClient: SupabaseStorageClient
) {
    suspend fun uploadSong(request: UploadSongRequest): SongEntity {
        val ownerId = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("Người dùng chưa đăng nhập")

        val waveform = audioWaveformExtractor.extract(request.audioUri)

        val songReference = firestore.collection(SONGS_COLLECTION).document()
        val songId = songReference.id
        val audioUrl = supabaseStorageClient.uploadAudio(
            ownerId = ownerId,
            songId = songId,
            uri = request.audioUri
        )

        val coverUrl = request.coverUri?.let { coverUri ->
            supabaseStorageClient.uploadCover(
                ownerId = ownerId,
                songId = songId,
                uri = coverUri
            )
        }.orEmpty()

        val song = SongEntity(
            id = songId,
            title = request.title,
            artist = request.artist,
            audioUrl = audioUrl,
            coverUrl = coverUrl,
            ownerId = ownerId,
            viewCount = 0L,
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
