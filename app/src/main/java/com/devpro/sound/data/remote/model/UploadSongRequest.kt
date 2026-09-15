package com.devpro.sound.data.remote.model

import android.net.Uri

data class UploadSongRequest(
    val title: String,
    val artist: String,
    val audioUri: Uri,
    val coverUri: Uri? = null
)
