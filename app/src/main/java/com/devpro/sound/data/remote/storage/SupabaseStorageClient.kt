package com.devpro.sound.data.remote.storage

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.devpro.sound.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseStorageClient @Inject constructor(
    @ApplicationContext context: Context
) {
    private val contentResolver: ContentResolver = context.contentResolver

    suspend fun uploadAudio(
        ownerId: String,
        songId: String,
        uri: Uri
    ): String = upload(
        bucket = BuildConfig.SUPABASE_AUDIO_BUCKET,
        path = "audio/$ownerId/songs/$songId.${extensionFor(uri, "mp3")}",
        uri = uri
    )

    suspend fun uploadCover(
        ownerId: String,
        songId: String,
        uri: Uri
    ): String = upload(
        bucket = BuildConfig.SUPABASE_COVER_BUCKET,
        path = "${BuildConfig.SUPABASE_COVER_PREFIX}/$ownerId/songs/$songId.${extensionFor(uri, "jpg")}",
        uri = uri
    )

    suspend fun uploadAvatar(
        ownerId: String,
        uri: Uri
    ): String {
        require(contentResolver.getType(uri)?.startsWith("image/") == true) {
            "Avatar phải là file ảnh"
        }

        return upload(
            bucket = BuildConfig.SUPABASE_COVER_BUCKET,
            path = "${BuildConfig.SUPABASE_COVER_PREFIX}/$ownerId/avatar-" +
                "${System.currentTimeMillis()}.${extensionFor(uri, "jpg")}",
            uri = uri
        )
    }

    private suspend fun upload(
        bucket: String,
        path: String,
        uri: Uri
    ): String = withContext(Dispatchers.IO) {
        require(BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) {
            "Thiếu Supabase anon key. Hãy cấu hình supabase.properties"
        }

        val input = contentResolver.openInputStream(uri)
            ?: throw IOException("Không thể đọc file đã chọn")
        val connection = (URL(buildUploadUrl(bucket, path)).openConnection() as HttpURLConnection)

        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.useCaches = false
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
            connection.setRequestProperty(
                "Authorization",
                "Bearer ${BuildConfig.SUPABASE_ANON_KEY}"
            )
            connection.setRequestProperty(
                "Content-Type",
                contentResolver.getType(uri) ?: "application/octet-stream"
            )
            connection.setRequestProperty("x-upsert", "false")

            val fileLength = contentResolver.openAssetFileDescriptor(uri, "r")?.use {
                it.length
            } ?: -1L
            if (fileLength >= 0) {
                connection.setFixedLengthStreamingMode(fileLength)
            } else {
                connection.setChunkedStreamingMode(BUFFER_SIZE)
            }

            input.use { source ->
                connection.outputStream.use { target ->
                    source.copyTo(target, BUFFER_SIZE)
                }
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val responseBody = (connection.errorStream ?: connection.inputStream)
                    .bufferedReader()
                    .use { it.readText() }
                throw IOException(
                    "Supabase upload thất bại ($responseCode): ${responseBody.take(MAX_ERROR_LENGTH)}"
                )
            }

            buildPublicUrl(bucket, path)
        } finally {
            connection.disconnect()
        }
    }

    private fun buildUploadUrl(bucket: String, path: String): String =
        "${BuildConfig.SUPABASE_URL.trimEnd('/')}/storage/v1/object/" +
            "${Uri.encode(bucket)}/${Uri.encode(path, "/")}"

    private fun buildPublicUrl(bucket: String, path: String): String =
        "${BuildConfig.SUPABASE_URL.trimEnd('/')}/storage/v1/object/public/" +
            "${Uri.encode(bucket)}/${Uri.encode(path, "/")}"

    private fun extensionFor(uri: Uri, fallback: String): String {
        val mimeType = contentResolver.getType(uri)
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?.lowercase()
            ?: fallback
    }

    private companion object {
        const val BUFFER_SIZE = 8 * 1024
        const val CONNECT_TIMEOUT_MS = 30_000
        const val READ_TIMEOUT_MS = 120_000
        const val MAX_ERROR_LENGTH = 500
    }
}
