package com.tangkinhcac.app.feature.library

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.tangkinhcac.app.R

class LibraryFragment : Fragment(R.layout.fragment_library) {
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val searchInput = view.findViewById<EditText>(R.id.library_search_input)
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                openSearch(searchInput.text.toString())
                true
            } else {
                false
            }
        }

        view.findViewById<ImageButton>(R.id.library_genres_button).setOnClickListener {
            val fragment = LibraryGenresFragment()
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.main_content_container, fragment)
                .setPrimaryNavigationFragment(fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun openSearch(query: String) {
        val fragment = LibrarySearchFragment.newInstance(query.trim())
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.main_content_container, fragment)
            .setPrimaryNavigationFragment(fragment)
            .addToBackStack(null)
            .commit()
    }
}
