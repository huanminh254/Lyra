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
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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
    private lateinit var publicSongAdapter: AccountPublicSongAdapter
    private var currentAvatarUrl = ""
    private val requestedProfileId: String?
        get() = arguments?.getString(ARG_PROFILE_ID)
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
            openNowPlaying(song)
        }
        publicSongAdapter = AccountPublicSongAdapter(::openNowPlaying)

        binding.accountPlaylists.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.accountPlaylists.adapter = playlistAdapter
        binding.accountLikes.layoutManager = LinearLayoutManager(requireContext())
        binding.accountLikes.adapter = likedSongAdapter
        binding.accountPublicSongs.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.accountPublicSongs.adapter = publicSongAdapter

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            currentAvatarUrl = state.avatarUrl
            binding.accountHeaderName.text = state.displayName
            binding.accountName.text = state.displayName
            binding.accountAvatar.loadAccountAvatar(state.avatarUrl)
            binding.accountHeaderAvatar.loadAccountAvatar(state.avatarUrl)
            binding.accountEmail.text = state.email.ifBlank {
                if (state.isPublicProfile) "@user" else firebaseAuth.currentUser?.email.orEmpty()
            }
            binding.accountFollowingCount.text = state.followingCount.toString()
            binding.accountFollowersCount.text = state.followersCount.toString()
            binding.accountLikesCount.text = state.likedSongs.size.toString()
            binding.accountBio.text = state.bio

            val publicVisibility = if (state.isPublicProfile) View.VISIBLE else View.GONE
            val selfVisibility = if (state.isPublicProfile) View.GONE else View.VISIBLE
            binding.accountPublicActions.visibility = publicVisibility
            binding.accountBio.visibility = if (state.isPublicProfile && state.bio.isNotBlank()) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.accountPublicTabs.visibility = publicVisibility
            binding.accountPublicSongsSection.visibility = publicVisibility
            binding.accountSelfActions.visibility = selfVisibility
            binding.accountAvatarAdd.visibility = selfVisibility
            binding.accountPlaylistsTitle.visibility = selfVisibility
            binding.accountPlaylists.visibility = selfVisibility
            binding.accountPlaylistsEmpty.visibility = selfVisibility
            binding.accountLikesHeader.visibility = selfVisibility
            binding.accountLikes.visibility = selfVisibility
            binding.accountLikesEmpty.visibility = selfVisibility
            binding.accountLogout.visibility = selfVisibility

            playlistAdapter.submitList(state.playlists)
            likedSongAdapter.submitList(state.likedSongs)
            publicSongAdapter.submitList(state.uploadedSongs)
            binding.accountPublicSongsEmpty.visibility = if (
                state.isPublicProfile && state.uploadedSongs.isEmpty()
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.accountPlaylistsEmpty.visibility = if (!state.isPublicProfile && state.playlists.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.accountLikesEmpty.visibility = if (!state.isPublicProfile && state.likedSongs.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }

            state.errorMessage?.let { message ->
                binding.accountPlaylistsEmpty.text = message
                binding.accountLikesEmpty.text = message
            }

            val firstPlayableSong = state.likedSongs.firstOrNull()
                ?: state.uploadedSongs.firstOrNull()
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
            state.nameErrorMessage?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }

            if (!state.isRefreshing) binding.accountRefresh.finishRefresh()
        }

        viewModel.loadProfile(requestedProfileId)

        binding.accountAvatar.setOnClickListener {
            showAvatarPreview()
        }
        binding.accountHeaderAvatar.setOnClickListener {
            showAvatarPreview()
        }
        binding.accountAvatarAdd.setOnClickListener {
            avatarPicker.launch("image/*")
        }
        binding.accountEdit.setOnClickListener {
            if (requestedProfileId == null) {
                showEditNameDialog(viewModel.uiState.value?.displayName.orEmpty())
            }
        }
        binding.accountBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
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
        binding.accountFollow.setOnClickListener { showFeatureMessage() }
        binding.accountMessage.setOnClickListener { showFeatureMessage() }
        binding.accountPublicAdd.setOnClickListener { showFeatureMessage() }
        binding.accountPublicUploadsTab.setOnClickListener {
            binding.accountScrollView.post {
                binding.accountScrollView.smoothScrollTo(
                    0,
                    binding.accountPublicSongsSection.top
                )
            }
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

    private fun showFeatureMessage() {
        Toast.makeText(
            requireContext(),
            getString(R.string.feature_in_development),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showEditNameDialog(currentName: String) {
        val input = EditText(requireContext()).apply {
            setSingleLine(true)
            hint = "Tên hiển thị"
            setText(currentName)
            setSelection(text.length)
            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_DONE
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Chỉnh sửa tên")
            .setView(input)
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Lưu", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val newName = input.text?.toString()?.trim().orEmpty()
                if (newName.isBlank()) {
                    input.error = "Vui lòng nhập tên hiển thị"
                    return@setOnClickListener
                }
                viewModel.updateName(newName)
                dialog.dismiss()
            }
            input.requestFocus()
            dialog.window?.setSoftInputMode(
                android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            )
        }

        dialog.show()
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

    companion object {
        private const val ARG_PROFILE_ID = "profile_id"

        fun newPublicProfile(userId: String): AccountFragment {
            return AccountFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROFILE_ID, userId)
                }
            }
        }
    }
}
