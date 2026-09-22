package com.devpro.sound.ui.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devpro.sound.R
import com.devpro.sound.data.model.Song
import com.devpro.sound.databinding.ItemAccountSongBinding
import com.devpro.sound.ui.components.loadSongCover

class AccountSongAdapter(
    private val onSongClick: (Song) -> Unit
) : ListAdapter<Song, AccountSongAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAccountSongBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemAccountSongBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            binding.accountSongCover.loadSongCover(song.coverUrl)
            binding.accountSongTitle.text = song.title
            binding.accountSongArtist.text = song.artist
            binding.accountSongMeta.text = binding.root.context.getString(
                R.string.account_song_meta,
                song.viewCount,
                song.duration
            )
            binding.root.setOnClickListener { onSongClick(song) }
            binding.accountSongAction.setOnClickListener { onSongClick(song) }
        }
    }

    private companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Song>() {
            override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
                return oldItem == newItem
            }
        }
    }
}
