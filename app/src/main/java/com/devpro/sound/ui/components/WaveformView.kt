package com.devpro.sound.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.sin

/**
 * A horizontally scrolling waveform. One waveform slot represents roughly
 * a quarter of a second. The current position stays around the middle while
 * the bars move from right to left.
 */
class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onSeek: ((Float) -> Unit)? = null

    private val playedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 107, 0)
        style = Paint.Style.FILL
    }
    private val remainingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(224, 224, 224)
        style = Paint.Style.FILL
    }
    private val centerLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(190, 255, 255, 255)
        strokeWidth = dp(1f)
        style = Paint.Style.STROKE
    }
    private val barRect = RectF()

    private var waveform = emptyList<Float>()
    private var progress = 0f
    private var renderedProgress = 0f
    private var animationStartProgress = 0f
    private var animationStartNanos = 0L
    private var animationDurationNanos = 0L
    private var frameScheduled = false
    private val frameRunnable = object : Runnable {
        override fun run() {
            frameScheduled = false
            val now = System.nanoTime()
            val elapsed = now - animationStartNanos
            val fraction = if (animationDurationNanos <= 0L) {
                1f
            } else {
                (elapsed.toDouble() / animationDurationNanos.toDouble())
                    .coerceIn(0.0, 1.0)
                    .toFloat()
            }
            renderedProgress = animationStartProgress +
                (progress - animationStartProgress) * fraction

            if (fraction >= 1f || abs(renderedProgress - progress) < PROGRESS_EPSILON) {
                renderedProgress = progress
            } else {
                scheduleNextFrame()
            }
            postInvalidateOnAnimation()
        }
    }
    private var fallbackWaveform: List<Float>? = null
    private var isDragging = false
    private var dragStartX = 0f
    private var dragStartProgress = 0f

    init {
        isClickable = true
        isFocusable = true
    }

    fun setWaveform(values: List<Float>) {
        val nextWaveform = values.takeIf { it.isNotEmpty() } ?: getFallbackWaveform()
        if (waveform === nextWaveform || waveform == nextWaveform) return
        waveform = nextWaveform
        postInvalidateOnAnimation()
    }

    fun setProgress(value: Float) {
        val safeProgress = value.coerceIn(0f, 1f)
        if (abs(progress - safeProgress) < PROGRESS_EPSILON) return
        progress = safeProgress

        if (isDragging) {
            removeCallbacks(frameRunnable)
            frameScheduled = false
            renderedProgress = safeProgress
            postInvalidateOnAnimation()
            return
        }

        val distance = abs(renderedProgress - safeProgress)
        if (distance < PROGRESS_EPSILON) {
            renderedProgress = safeProgress
            postInvalidateOnAnimation()
            return
        }

        animationStartProgress = renderedProgress
        animationStartNanos = System.nanoTime()
        animationDurationNanos = (distance * PROGRESS_ANIMATION_DURATION_MS_PER_UNIT)
            .toLong()
            .coerceIn(MIN_PROGRESS_ANIMATION_MS, MAX_PROGRESS_ANIMATION_MS) * NANOS_PER_MILLISECOND
        scheduleNextFrame()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (waveform.isEmpty() || width <= 0 || height <= 0) return

        val slotWidth = dp(BAR_SLOT_WIDTH_DP)
        val barWidth = dp(BAR_WIDTH_DP).coerceAtMost(slotWidth * 0.72f)
        val centerY = height / 2f
        val maxBarHeight = height * 0.92f
        val playheadX = width / 2f
        val playedBoundary = renderedProgress * waveform.size
        val scrollSlot = renderedProgress * (waveform.size - 1).coerceAtLeast(0)

        // At the beginning, bars start around the centre and extend right.
        // As playback advances, the waveform moves from right to left. Near
        // the end we deliberately do not clamp the offset: the final bar is
        // allowed to move into the centre instead of stopping at the edge.
        val offset = playheadX - (scrollSlot + 0.5f) * slotWidth

        canvas.drawLine(0f, centerY, width.toFloat(), centerY, centerLinePaint)

        val firstVisibleIndex = ((-offset / slotWidth).toInt() - 1)
            .coerceAtLeast(0)
        val lastVisibleIndex = (((width - offset) / slotWidth).toInt() + 1)
            .coerceAtMost(waveform.lastIndex)

        if (firstVisibleIndex <= lastVisibleIndex) {
            for (index in firstVisibleIndex..lastVisibleIndex) {
                val safeAmplitude = waveform[index].coerceIn(0f, 1f)
                val barHeight = (maxBarHeight * safeAmplitude.coerceAtLeast(0.06f))
                    .coerceAtLeast(dp(MIN_BAR_HEIGHT_DP))
                val centerX = offset + index * slotWidth + slotWidth / 2f
                barRect.set(
                    centerX - barWidth / 2f,
                    centerY - barHeight / 2f,
                    centerX + barWidth / 2f,
                    centerY + barHeight / 2f
                )
                val paint = if (index < playedBoundary) playedPaint else remainingPaint
                canvas.drawRoundRect(barRect, barWidth / 2f, barWidth / 2f, paint)
            }
        }

        // The playhead remains fixed while the waveform scrolls behind it.
        canvas.drawLine(playheadX, 0f, playheadX, height.toFloat(), centerLinePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = true
                removeCallbacks(frameRunnable)
                frameScheduled = false
                dragStartX = event.x
                dragStartProgress = progress
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragging) updateProgressFromDrag(event.x)
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) updateProgressFromDrag(event.x)
                isDragging = false
                parent?.requestDisallowInterceptTouchEvent(false)
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun updateProgressFromDrag(x: Float) {
        if (waveform.isEmpty() || width <= 0) return

        // Dragging left advances playback; dragging right rewinds it.
        val dragDistance = dragStartX - x
        val safeProgress = (dragStartProgress + dragDistance / width.toFloat())
            .coerceIn(0f, 1f)
        progress = safeProgress
        renderedProgress = safeProgress
        postInvalidateOnAnimation()
        onSeek?.invoke(safeProgress)
    }

    private fun scheduleNextFrame() {
        if (frameScheduled) return
        frameScheduled = true
        postOnAnimation(frameRunnable)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(frameRunnable)
        frameScheduled = false
        super.onDetachedFromWindow()
    }

    private fun getFallbackWaveform(): List<Float> {
        return fallbackWaveform ?: List(FALLBACK_BAR_COUNT) { index ->
            (0.18f + abs(sin(index * 0.53f)).toFloat() * 0.7f)
                .coerceIn(0f, 1f)
        }.also { fallbackWaveform = it }
    }

    private fun dp(value: Float): Float {
        return value * resources.displayMetrics.density
    }

    private companion object {
        const val BAR_SLOT_WIDTH_DP = 6f
        const val BAR_WIDTH_DP = 3.5f
        const val MIN_BAR_HEIGHT_DP = 2f
        const val FALLBACK_BAR_COUNT = 96
        const val PROGRESS_ANIMATION_DURATION_MS_PER_UNIT = 1_000f
        const val MIN_PROGRESS_ANIMATION_MS = 80L
        const val MAX_PROGRESS_ANIMATION_MS = 550L
        const val NANOS_PER_MILLISECOND = 1_000_000L
        const val PROGRESS_EPSILON = 0.0001f
    }
}
