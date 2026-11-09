package com.tangkinhcac.app.feature.home

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tangkinhcac.app.R
import com.tangkinhcac.app.feature.community.CommunityFragment
import com.tangkinhcac.app.feature.library.LibraryFragment
import com.tangkinhcac.app.ui.applySystemBarPadding

class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val homeRoot = view.findViewById<View>(R.id.home_root)
        homeRoot.applySystemBarPadding()

        val bottomNavigation = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            showTab(item.itemId)
            true
        }
        bottomNavigation.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.navigation_library) {
                childFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }

        if (savedInstanceState == null) {
            bottomNavigation.selectedItemId = R.id.navigation_home
            showTab(R.id.navigation_home)
        }
    }

    private fun showTab(itemId: Int) {
        childFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val content = requireView().findViewById<FrameLayout>(R.id.main_content_container)
        content.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                if (itemId == R.id.navigation_library) R.color.library_background else android.R.color.white,
            ),
        )
        val fragment =
            when (itemId) {
                R.id.navigation_community -> CommunityFragment()
                R.id.navigation_library -> LibraryFragment()
                else -> EmptyTabFragment()
            }

        childFragmentManager
            .beginTransaction()
            .replace(R.id.main_content_container, fragment)
            .setPrimaryNavigationFragment(fragment)
            .commit()
    }
}
