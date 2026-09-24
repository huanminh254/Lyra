package com.devpro.sound.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import kotlin.math.abs

class SwipeDismissFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var onSwipeDownStart: (() -> Unit)? = null
    var onSwipeDownChanged: ((Float) -> Unit)? = null
    var onSwipeDownEnd: ((Float) -> Unit)? = null
    var onSwipeDownCancel: (() -> Unit)? = null
    var onOtherTouchEvent: ((MotionEvent) -> Boolean)? = null

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var startX = 0f
    private var startY = 0f
    private var isDraggingDown = false
    private var velocityTracker: android.view.VelocityTracker? = null

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                isDraggingDown = false
                velocityTracker?.recycle()
                velocityTracker = android.view.VelocityTracker.obtain().also {
                    it.addMovement(event)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.addMovement(event)
                val distanceX = event.x - startX
                val distanceY = event.y - startY
                if (!isDraggingDown &&
                    distanceY > touchSlop &&
                    distanceY > abs(distanceX)
                ) {
                    isDraggingDown = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                    onSwipeDownStart?.invoke()
                    return true
                }
            }

            MotionEvent.ACTION_CANCEL -> resetGesture()
        }

        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDraggingDown) {
            return onOtherTouchEvent?.invoke(event) ?: true
        }

        velocityTracker?.addMovement(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                onSwipeDownChanged?.invoke((event.y - startY).coerceAtLeast(0f))
            }

            MotionEvent.ACTION_UP -> {
                velocityTracker?.computeCurrentVelocity(1000)
                onSwipeDownEnd?.invoke(velocityTracker?.yVelocity ?: 0f)
                resetGesture()
            }

            MotionEvent.ACTION_CANCEL -> {
                onSwipeDownCancel?.invoke()
                resetGesture()
            }
        }
        return true
    }

    private fun resetGesture() {
        velocityTracker?.recycle()
        velocityTracker = null
        isDraggingDown = false
        parent?.requestDisallowInterceptTouchEvent(false)
    }
}
