package com.devpro.sound.data.remote.datasource

import com.devpro.sound.data.audio.AudioWaveformExtractor
import com.devpro.sound.data.remote.model.SongEntity
import com.devpro.sound.data.remote.model.UploadSongRequest
import com.devpro.sound.data.remote.storage.SupabaseStorageClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
        var audioUrl: String? = null
        var coverUrl: String? = null

        return try {
            audioUrl = supabaseStorageClient.uploadAudio(
                ownerId = ownerId,
                songId = songId,
                uri = request.audioUri
            )

            coverUrl = request.coverUri?.let { coverUri ->
                supabaseStorageClient.uploadCover(
                    ownerId = ownerId,
                    songId = songId,
                    uri = coverUri
                )
            }

            val song = SongEntity(
                id = songId,
                title = request.title,
                artist = request.artist,
                audioUrl = audioUrl.orEmpty(),
                coverUrl = coverUrl.orEmpty(),
                ownerId = ownerId,
                viewCount = 0L,
                waveform = waveform.map(Float::toDouble)
            )

            songReference.set(song).await()
            song
        } catch (exception: Exception) {
            runCatching { songReference.delete().await() }
            coverUrl?.let { url ->
                runCatching { supabaseStorageClient.deleteObject(url) }
            }
            audioUrl?.let { url ->
                runCatching { supabaseStorageClient.deleteObject(url) }
            }
            throw exception
        }
    }

    private companion object {
        const val SONGS_COLLECTION = "songs"
    }
}
