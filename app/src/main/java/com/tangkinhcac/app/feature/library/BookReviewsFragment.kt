package com.tangkinhcac.app.feature.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tangkinhcac.app.R

class BookReviewsFragment : Fragment(R.layout.fragment_book_reviews) {
    // shortcut: keep the submitted review in this screen until the backend review schema is ready.
    private var submittedReview: SubmittedReview? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        submittedReview =
            savedInstanceState?.let {
                val rating = it.getInt(KEY_SUBMITTED_RATING)
                val comment = it.getString(KEY_SUBMITTED_COMMENT).orEmpty()
                if (rating > 0) SubmittedReview(rating, comment) else null
            }

        parentFragmentManager.setFragmentResultListener(
            BookReviewBottomSheet.RESULT_REVIEW,
            this,
        ) { _, result ->
            submittedReview =
                SubmittedReview(
                    rating = result.getInt(BookReviewBottomSheet.RESULT_RATING),
                    comment = result.getString(BookReviewBottomSheet.RESULT_COMMENT).orEmpty(),
                )
            view?.let(::renderSubmittedReview)
        }
    }

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

        view.findViewById<ImageButton>(R.id.book_reviews_back).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        view.findViewById<ImageView>(R.id.book_reviews_cover).apply {
            setImageResource(book.coverResId)
            clipToOutline = true
        }
        view.findViewById<TextView>(R.id.book_reviews_title).text = book.title
        view.findViewById<TextView>(R.id.book_reviews_rating).text = book.rating.replace('.', ',')
        view.findViewById<TextView>(R.id.book_reviews_count).text =
            getString(R.string.book_detail_review_count, book.reviewCount)
        view.findViewById<View>(R.id.book_reviews_write).setOnClickListener { openComposer(book.id) }

        renderSubmittedReview(view)
        if (savedInstanceState == null && requireArguments().getBoolean(ARG_OPEN_COMPOSER)) {
            openComposer(book.id)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        submittedReview?.let {
            outState.putInt(KEY_SUBMITTED_RATING, it.rating)
            outState.putString(KEY_SUBMITTED_COMMENT, it.comment)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        setBottomNavigationVisible(parentFragmentManager.backStackEntryCount == 0)
        super.onDestroyView()
    }

    private fun renderSubmittedReview(root: View) {
        val container = root.findViewById<LinearLayout>(R.id.book_reviews_submitted_container)
        container.removeAllViews()
        container.isVisible = submittedReview != null
        submittedReview?.let { review ->
            val reviewView =
                LayoutInflater
                    .from(root.context)
                    .inflate(R.layout.item_book_review, container, false)
            reviewView.findViewById<TextView>(R.id.book_review_initials).text =
                getString(R.string.book_review_you_initials)
            reviewView.findViewById<TextView>(R.id.book_review_name).text = getString(R.string.book_review_you)
            reviewView.findViewById<TextView>(R.id.book_review_time).text = getString(R.string.book_review_just_now)
            reviewView.findViewById<TextView>(R.id.book_review_stars).text = reviewStars(review.rating)
            reviewView.findViewById<TextView>(R.id.book_review_comment).apply {
                text = review.comment
                isVisible = review.comment.isNotBlank()
            }
            container.addView(reviewView)
        }
    }

    private fun openComposer(bookId: String) {
        BookReviewBottomSheet.newInstance(bookId).show(parentFragmentManager, BookReviewBottomSheet.TAG)
    }

    private fun setBottomNavigationVisible(isVisible: Boolean) {
        parentFragment
            ?.view
            ?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
            ?.isVisible = isVisible
    }

    private data class SubmittedReview(
        val rating: Int,
        val comment: String,
    )

    companion object {
        private const val ARG_BOOK_ID = "book_id"
        private const val ARG_OPEN_COMPOSER = "open_composer"
        private const val KEY_SUBMITTED_RATING = "submitted_rating"
        private const val KEY_SUBMITTED_COMMENT = "submitted_comment"

        fun newInstance(
            bookId: String,
            openComposer: Boolean,
        ): BookReviewsFragment =
            BookReviewsFragment().apply {
                arguments =
                    Bundle().apply {
                        putString(ARG_BOOK_ID, bookId)
                        putBoolean(ARG_OPEN_COMPOSER, openComposer)
                    }
            }
    }
}

internal fun reviewStars(rating: Int): String =
    "★★★★★".take(rating.coerceIn(0, 5)) + "☆☆☆☆☆".take((5 - rating).coerceIn(0, 5))
