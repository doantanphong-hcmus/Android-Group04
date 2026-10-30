package com.tangkinhcac.app.ui

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Keeps interactive content clear of system bars while decorative artwork remains edge-to-edge.
 */
fun View.applySystemBarPadding() {
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { target, windowInsets ->
        val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        target.setPadding(
            initialLeft + systemBars.left,
            initialTop + systemBars.top,
            initialRight + systemBars.right,
            initialBottom + systemBars.bottom,
        )
        windowInsets
    }
    ViewCompat.requestApplyInsets(this)
}
