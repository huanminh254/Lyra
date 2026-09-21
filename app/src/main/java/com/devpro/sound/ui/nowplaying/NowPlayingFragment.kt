package com.devpro.sound.ui.nowplaying

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.util.TypedValue
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.devpro.sound.MainActivity
import com.devpro.sound.R
import com.devpro.sound.data.model.Comment
import com.devpro.sound.databinding.FragmentNowPlayingBinding
import com.devpro.sound.ui.components.loadSongCover
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.activityViewModels
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

@AndroidEntryPoint
class NowPlayingFragment : Fragment() {
    private var _binding: FragmentNowPlayingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NowPlayingViewModel by activityViewModels()
    private val swipeDistance = 120f
    private var commentAnimator: AnimatorSet? = null
    private var playbackControlsHidden = false
    private var boundSongId: String? = null
    private var lastRenderedPlaying: Boolean? = null
    private var lastRenderedFavorite: Boolean? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNowPlayingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        boundSongId = null
        lastRenderedPlaying = null
        lastRenderedFavorite = null

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
        binding.nowPlayingLike.setOnClickListener { viewModel.toggleFavorite() }
        binding.nowPlayingControls.setOnClickListener {
            viewModel.onPlayPauseClick()
        }
        binding.commentInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                submitComment()
                true
            } else {
                false
            }
        }
        binding.commentSend.setOnClickListener { submitComment() }
        binding.commentFire.setOnClickListener { submitComment("🔥") }
        binding.commentWave.setOnClickListener { submitComment("👋") }
        binding.commentCry.setOnClickListener { submitComment("🥺") }
        binding.nowPlayingSeek.onSeek = viewModel::onSeek
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
            if (boundSongId != state.song?.id) {
                boundSongId = state.song?.id
                state.song?.let { song ->
                    binding.nowPlayingTitle.text = song.title
                    binding.nowPlayingArtist.text = song.artist
                    binding.nowPlayingCover.loadSongCover(song.coverUrl)
                    binding.nowPlayingSeek.setWaveform(song.waveform)
                    prepareCoverParallax()
                }
            }

            if (lastRenderedPlaying != state.isPlaying) {
                lastRenderedPlaying = state.isPlaying
                binding.nowPlayingPlayPause.setImageResource(
                    if (state.isPlaying) com.devpro.sound.R.drawable.pause
                    else com.devpro.sound.R.drawable.resume
                )
                updatePlaybackControls(state.isPlaying)
                updateCommentAnimation(state.isPlaying)
            }

            val isFavorite = state.song?.let { song ->
                state.favoriteSongs.any { favorite -> favorite.id == song.id }
            } == true
            if (lastRenderedFavorite != isFavorite) {
                lastRenderedFavorite = isFavorite
                binding.nowPlayingLike.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isFavorite) android.R.color.holo_red_light else R.color.white
                    )
                )
                binding.nowPlayingLike.contentDescription = if (isFavorite) {
                    "Bỏ yêu thích"
                } else {
                    "Thêm vào yêu thích"
                }
            }
            binding.nowPlayingTime.text = getString(
                R.string.time_format,
                formatTime(state.currentPositionMs),
                formatTime(state.durationMs)
            )
        }

        viewModel.playbackProgress.observe(viewLifecycleOwner) { progress ->
            binding.nowPlayingSeek.setProgress(progress)
            updateCoverParallax(progress)
        }

        viewModel.likeCount.observe(viewLifecycleOwner) { count ->
            binding.nowPlayingLikeCount.text = count.toString()
        }

        viewModel.commentCount.observe(viewLifecycleOwner) { count ->
            binding.nowPlayingCommentCount.text = count.toString()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentComments.collectLatest { comments ->
                    renderCurrentComments(comments)
                }
            }
        }
    }

    private fun submitComment(reaction: String? = null) {
        val content = reaction ?: binding.commentInput.text?.toString().orEmpty()
        if (content.trim().isEmpty()) return
        viewModel.addComment(content)
        if (reaction == null) binding.commentInput.text?.clear()
    }

    private fun renderCurrentComments(comments: List<Comment>) {
        commentAnimator?.cancel()
        commentAnimator = null
        binding.nowPlayingCommentOverlay.removeAllViews()

        if (comments.isEmpty()) {
            binding.nowPlayingCommentOverlay.visibility = View.GONE
            return
        }

        binding.nowPlayingCommentOverlay.visibility = View.VISIBLE
        val bubbles = comments.map { comment ->
            createCommentBubble(comment).also { bubble ->
                binding.nowPlayingCommentOverlay.addView(bubble)
            }
        }

        binding.nowPlayingCommentOverlay.post {
            placeBubblesRandomly(bubbles)

            val bubbleAnimations = bubbles.map { bubble ->
                bubble.alpha = 0f
                val fadeIn = ObjectAnimator.ofFloat(bubble, View.ALPHA, 0f, 1f).apply {
                    duration = COMMENT_FADE_DURATION_MS
                }
                val hold = ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = COMMENT_VISIBLE_DURATION_MS
                }
                val fadeOut = ObjectAnimator.ofFloat(bubble, View.ALPHA, 1f, 0f).apply {
                    duration = COMMENT_EXIT_DURATION_MS
                }
                AnimatorSet().apply {
                    playSequentially(fadeIn, hold, fadeOut)
                }
            }

            commentAnimator = AnimatorSet().apply {
                playSequentially(bubbleAnimations)
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (commentAnimator === animation) {
                            binding.nowPlayingCommentOverlay.visibility = View.GONE
                            commentAnimator = null
                        }
                    }
                })
                start()
            }

            if (viewModel.uiState.value?.isPlaying != true) {
                commentAnimator?.pause()
            }
        }
    }

    private fun createCommentBubble(comment: Comment): TextView {
        return TextView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
            }
            setPadding(dp(14), dp(8), dp(14), dp(8))
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            maxWidth = dp(240)
            text = comment.content
            background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.comment_bubble_background
            )
            elevation = dp(2).toFloat()
        }
    }

    private fun placeBubblesRandomly(bubbles: List<TextView>) {
        val container = binding.nowPlayingCommentOverlay
        bubbles.forEach { bubble ->
            val maxX = (container.width - bubble.width).coerceAtLeast(0)
            val maxY = (container.height - bubble.height).coerceAtLeast(0)
            bubble.translationX = if (maxX == 0) 0f else Random.nextInt(maxX + 1).toFloat()
            bubble.translationY = if (maxY == 0) 0f else Random.nextInt(maxY + 1).toFloat()
        }
    }

    private fun updateCommentAnimation(isPlaying: Boolean) {
        commentAnimator?.let { animator ->
            if (isPlaying) animator.resume() else animator.pause()
        }
    }

    private fun updatePlaybackControls(isPlaying: Boolean) {
        if (playbackControlsHidden == isPlaying) return
        playbackControlsHidden = isPlaying

        val controls = listOf(
            binding.nowPlayingPrevious,
            binding.nowPlayingPlayPause,
            binding.nowPlayingNext
        )

        if (isPlaying) {
            controls.forEach { control ->
                control.animate()
                    .alpha(0f)
                    .setDuration(CONTROLS_ANIMATION_DURATION_MS)
                    .withEndAction {
                        control.visibility = View.INVISIBLE
                    }
                    .start()
            }
        } else {
            controls.forEach { control ->
                control.animate().cancel()
                control.visibility = View.VISIBLE
                control.alpha = 0f
                control.animate()
                    .alpha(1f)
                    .setDuration(CONTROLS_ANIMATION_DURATION_MS)
                    .start()
            }
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).roundToInt()
    }

    private fun prepareCoverParallax() {
        binding.nowPlayingCover.post {
            // Slightly enlarge the image so horizontal movement never exposes
            // an empty edge inside the rounded card.
            binding.nowPlayingCover.scaleX = COVER_SCALE
            binding.nowPlayingCover.scaleY = COVER_SCALE
            updateCoverParallax(viewModel.uiState.value?.progress ?: 0f)
        }
    }

    private fun updateCoverParallax(progress: Float) {
        if (binding.nowPlayingCover.width <= 0) return
        val maxTranslation = binding.nowPlayingCover.width * COVER_TRANSLATION_FRACTION
        binding.nowPlayingCover.translationX = -progress.coerceIn(0f, 1f) * maxTranslation
    }

    private fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs.coerceAtLeast(0L) / 1000
        return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }

    override fun onDestroyView() {
        commentAnimator?.cancel()
        commentAnimator = null
        boundSongId = null
        lastRenderedPlaying = null
        lastRenderedFavorite = null
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val COMMENT_FADE_DURATION_MS = 50L
        const val COMMENT_VISIBLE_DURATION_MS = 1000L
        const val COMMENT_EXIT_DURATION_MS = 400L
        const val COVER_SCALE = 1.08f
        const val COVER_TRANSLATION_FRACTION = 0.08f
        const val CONTROLS_ANIMATION_DURATION_MS = 180L
    }
}
