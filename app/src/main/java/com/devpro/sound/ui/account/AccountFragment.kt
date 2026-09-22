package com.devpro.sound.ui.account

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.devpro.sound.MainActivity
import com.devpro.sound.R
import com.devpro.sound.data.model.Song
import com.devpro.sound.databinding.FragmentAccountBinding
import com.devpro.sound.ui.favorites.FavoritesFragment
import com.devpro.sound.ui.nowplaying.NowPlayingFragment
import com.devpro.sound.ui.nowplaying.NowPlayingViewModel
import com.google.firebase.auth.FirebaseAuth
import coil3.load
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccountFragment : Fragment() {
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AccountViewModel by viewModels()
    private val nowPlayingViewModel: NowPlayingViewModel by activityViewModels()
    private lateinit var playlistAdapter: AccountPlaylistAdapter
    private lateinit var likedSongAdapter: AccountSongAdapter
    private var currentAvatarUrl = ""
    private val avatarPicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let(viewModel::updateAvatar)
    }

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.accountRefresh.setIndicatorColor(Color.BLACK)
        binding.accountRefresh.setOnPullToRefreshListener(viewModel::refresh)
        playlistAdapter = AccountPlaylistAdapter()
        likedSongAdapter = AccountSongAdapter { song ->
            nowPlayingViewModel.onSongClick(song)
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, NowPlayingFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.accountPlaylists.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.accountPlaylists.adapter = playlistAdapter
        binding.accountLikes.layoutManager = LinearLayoutManager(requireContext())
        binding.accountLikes.adapter = likedSongAdapter

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            currentAvatarUrl = state.avatarUrl
            binding.accountHeaderName.text = state.displayName
            binding.accountName.text = state.displayName
            binding.accountAvatar.loadAccountAvatar(state.avatarUrl)
            binding.accountHeaderAvatar.loadAccountAvatar(state.avatarUrl)
            binding.accountEmail.text = state.email.ifBlank {
                firebaseAuth.currentUser?.email.orEmpty()
            }
            binding.accountFollowingCount.text = state.followingCount.toString()
            binding.accountFollowersCount.text = state.followersCount.toString()
            binding.accountLikesCount.text = state.likedSongs.size.toString()

            playlistAdapter.submitList(state.playlists)
            likedSongAdapter.submitList(state.likedSongs)
            binding.accountPlaylistsEmpty.visibility = if (state.playlists.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.accountLikesEmpty.visibility = if (state.likedSongs.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }

            state.errorMessage?.let { message ->
                binding.accountPlaylistsEmpty.text = message
                binding.accountLikesEmpty.text = message
            }

            val firstPlayableSong = state.likedSongs.firstOrNull()
                ?: state.playlists.firstOrNull()?.coverSong
            binding.accountPlay.isEnabled = firstPlayableSong != null
            binding.accountPlay.alpha = if (firstPlayableSong != null) 1f else 0.5f
            binding.accountPlay.setOnClickListener {
                firstPlayableSong?.let(::openNowPlaying)
            }

            val avatarEnabled = !state.isUploadingAvatar
            binding.accountAvatar.isEnabled = avatarEnabled
            binding.accountHeaderAvatar.isEnabled = avatarEnabled
            binding.accountAvatar.alpha = if (avatarEnabled) 1f else 0.5f
            binding.accountHeaderAvatar.alpha = if (avatarEnabled) 1f else 0.5f
            binding.accountAvatarAdd.isEnabled = avatarEnabled
            binding.accountAvatarAdd.alpha = if (avatarEnabled) 1f else 0.5f

            state.avatarErrorMessage?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }

            if (!state.isRefreshing) binding.accountRefresh.finishRefresh()
        }

        binding.accountAvatar.setOnClickListener {
            showAvatarPreview()
        }
        binding.accountHeaderAvatar.setOnClickListener {
            showAvatarPreview()
        }
        binding.accountAvatarAdd.setOnClickListener {
            avatarPicker.launch("image/*")
        }
        binding.accountBack.setOnClickListener {
            (activity as? MainActivity)?.navigateToDiscover()
        }
        binding.accountLikesSeeAll.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, FavoritesFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.accountLogout.setOnClickListener {
            firebaseAuth.signOut()
        }
    }

    private fun openNowPlaying(song: Song) {
        nowPlayingViewModel.onSongPlayClick(song)
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, NowPlayingFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun ImageView.loadAccountAvatar(avatarUrl: String) {
        setImageResource(R.drawable.account)
        load(avatarUrl.takeIf { it.isNotBlank() })
    }

    private fun showAvatarPreview() {
        val dialog = Dialog(requireContext())
        val container = FrameLayout(requireContext()).apply {
            setBackgroundColor(Color.BLACK)
        }
        val preview = ImageView(requireContext()).apply {
            setImageResource(R.drawable.account)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(dp(24), dp(24), dp(24), dp(24))
            load(currentAvatarUrl.takeIf { it.isNotBlank() })
            setOnClickListener { dialog.dismiss() }
        }
        container.addView(
            preview,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val closeButton = ImageButton(requireContext()).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(Color.WHITE)
            contentDescription = getString(R.string.close)
            setOnClickListener { dialog.dismiss() }
        }
        container.addView(
            closeButton,
            FrameLayout.LayoutParams(dp(48), dp(48), Gravity.TOP or Gravity.END).apply {
                topMargin = dp(16)
                marginEnd = dp(12)
            }
        )

        dialog.setContentView(container)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        dialog.setOnShowListener {
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        dialog.show()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
