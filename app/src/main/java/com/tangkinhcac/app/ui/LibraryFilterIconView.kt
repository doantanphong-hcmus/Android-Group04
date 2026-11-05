package com.tangkinhcac.app.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.tangkinhcac.app.R
import kotlin.math.min

/** Displays one icon from the design team's four-icon library filter sprite. */
class LibraryFilterIconView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        private val bitmap: Bitmap =
            (AppCompatResources.getDrawable(context, R.drawable.library_filter_icons) as BitmapDrawable).bitmap
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val source = Rect()
        private val destination = RectF()
        private var iconIndex = 0

        init {
            val attributes = context.obtainStyledAttributes(attrs, R.styleable.LibraryFilterIconView)
            try {
                iconIndex = attributes.getInt(R.styleable.LibraryFilterIconView_filterIconIndex, 0)
            } finally {
                attributes.recycle()
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val sourceLeft = (ICON_LEFTS[iconIndex] - SOURCE_PADDING).coerceAtLeast(0)
            val sourceTop = (ICON_TOPS[iconIndex] - SOURCE_PADDING).coerceAtLeast(0)
            val sourceRight = (ICON_RIGHTS[iconIndex] + SOURCE_PADDING).coerceAtMost(bitmap.width)
            val sourceBottom = (ICON_BOTTOMS[iconIndex] + SOURCE_PADDING).coerceAtMost(bitmap.height)
            val sourceWidth = sourceRight - sourceLeft
            val sourceHeight = sourceBottom - sourceTop
            source.set(sourceLeft, sourceTop, sourceRight, sourceBottom)

            val availableWidth = width - paddingLeft - paddingRight
            val availableHeight = height - paddingTop - paddingBottom
            val scale = min(availableWidth.toFloat() / sourceWidth, availableHeight.toFloat() / sourceHeight)
            val destinationWidth = sourceWidth * scale
            val destinationHeight = sourceHeight * scale
            val left = paddingLeft + (availableWidth - destinationWidth) / 2f
            val top = paddingTop + (availableHeight - destinationHeight) / 2f
            destination.set(left, top, left + destinationWidth, top + destinationHeight)

            canvas.drawBitmap(bitmap, source, destination, paint)
        }

        private companion object {
            const val SOURCE_PADDING = 20
            val ICON_LEFTS = intArrayOf(288, 727, 1189, 1635)
            val ICON_TOPS = intArrayOf(235, 235, 256, 242)
            val ICON_RIGHTS = intArrayOf(476, 962, 1434, 1899)
            val ICON_BOTTOMS = intArrayOf(468, 458, 458, 454)
        }
    }
