package com.devpro.sound.ui.components

import android.widget.ImageView
import coil3.load
import com.devpro.sound.R

fun ImageView.loadSongCover(coverUrl: String?) {
    setImageResource(R.drawable.ic_launcher_background)
    load(coverUrl?.takeIf { it.isNotBlank() })
}
