package com.devpro.sound.ui.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.devpro.sound.MainActivity
import com.devpro.sound.R
import com.devpro.sound.databinding.FragmentDiscoverBinding
import com.devpro.sound.ui.components.MiniPlayerBinder
import com.devpro.sound.ui.components.SongAdapter
import com.devpro.sound.ui.nowplaying.NowPlayingViewModel

class DiscoverFragment : Fragment() {
    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!
    private val viewModel by lazy {
        ViewModelProvider(requireActivity(), NowPlayingViewModel.Factory.create(requireContext()))[NowPlayingViewModel::class.java]
    }
    private lateinit var adapter: SongAdapter
    private lateinit var miniPlayer: MiniPlayerBinder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SongAdapter(viewModel::onSongClick)
        binding.discoverList.layoutManager = LinearLayoutManager(requireContext())
        binding.discoverList.adapter = adapter
        binding.discoverSearch.setOnClickListener { (activity as? MainActivity)?.openSearch() }
        miniPlayer = MiniPlayerBinder(
            binding.root.findViewById(R.id.mini_player),
            onOpen = { (activity as? MainActivity)?.openNowPlaying() },
            onPlayPause = viewModel::onPlayPauseClick
        )
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.songs.take(12))
            binding.discoverLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            binding.discoverError.visibility = if (state.errorMessage != null || (!state.isLoading && state.songs.isEmpty())) View.VISIBLE else View.GONE
            binding.discoverError.text = state.errorMessage ?: if (state.songs.isEmpty()) "Chưa có bài hát" else ""
            miniPlayer.render(state.song, state.isPlaying)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
