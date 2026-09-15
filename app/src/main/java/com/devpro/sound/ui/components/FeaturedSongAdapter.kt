package com.devpro.sound.ui.components
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devpro.sound.R
import com.devpro.sound.data.model.Song
import com.devpro.sound.databinding.ItemFeaturedSongBinding

class FeaturedSongAdapter(
    private val onItemClick: (Song) -> Unit,
    private val onPlayClick: (Song) -> Unit
): RecyclerView.Adapter<FeaturedSongAdapter.FeaturedViewHolder>(){
    private val songs = mutableListOf<Song>()
    private var playingSongId: String? = null

    fun submitList(newListSongs: List<Song>){
        songs.clear()
        songs.addAll(newListSongs)
        notifyDataSetChanged()
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
            binding.featuredPlayCount.text = "▷\n243"
            binding.featuredDownloadCount.text = "⇩\n243"
            binding.featuredFavoriteCount.text = "♡\n193"
            binding.featuredPlay.setImageResource(
                if (playingSongId == song.id) R.drawable.pause else R.drawable.resume
            )

            binding.featuredPlay.setOnClickListener {
                changePlay(song)
                onPlayClick(song)
            }
            binding.root.setOnClickListener {
                onItemClick(song)
            }
        }

        private fun changePlay(song: Song) {
            val oldPlayingSongId = playingSongId
            playingSongId = if (oldPlayingSongId == song.id) null else song.id

            oldPlayingSongId?.let { oldId ->
                val oldPosition = songs.indexOfFirst { it.id == oldId }
                if (oldPosition != -1) {
                    notifyItemChanged(oldPosition)
                }
            }

            val newPosition = songs.indexOfFirst { it.id == song.id }
            if (newPosition != -1) {
                notifyItemChanged(newPosition)
            }
        }
    }
}
