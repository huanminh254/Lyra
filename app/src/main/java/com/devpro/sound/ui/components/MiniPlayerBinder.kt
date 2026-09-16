package com.devpro.sound.ui.components

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.devpro.sound.R
import com.devpro.sound.data.model.Song
import kotlin.math.roundToInt

class MiniPlayerBinder(
    private val root: View,
    private val onOpen: () -> Unit,
    private val onPlayPause: () -> Unit,
    private val onFavorite: () -> Unit
) {
    private val title: TextView = root.findViewById(R.id.mini_title)
    private val artist: TextView = root.findViewById(R.id.mini_artist)
    private val playPause: ImageButton = root.findViewById(R.id.mini_play_pause)
    private val favorMini: ImageButton = root.findViewById(R.id.favorites_mini)
    private val progressIndicator: CircularProgressIndicator =
        root.findViewById(R.id.mini_progress)

    init {
        root.setOnClickListener { onOpen() }
        playPause.setOnClickListener { onPlayPause() }
        favorMini.setOnClickListener { onFavorite() }
    }

    fun render(
        song: Song?,
        isPlaying: Boolean,
        isFavorite: Boolean,
        progress: Float
    ) {
        root.visibility = if (song == null) View.GONE else View.VISIBLE
        song ?: return
        title.text = song.title
        artist.text = song.artist
        playPause.setImageResource(if (isPlaying) R.drawable.pause else R.drawable.resume)
        progressIndicator.progress = (progress.coerceIn(0f, 1f) * 1000).roundToInt()
        favorMini.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                root.context,
                if (isFavorite) R.color.favorite_red else R.color.white
            )
        )
    }
}
