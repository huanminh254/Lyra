package com.devpro.sound.ui.downloads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.devpro.sound.R
import com.devpro.sound.databinding.FragmentDownloadsBinding
import com.devpro.sound.ui.components.SongAdapter
import com.devpro.sound.ui.nowplaying.NowPlayingFragment
import com.devpro.sound.ui.nowplaying.NowPlayingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadsFragment : Fragment() {
    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NowPlayingViewModel by activityViewModels()
    private lateinit var adapter: SongAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDownloadsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = SongAdapter { song ->
            viewModel.onSongClick(song)
            parentFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, NowPlayingFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.downloadsList.layoutManager = LinearLayoutManager(requireContext())
        binding.downloadsList.adapter = adapter
        binding.downloadsUpload.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, UploadSongFragment())
                .addToBackStack(null)
                .commit()
        }
        adapter.submitList(emptyList())
        binding.downloadsCount.text = getString(R.string.downloads_not_ready)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
