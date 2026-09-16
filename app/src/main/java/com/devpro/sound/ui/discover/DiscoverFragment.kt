package com.devpro.sound.ui.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.devpro.sound.R
import com.devpro.sound.databinding.FragmentDiscoverBinding
import com.devpro.sound.ui.components.FeaturedSongAdapter
import com.devpro.sound.ui.components.PopularSongAdapter
import com.devpro.sound.ui.nowplaying.NowPlayingFragment
import com.devpro.sound.ui.nowplaying.NowPlayingViewModel
import com.devpro.sound.ui.search.SearchFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiscoverFragment : Fragment() {
    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DiscoverViewModel by viewModels()
    private val nowPlayingViewModel: NowPlayingViewModel by activityViewModels()
    private lateinit var featuredSongAdapter: FeaturedSongAdapter
    private lateinit var popularSongAdapter: PopularSongAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.discoverSearch.setOnClickListener {
            openFragment(SearchFragment())
        }
        binding.discoverPopularList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.discoverList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        featuredSongAdapter = FeaturedSongAdapter(
            onItemClick = {song ->
                nowPlayingViewModel.onSongClick(song)
                openFragment(NowPlayingFragment())
            },
            onPlayClick = {song ->
                nowPlayingViewModel.onSongPlayClick(song)
            }
        )
        popularSongAdapter = PopularSongAdapter(
            onItemClick = { song ->
                nowPlayingViewModel.onSongClick(song)
                openFragment(NowPlayingFragment())
            },
            onPlayClick = { song ->
                nowPlayingViewModel.onSongPlayClick(song)
            }
        )
        viewModel.uiState.observe(viewLifecycleOwner){state->
            featuredSongAdapter.submitList(state.songs)
            popularSongAdapter.submitList(state.songs.take(5))
            binding.discoverLoading.visibility = if(state.isLoading) View.VISIBLE else View.GONE
            binding.discoverError.visibility = if(state.errorMessenger != null) View.VISIBLE else View.GONE
        }
        nowPlayingViewModel.uiState.observe(viewLifecycleOwner) { state ->
            featuredSongAdapter.updatePlaybackState(state.song?.id, state.isPlaying)
            popularSongAdapter.updatePlaybackState(state.song?.id, state.isPlaying)
            state.song?.let { song ->
                featuredSongAdapter.updateViewCount(song.id, song.viewCount)
                popularSongAdapter.updateViewCount(song.id, song.viewCount)
            }
        }
        binding.discoverPopularList.adapter = popularSongAdapter
        binding.discoverList.adapter = featuredSongAdapter
    }
    private fun openFragment(fragment: Fragment){
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
