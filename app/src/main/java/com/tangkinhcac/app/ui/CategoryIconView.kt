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

/** Displays one cell from the design team's eight-icon category sprite. */
class CategoryIconView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        private val bitmap: Bitmap =
            (AppCompatResources.getDrawable(context, R.drawable.library_category_icons) as BitmapDrawable).bitmap
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val source = Rect()
        private val destination = RectF()
        private var iconIndex = 0

        init {
            val attributes = context.obtainStyledAttributes(attrs, R.styleable.CategoryIconView)
            try {
                iconIndex = attributes.getInt(R.styleable.CategoryIconView_categoryIconIndex, 0)
            } finally {
                attributes.recycle()
            }
        }

        fun setIconIndex(index: Int) {
            iconIndex = index.coerceIn(0, ICON_COUNT - 1)
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val cellWidth = bitmap.width / COLUMN_COUNT
            val cellHeight = bitmap.height / ROW_COUNT
            val column = iconIndex % COLUMN_COUNT
            val row = iconIndex / COLUMN_COUNT
            source.set(
                column * cellWidth,
                row * cellHeight,
                (column + 1) * cellWidth,
                (row + 1) * cellHeight,
            )
            val availableWidth = width - paddingLeft - paddingRight
            val availableHeight = height - paddingTop - paddingBottom
            val scale = min(availableWidth.toFloat() / cellWidth, availableHeight.toFloat() / cellHeight)
            val destinationWidth = cellWidth * scale
            val destinationHeight = cellHeight * scale
            val left = paddingLeft + (availableWidth - destinationWidth) / 2f
            val top = paddingTop + (availableHeight - destinationHeight) / 2f

            destination.set(left, top, left + destinationWidth, top + destinationHeight)
            canvas.drawBitmap(bitmap, source, destination, paint)
        }

        private companion object {
            const val COLUMN_COUNT = 4
            const val ROW_COUNT = 2
            const val ICON_COUNT = COLUMN_COUNT * ROW_COUNT
        }
    }
