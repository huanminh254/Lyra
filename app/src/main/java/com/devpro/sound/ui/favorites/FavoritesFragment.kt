package com.devpro.sound.ui.favorites

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.devpro.sound.R
import com.devpro.sound.databinding.FragmentFavoritesBinding
import com.devpro.sound.ui.components.SongAdapter
import com.devpro.sound.ui.nowplaying.NowPlayingViewModel
import com.devpro.sound.ui.nowplaying.NowPlayingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NowPlayingViewModel by activityViewModels()
    private lateinit var adapter: SongAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.favoritesRefresh.setIndicatorColor(Color.BLACK)
        binding.favoritesRefresh.setOnPullToRefreshListener(viewModel::refreshSongs)
        adapter = SongAdapter { song ->
            viewModel.onSongClick(song)
            parentFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, NowPlayingFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.favoritesList.layoutManager = LinearLayoutManager(requireContext())
        binding.favoritesList.adapter = adapter
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            val songs = state.favoriteSongs.take(18)
            adapter.submitList(songs)
            binding.favoritesCount.text = resources.getQuantityString(
                R.plurals.favorite_track_count,
                songs.size,
                songs.size
            )
            if (!state.isRefreshing) binding.favoritesRefresh.finishRefresh()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
