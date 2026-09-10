package com.devpro.sound.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.devpro.sound.data.model.Song
import com.devpro.sound.databinding.FragmentSearchBinding
import com.devpro.sound.ui.components.SongAdapter
import com.devpro.sound.ui.nowplaying.NowPlayingViewModel

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel by lazy {
        ViewModelProvider(requireActivity(), NowPlayingViewModel.Factory.create(requireContext()))[NowPlayingViewModel::class.java]
    }
    private lateinit var adapter: SongAdapter
    private var allSongs: List<Song> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SongAdapter(viewModel::onSongClick)
        binding.searchList.layoutManager = LinearLayoutManager(requireContext())
        binding.searchList.adapter = adapter
        binding.searchBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { renderResults(s?.toString().orEmpty()) }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            allSongs = state.songs
            renderResults(binding.searchInput.text?.toString().orEmpty())
        }
    }

    private fun renderResults(query: String) {
        adapter.submitList(if (query.isBlank()) allSongs.take(10) else allSongs.filter {
            it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
