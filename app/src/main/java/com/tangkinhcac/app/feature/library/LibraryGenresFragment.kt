package com.tangkinhcac.app.feature.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.tangkinhcac.app.R
import com.tangkinhcac.app.ui.CategoryIconView

class LibraryGenresFragment : Fragment(R.layout.fragment_library_genres) {
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.genres_back_button).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val container = view.findViewById<LinearLayout>(R.id.genres_container)
        genres.forEachIndexed { index, title ->
            val row =
                LayoutInflater
                    .from(view.context)
                    .inflate(R.layout.item_library_genre, container, false)
            row.findViewById<CategoryIconView>(R.id.genre_icon).setIconIndex(index)
            row.findViewById<TextView>(R.id.genre_name).setText(title)
            container.addView(row)
        }
    }

    private companion object {
        val genres =
            listOf(
                R.string.library_genre_classic,
                R.string.library_genre_history,
                R.string.library_genre_legend,
                R.string.library_genre_novel,
                R.string.library_genre_martial_arts,
                R.string.library_genre_fantasy,
                R.string.library_genre_philosophy,
                R.string.library_genre_other,
            )
    }
}
