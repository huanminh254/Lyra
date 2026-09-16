package com.devpro.sound.ui.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.devpro.sound.R
import com.devpro.sound.data.model.Song
import com.devpro.sound.databinding.ItemFeaturedSongBinding

class PopularSongAdapter(
    private val onItemClick: (Song) -> Unit,
    private val onPlayClick: (Song) -> Unit
): RecyclerView.Adapter<PopularSongAdapter.PopularViewHolder>() {
    private val songs = mutableListOf<Song>()
    private var currentSongId: String? = null
    private var isPlaying = false

    fun submitList(newSongs: List<Song>) {
        val oldSongs = songs.toList()
        songs.clear()
        songs.addAll(newSongs)

        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldSongs.size

            override fun getNewListSize(): Int = songs.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldSongs[oldItemPosition].id == songs[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldSongs[oldItemPosition] == songs[newItemPosition]
            }
        }).dispatchUpdatesTo(this)
    }

    fun updatePlaybackState(songId: String?, playing: Boolean) {
        val oldSongId = currentSongId
        val oldPlaying = isPlaying
        currentSongId = songId
        isPlaying = playing

        if (oldSongId == currentSongId && oldPlaying == isPlaying) return

        oldSongId?.let { id ->
            songs.indexOfFirst { it.id == id }
                .takeIf { it != -1 }
                ?.let(::notifyItemChanged)
        }
        currentSongId?.let { id ->
            songs.indexOfFirst { it.id == id }
                .takeIf { it != -1 }
                ?.let(::notifyItemChanged)
        }
    }

    fun updateViewCount(songId: String?, viewCount: Long) {
        val index = songs.indexOfFirst { it.id == songId }
        if (index == -1 || songs[index].viewCount == viewCount) return
        songs[index] = songs[index].copy(viewCount = viewCount)
        notifyItemChanged(index)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PopularViewHolder {
        val binding = ItemFeaturedSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PopularViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: PopularViewHolder,
        position: Int
    ) {
        holder.bind(songs[position])
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    inner class PopularViewHolder(
        private val binding: ItemFeaturedSongBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            binding.featuredTitle.text = song.title
            binding.featuredCover.loadSongCover(song.coverUrl)
            binding.featuredPlayCount.text = "▷\n${song.viewCount}"
            binding.featuredDownloadCount.text = "⇩\n243"
            binding.featuredFavoriteCount.text = "♡\n193"
            binding.featuredPlay.setImageResource(
                if (currentSongId == song.id && isPlaying) R.drawable.pause else R.drawable.resume
            )

            binding.featuredPlay.setOnClickListener {
                onPlayClick(song)
            }
            binding.root.setOnClickListener {
                onItemClick(song)
            }
        }
    }
}
