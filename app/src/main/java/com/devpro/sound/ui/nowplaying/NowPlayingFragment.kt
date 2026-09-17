package com.devpro.sound.ui.nowplaying

import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.devpro.sound.MainActivity
import com.devpro.sound.databinding.FragmentNowPlayingBinding
import com.devpro.sound.ui.components.loadSongCover
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.activityViewModels

@AndroidEntryPoint
class NowPlayingFragment : Fragment() {
    private var _binding: FragmentNowPlayingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NowPlayingViewModel by activityViewModels()
    private val swipeDistance = 120f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNowPlayingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    (requireActivity() as? MainActivity)?.navigateToDiscover()
                }
            }
        )

        binding.nowPlayingBack.setOnClickListener {
            (requireActivity() as? MainActivity)?.navigateToDiscover()
        }
        binding.nowPlayingMore.setOnClickListener {
            Toast.makeText(
                requireContext(),
                getString(com.devpro.sound.R.string.feature_in_development),
                Toast.LENGTH_SHORT
            ).show()
        }
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
        val gestureDetector = GestureDetector(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(event: MotionEvent): Boolean = true

                override fun onFling(
                    startEvent: MotionEvent?,
                    endEvent: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (startEvent == null) return false

                    val distanceX = endEvent.x - startEvent.x
                    val distanceY = endEvent.y - startEvent.y
                    if (
                        kotlin.math.abs(distanceX) >= swipeDistance &&
                        kotlin.math.abs(distanceX) > kotlin.math.abs(distanceY)
                    ) {
                        if (distanceX < 0) viewModel.onNextClick()
                        else viewModel.onPreviousClick()
                        return true
                    }
                    return false
                }
            }
        )

        binding.nowPlayingRoot.setOnTouchListener { root, event ->
            if (event.actionMasked == MotionEvent.ACTION_UP) {
                root.performClick()
            }
            gestureDetector.onTouchEvent(event)
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
            binding.nowPlayingTime.text = getString(
                com.devpro.sound.R.string.time_format,
                formatTime(state.currentPositionMs),
                formatTime(state.durationMs)
            )
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
