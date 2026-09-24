package com.devpro.sound.ui.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.devpro.sound.R
import com.devpro.sound.data.remote.model.UserEntity
import com.devpro.sound.databinding.ItemAccountUserBinding

class AccountUserAdapter(
    private val onUserClick: (UserEntity) -> Unit
) : ListAdapter<UserEntity, AccountUserAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAccountUserBinding.inflate(
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
        private val binding: ItemAccountUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserEntity) {
            binding.accountUserAvatar.setImageResource(R.drawable.account)
            binding.accountUserAvatar.load(user.avatarUrl.takeIf { it.isNotBlank() })
            binding.accountUserName.text = user.name.ifBlank { "Người dùng" }
            binding.accountUserSubtitle.text = user.accountSubtitle.ifBlank {
                "@${user.id.take(8)}"
            }
            binding.root.setOnClickListener { onUserClick(user) }
        }
    }

    private companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UserEntity>() {
            override fun areItemsTheSame(oldItem: UserEntity, newItem: UserEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: UserEntity, newItem: UserEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
