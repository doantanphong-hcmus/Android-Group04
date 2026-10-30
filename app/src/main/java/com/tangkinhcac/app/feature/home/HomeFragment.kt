package com.tangkinhcac.app.feature.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.tangkinhcac.app.R
import com.tangkinhcac.app.ui.applySystemBarPadding

class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val homeRoot = view.findViewById<View>(R.id.home_root)
        homeRoot.applySystemBarPadding()
    }
}
