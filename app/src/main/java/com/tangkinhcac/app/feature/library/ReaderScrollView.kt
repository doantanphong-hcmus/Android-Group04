package com.tangkinhcac.app.feature.library

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.ScrollView
import kotlin.math.abs

class ReaderScrollView(
    context: Context,
    attrs: AttributeSet?,
) : ScrollView(context, attrs) {
    var onPageTurn: ((Int) -> Unit)? = null
    private var startX = 0f
    private var startY = 0f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
            }

            MotionEvent.ACTION_UP -> {
                val distanceX = event.x - startX
                val distanceY = event.y - startY
                if (abs(distanceX) > 90 * resources.displayMetrics.density && abs(distanceX) > abs(distanceY)) {
                    onPageTurn?.invoke(if (distanceX < 0) 1 else -1)
                    return true
                }
                if (abs(distanceX) < touchSlop && abs(distanceY) < touchSlop) {
                    performClick()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean = super.performClick()
}
