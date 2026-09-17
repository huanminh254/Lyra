package com.devpro.sound.ui.components
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.devpro.sound.R
import com.devpro.sound.data.model.Song
import com.devpro.sound.databinding.ItemFeaturedSongBinding

class FeaturedSongAdapter(
    private val onItemClick: (Song) -> Unit,
    private val onPlayClick: (Song) -> Unit
): RecyclerView.Adapter<FeaturedSongAdapter.FeaturedViewHolder>(){
    private val songs = mutableListOf<Song>()
    private var currentSongId: String? = null
    private var isPlaying = false

    fun submitList(newListSongs: List<Song>){
        val oldSongs = songs.toList()
        songs.clear()
        songs.addAll(newListSongs)
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
    ): FeaturedViewHolder {
        val binding = ItemFeaturedSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FeaturedViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FeaturedViewHolder,
        position: Int
    ) {
        holder.bind(songs[position])
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    inner class FeaturedViewHolder(
       private val binding: ItemFeaturedSongBinding
    ) : RecyclerView.ViewHolder(binding.root){
        fun bind(song: Song) {
            binding.featuredTitle.text = song.title
            binding.featuredCover.loadSongCover(song.coverUrl)
            binding.featuredPlayCount.text = binding.root.context.getString(
                R.string.view_count_format,
                song.viewCount
            )
            binding.featuredDownloadCount.text = binding.root.context.getString(
                R.string.download_count_format
            )
            binding.featuredFavoriteCount.text = binding.root.context.getString(
                R.string.favorite_count_format_static
            )
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
