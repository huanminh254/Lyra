package com.devpro.sound.ui.components

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * Shared pull-to-refresh container using the standard SwipeRefreshLayout spinner.
 */
class PullToRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs) {

    private val density = resources.displayMetrics.density
    private val triggerDistance = 120f * density
    private var onPullToRefresh: (() -> Unit)? = null

    init {
        if (!isInEditMode) {
            setDistanceToTriggerSync(triggerDistance.toInt())
            setColorSchemeColors(Color.GRAY)
            setProgressBackgroundColorSchemeColor(Color.TRANSPARENT)
            setOnChildScrollUpCallback { _, _ -> findScrollableChild(this) }

            super.setOnRefreshListener {
                onPullToRefresh?.invoke()
            }
        }
    }

    fun setOnPullToRefreshListener(listener: () -> Unit) {
        onPullToRefresh = listener
    }

    fun setIndicatorColor(color: Int) {
        setColorSchemeColors(color)
    }

    fun finishRefresh() {
        isRefreshing = false
    }

    private fun findScrollableChild(view: View): Boolean {
        if (view !== this && view.canScrollVertically(-1)) return true
        if (view is ViewGroup) {
            for (index in view.childCount - 1 downTo 0) {
                if (findScrollableChild(view.getChildAt(index))) return true
            }
        }
        return false
    }
}
