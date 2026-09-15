package com.devpro.sound.ui.downloads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.devpro.sound.data.remote.model.UploadSongRequest
import com.devpro.sound.databinding.FragmentUploadSongBinding
import com.devpro.sound.ui.nowplaying.NowPlayingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadSongFragment : Fragment() {
    private var _binding: FragmentUploadSongBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UploadSongViewModel by viewModels()
    private val nowPlayingViewModel: NowPlayingViewModel by activityViewModels()
    private var audioUri: android.net.Uri? = null
    private var coverUri: android.net.Uri? = null

    private val audioPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        audioUri = uri
        binding.uploadAudioName.text = uri?.lastPathSegment ?: "Đã chọn file nhạc"
    }

    private val coverPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        coverUri = uri
        binding.uploadCoverName.text = uri?.lastPathSegment ?: "Đã chọn ảnh bìa"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadSongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.uploadBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.uploadAudio.setOnClickListener {
            audioPicker.launch(arrayOf("audio/*"))
        }
        binding.uploadCover.setOnClickListener {
            coverPicker.launch(arrayOf("image/*"))
        }
        binding.uploadSubmit.setOnClickListener {
            submitUpload()
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.uploadSubmit.isEnabled = !state.isLoading
            binding.uploadProgress.visibility = if (state.isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }

            state.message?.takeIf { it.isNotBlank() }?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }

            if (state.isSuccess) {
                nowPlayingViewModel.refreshSongs()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun submitUpload() {
        val title = binding.uploadTitle.text?.toString()?.trim().orEmpty()
        val artist = binding.uploadArtist.text?.toString()?.trim().orEmpty()

        binding.uploadTitleLayout.error = null
        binding.uploadArtistLayout.error = null

        if (title.isBlank()) {
            binding.uploadTitleLayout.error = "Vui lòng nhập tên bài hát"
            return
        }
        if (artist.isBlank()) {
            binding.uploadArtistLayout.error = "Vui lòng nhập tên nghệ sĩ"
            return
        }
        val selectedAudioUri = audioUri
        if (selectedAudioUri == null) {
            Toast.makeText(
                requireContext(),
                "Vui lòng chọn file nhạc",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        viewModel.upload(
            UploadSongRequest(
                title = title,
                artist = artist,
                audioUri = selectedAudioUri,
                coverUri = coverUri
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
