package com.devpro.sound.ui.downloads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.devpro.sound.databinding.FragmentDownloadsBinding
import com.devpro.sound.ui.components.SongAdapter
import com.devpro.sound.ui.nowplaying.NowPlayingViewModel

class DownloadsFragment : Fragment() {
    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by lazy {
        ViewModelProvider(requireActivity(), NowPlayingViewModel.Factory.create(requireContext()))[NowPlayingViewModel::class.java]
    }
    private lateinit var adapter: SongAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDownloadsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = SongAdapter(viewModel::onSongClick)
        binding.downloadsList.layoutManager = LinearLayoutManager(requireContext())
        binding.downloadsList.adapter = adapter
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            val songs = state.songs.take(16)
            adapter.submitList(songs)
            binding.downloadsCount.text = "${songs.size} tracks saved offline"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
