package com.devpro.sound.ui.components

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.devpro.sound.R
import com.devpro.sound.data.model.Song

class MiniPlayerBinder(
    private val root: View,
    private val onOpen: () -> Unit,
    private val onPlayPause: () -> Unit
) {
    private val cover: android.widget.ImageView = root.findViewById(R.id.mini_cover)
    private val title: TextView = root.findViewById(R.id.mini_title)
    private val artist: TextView = root.findViewById(R.id.mini_artist)
    private val playPause: ImageButton = root.findViewById(R.id.mini_play_pause)

    init {
        root.setOnClickListener { onOpen() }
        playPause.setOnClickListener { onPlayPause() }
    }

    fun render(song: Song?, isPlaying: Boolean) {
        root.visibility = if (song == null) View.GONE else View.VISIBLE
        song ?: return
        title.text = song.title
        artist.text = song.artist
        cover.loadSongCover(song.coverUrl)
        playPause.setImageResource(if (isPlaying) R.drawable.pause else R.drawable.resume)
    }
}
