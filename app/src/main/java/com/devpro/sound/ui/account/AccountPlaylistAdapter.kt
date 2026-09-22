package com.devpro.sound.ui.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devpro.sound.databinding.ItemAccountPlaylistBinding
import com.devpro.sound.ui.components.loadSongCover

class AccountPlaylistAdapter : ListAdapter<AccountPlaylistUiModel, AccountPlaylistAdapter.ViewHolder>(
    DIFF_CALLBACK
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAccountPlaylistBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemAccountPlaylistBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(playlist: AccountPlaylistUiModel) {
            binding.playlistTitle.text = playlist.title
            binding.playlistOwner.text = playlist.ownerName
            binding.playlistCover.loadSongCover(playlist.coverSong?.coverUrl)
        }
    }

    private companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AccountPlaylistUiModel>() {
            override fun areItemsTheSame(
                oldItem: AccountPlaylistUiModel,
                newItem: AccountPlaylistUiModel
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: AccountPlaylistUiModel,
                newItem: AccountPlaylistUiModel
            ): Boolean = oldItem == newItem
        }
    }
}
