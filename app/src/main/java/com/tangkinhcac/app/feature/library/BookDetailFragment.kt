package com.tangkinhcac.app.feature.library

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tangkinhcac.app.R

class BookDetailFragment : Fragment(R.layout.fragment_book_detail) {
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setBottomNavigationVisible(false)

        val bookId = requireArguments().getString(ARG_BOOK_ID).orEmpty()
        val book = LibrarySearchCatalog.books.firstOrNull { it.id == bookId }
        if (book == null) {
            parentFragmentManager.popBackStack()
            return
        }

        bindBook(view, book)
        bindActions(view, book)
    }

    override fun onDestroyView() {
        setBottomNavigationVisible(parentFragmentManager.backStackEntryCount == 0)
        super.onDestroyView()
    }

    private fun bindBook(
        root: View,
        book: LibraryBook,
    ) {
        root.findViewById<ImageView>(R.id.book_detail_cover).apply {
            setImageResource(book.coverResId)
            clipToOutline = true
        }
        root.findViewById<TextView>(R.id.book_detail_title).text = book.title
        root.findViewById<TextView>(R.id.book_detail_author).text = book.author
        root.findViewById<TextView>(R.id.book_detail_metadata).text =
            getString(
                R.string.book_detail_metadata_format,
                book.country,
                book.genres.firstOrNull { it.contains("bát", ignoreCase = true) }
                    ?: book.genres.firstOrNull().orEmpty(),
            )
        root.findViewById<TextView>(R.id.book_detail_rating).text =
            getString(R.string.book_detail_rating_format, book.rating, book.reviewCount)
        root.findViewById<TextView>(R.id.book_detail_synopsis).text = book.synopsis
    }

    private fun bindActions(
        root: View,
        book: LibraryBook,
    ) {
        root.findViewById<ImageButton>(R.id.book_detail_back).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val bookmark = root.findViewById<ImageButton>(R.id.book_detail_bookmark)
        bookmark.setOnClickListener {
            bookmark.isSelected = !bookmark.isSelected
            bookmark.setImageResource(
                if (bookmark.isSelected) {
                    R.drawable.ic_community_bookmark_active
                } else {
                    R.drawable.ic_community_bookmark
                },
            )
            bookmark.contentDescription =
                getString(
                    if (bookmark.isSelected) {
                        R.string.book_detail_remove_bookmark
                    } else {
                        R.string.book_detail_add_bookmark
                    },
                )
        }

        val synopsis = root.findViewById<TextView>(R.id.book_detail_synopsis)
        val expand = root.findViewById<TextView>(R.id.book_detail_expand)
        expand.setOnClickListener {
            val isExpanded = synopsis.maxLines == Int.MAX_VALUE
            synopsis.maxLines = if (isExpanded) COLLAPSED_SYNOPSIS_LINES else Int.MAX_VALUE
            expand.setText(if (isExpanded) R.string.book_detail_read_more else R.string.book_detail_collapse)
        }

        val openReviews = View.OnClickListener { openReviews(book.id) }
        root.findViewById<View>(R.id.book_detail_rating).setOnClickListener(openReviews)
        root.findViewById<View>(R.id.book_detail_reviews).setOnClickListener(openReviews)
        root.findViewById<View>(R.id.book_detail_view_all_reviews).setOnClickListener(openReviews)
        root.findViewById<View>(R.id.book_detail_write_review).setOnClickListener {
            openReviews(book.id, openComposer = true)
        }

        root.findViewById<View>(R.id.book_detail_read).setOnClickListener {
            val fragment = BookReaderFragment.newInstance(book.id, book.title)
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.main_content_container, fragment)
                .setPrimaryNavigationFragment(fragment)
                .addToBackStack(null)
                .commit()
        }
        root.findViewById<View>(R.id.book_detail_listen).setOnClickListener {
            Toast.makeText(requireContext(), R.string.book_detail_content_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openReviews(
        bookId: String,
        openComposer: Boolean = false,
    ) {
        val fragment = BookReviewsFragment.newInstance(bookId, openComposer)
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.main_content_container, fragment)
            .setPrimaryNavigationFragment(fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setBottomNavigationVisible(isVisible: Boolean) {
        parentFragment
            ?.view
            ?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
            ?.isVisible = isVisible
    }

    companion object {
        private const val ARG_BOOK_ID = "book_id"
        private const val COLLAPSED_SYNOPSIS_LINES = 3

        fun newInstance(bookId: String): BookDetailFragment =
            BookDetailFragment().apply {
                arguments = Bundle().apply { putString(ARG_BOOK_ID, bookId) }
            }
    }
}
