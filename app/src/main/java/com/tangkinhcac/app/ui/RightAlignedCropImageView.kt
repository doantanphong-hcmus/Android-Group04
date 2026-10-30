package com.tangkinhcac.app.ui

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max

/**
 * Crops a full-screen image from the left so artwork near its right edge remains visible.
 */
class RightAlignedCropImageView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : AppCompatImageView(context, attrs, defStyleAttr) {
        init {
            scaleType = ScaleType.MATRIX
        }

        override fun onSizeChanged(
            width: Int,
            height: Int,
            oldWidth: Int,
            oldHeight: Int,
        ) {
            super.onSizeChanged(width, height, oldWidth, oldHeight)
            updateCropMatrix(width, height)
        }

        private fun updateCropMatrix(
            viewWidth: Int,
            viewHeight: Int,
        ) {
            val image = drawable ?: return
            if (viewWidth == 0 || viewHeight == 0 || image.intrinsicWidth <= 0 || image.intrinsicHeight <= 0) {
                return
            }

            val scale =
                max(
                    viewWidth.toFloat() / image.intrinsicWidth,
                    viewHeight.toFloat() / image.intrinsicHeight,
                )
            val scaledWidth = image.intrinsicWidth * scale
            val scaledHeight = image.intrinsicHeight * scale

            imageMatrix =
                Matrix().apply {
                    setScale(scale, scale)
                    postTranslate(
                        viewWidth - scaledWidth,
                        (viewHeight - scaledHeight) / 2f,
                    )
                }
        }
    }
