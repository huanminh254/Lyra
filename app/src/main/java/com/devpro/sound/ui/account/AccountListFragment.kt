package com.devpro.sound.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.devpro.sound.R
import com.devpro.sound.data.model.Song
import com.devpro.sound.databinding.FragmentAccountListBinding
import com.devpro.sound.ui.components.SongAdapter
import com.devpro.sound.ui.nowplaying.NowPlayingFragment
import com.devpro.sound.ui.nowplaying.NowPlayingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountListFragment : Fragment() {
    private var _binding: FragmentAccountListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AccountListViewModel by viewModels()
    private val nowPlayingViewModel: NowPlayingViewModel by activityViewModels()
    private lateinit var userAdapter: AccountUserAdapter
    private lateinit var songAdapter: SongAdapter

    private val profileId: String?
        get() = arguments?.getString(ARG_PROFILE_ID)

    private val mode: String
        get() = arguments?.getString(ARG_MODE) ?: MODE_LIKES

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.accountListBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.accountListRecycler.layoutManager = LinearLayoutManager(requireContext())

        if (mode == MODE_LIKES) {
            songAdapter = SongAdapter(::openSong)
            binding.accountListRecycler.adapter = songAdapter
        } else {
            userAdapter = AccountUserAdapter { user ->
                parentFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        AccountFragment.newPublicProfile(user.id)
                    )
                    .addToBackStack(null)
                    .commit()
            }
            binding.accountListRecycler.adapter = userAdapter
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.accountListTitle.text = state.title
            binding.accountListProgress.visibility = if (state.isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.accountListError.visibility = if (state.errorMessage != null) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.accountListError.text = state.errorMessage

            if (mode == MODE_LIKES) {
                songAdapter.submitList(state.songs)
                binding.accountListEmpty.visibility = if (
                    !state.isLoading && state.errorMessage == null && state.songs.isEmpty()
                ) View.VISIBLE else View.GONE
            } else {
                userAdapter.submitList(state.users)
                binding.accountListEmpty.visibility = if (
                    !state.isLoading && state.errorMessage == null && state.users.isEmpty()
                ) View.VISIBLE else View.GONE
            }
        }

        viewModel.load(profileId, mode)
    }

    private fun openSong(song: Song) {
        nowPlayingViewModel.onSongPlayClick(song)
        parentFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container, NowPlayingFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val MODE_FOLLOWING = "following"
        const val MODE_FOLLOWERS = "followers"
        const val MODE_LIKES = "likes"

        private const val ARG_PROFILE_ID = "profile_id"
        private const val ARG_MODE = "mode"

        fun newInstance(profileId: String?, mode: String): AccountListFragment {
            return AccountListFragment().apply {
                arguments = Bundle().apply {
                    profileId?.let { putString(ARG_PROFILE_ID, it) }
                    putString(ARG_MODE, mode)
                }
            }
        }
    }
}
