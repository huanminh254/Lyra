package com.devpro.sound.ui.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devpro.sound.data.model.Song
import com.devpro.sound.databinding.ItemSongBinding

class SongAdapter(
    private val onSongClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    private val songs = mutableListOf<Song>()

    fun submitList(items: List<Song>) {
        songs.clear()
        songs.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount(): Int = songs.size

    inner class SongViewHolder(
        private val binding: ItemSongBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            binding.songTitle.text = song.title
            binding.songArtist.text = song.artist
            binding.songCover.loadSongCover(song.coverUrl)
            binding.root.setOnClickListener { onSongClick(song) }
            binding.songAction.setOnClickListener { onSongClick(song) }
        }
    }
}
