package com.devpro.sound.ui.nowplaying

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.devpro.sound.databinding.FragmentNowPlayingBinding
import com.devpro.sound.ui.components.loadSongCover

class NowPlayingFragment : Fragment() {
    private var _binding: FragmentNowPlayingBinding? = null
    private val binding get() = _binding!!
    private val viewModel by lazy {
        ViewModelProvider(requireActivity(), NowPlayingViewModel.Factory.create(requireContext()))[NowPlayingViewModel::class.java]
    }
    private var initialTouchX = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNowPlayingBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Suppress("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.nowPlayingBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.nowPlayingPlayPause.setOnClickListener { viewModel.onPlayPauseClick() }
        binding.nowPlayingNext.setOnClickListener { viewModel.onNextClick() }
        binding.nowPlayingPrevious.setOnClickListener { viewModel.onPreviousClick() }
        binding.nowPlayingSeek.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) viewModel.onSeek(progress / 1000f)
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) = Unit
        })
        binding.nowPlayingRoot.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    initialTouchX = event.x
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val distance = event.x - initialTouchX
                    if (distance < -120f) viewModel.onNextClick()
                    if (distance > 120f) viewModel.onPreviousClick()
                    true
                }
                else -> true
            }
        }
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.nowPlayingLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            binding.nowPlayingError.visibility = if (state.errorMessage != null) View.VISIBLE else View.GONE
            binding.nowPlayingError.text = state.errorMessage
            state.song?.let { song ->
                binding.nowPlayingTitle.text = song.title
                binding.nowPlayingArtist.text = song.artist
                binding.nowPlayingCover.loadSongCover(song.coverUrl)
            }
            binding.nowPlayingPlayPause.setImageResource(
                if (state.isPlaying) com.devpro.sound.R.drawable.pause else com.devpro.sound.R.drawable.resume
            )
            binding.nowPlayingSeek.progress = (state.progress * 1000).toInt().coerceIn(0, 1000)
            binding.nowPlayingTime.text = "${formatTime(state.currentPositionMs)} / ${formatTime(state.durationMs)}"
        }
    }

    private fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs.coerceAtLeast(0L) / 1000
        return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
