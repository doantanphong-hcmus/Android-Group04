package com.tangkinhcac.app.feature.home

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.tangkinhcac.app.R

class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val homeRoot = view.findViewById<View>(R.id.home_root)
        ViewCompat.setOnApplyWindowInsetsListener(homeRoot) { target, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            target.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom,
            )
            windowInsets
        }
    }
}
