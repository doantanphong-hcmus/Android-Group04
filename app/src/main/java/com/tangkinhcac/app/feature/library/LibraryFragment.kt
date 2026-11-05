package com.tangkinhcac.app.feature.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.tangkinhcac.app.R

class LibraryFragment : Fragment(R.layout.fragment_library) {
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        renderBooks(view)

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

    private fun renderBooks(root: View) {
        val booksContainer = root.findViewById<LinearLayout>(R.id.library_books_container)
        LibrarySearchCatalog.books.forEach { book ->
            val bookView =
                LayoutInflater
                    .from(booksContainer.context)
                    .inflate(R.layout.item_library_book_card, booksContainer, false)
            bookView.findViewById<ImageView>(R.id.library_book_cover).setImageResource(book.coverResId)
            bookView.findViewById<TextView>(R.id.library_book_title).text = book.title
            bookView.findViewById<TextView>(R.id.library_book_author).text = book.author
            bookView.findViewById<TextView>(R.id.library_book_rating).text =
                getString(R.string.library_rating_format, book.rating, book.reviewCount)
            bookView.findViewById<TextView>(R.id.library_book_badge).apply {
                setText(if (book.isVip) R.string.library_badge_vip else R.string.library_badge_free)
                setBackgroundResource(
                    if (book.isVip) {
                        R.drawable.bg_library_badge_vip
                    } else {
                        R.drawable.bg_library_badge_free
                    },
                )
            }
            booksContainer.addView(bookView)
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
