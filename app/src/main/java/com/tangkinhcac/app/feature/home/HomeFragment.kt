package com.tangkinhcac.app.feature.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tangkinhcac.app.R
import com.tangkinhcac.app.feature.community.CommunityFragment
import com.tangkinhcac.app.feature.library.LibraryFragment
import com.tangkinhcac.app.feature.rankings.RankingsActivity
import com.tangkinhcac.app.ui.applySystemBarPadding

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var selectedTabId = R.id.navigation_home

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val homeRoot = view.findViewById<View>(R.id.home_root)
        homeRoot.applySystemBarPadding()

        // shortcut: temporary prototype entry point, remove when Rankings joins the production navigation flow.
        view.findViewById<AppCompatButton>(R.id.open_rankings_test_button).setOnClickListener {
            startActivity(Intent(requireContext(), RankingsActivity::class.java))
        }

        selectedTabId = savedInstanceState?.getInt(KEY_SELECTED_TAB_ID) ?: selectedTabId
        val bottomNavigation = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = selectedTabId
        bottomNavigation.setOnItemSelectedListener { item ->
            selectedTabId = item.itemId
            showTab(item.itemId)
            true
        }
        bottomNavigation.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.navigation_library) {
                childFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }

        showTab(selectedTabId)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_SELECTED_TAB_ID, selectedTabId)
        super.onSaveInstanceState(outState)
    }

    private fun showTab(itemId: Int) {
        childFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        requireView().findViewById<View>(R.id.open_rankings_test_button).isVisible =
            itemId == R.id.navigation_home
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

    companion object {
        private const val KEY_SELECTED_TAB_ID = "selected_tab_id"
    }
}
