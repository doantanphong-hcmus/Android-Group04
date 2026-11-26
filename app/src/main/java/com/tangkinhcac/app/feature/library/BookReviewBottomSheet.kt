package com.tangkinhcac.app.feature.library

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tangkinhcac.app.R

class BookReviewBottomSheet : BottomSheetDialogFragment(R.layout.sheet_book_review) {
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val bookId = requireArguments().getString(ARG_BOOK_ID).orEmpty()
        val book = LibrarySearchCatalog.books.firstOrNull { it.id == bookId }
        if (book == null) {
            dismiss()
            return
        }

        view.findViewById<TextView>(R.id.book_review_sheet_title).text =
            getString(R.string.book_review_sheet_title, book.title)
        val rating = view.findViewById<RatingBar>(R.id.book_review_sheet_rating)
        val ratingValue = view.findViewById<TextView>(R.id.book_review_sheet_rating_value)
        val accent = ContextCompat.getColor(requireContext(), R.color.library_accent)
        rating.progressTintList = ColorStateList.valueOf(accent)
        rating.secondaryProgressTintList = ColorStateList.valueOf(accent)
        rating.progressBackgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.library_border))
        rating.setOnRatingBarChangeListener { _, value, _ ->
            ratingValue.text = getString(R.string.book_review_rating_value, value.toInt())
        }

        view.findViewById<View>(R.id.book_review_sheet_submit).setOnClickListener {
            if (rating.rating <= 0f) {
                ratingValue.setText(R.string.book_review_rating_required)
                return@setOnClickListener
            }
            parentFragmentManager.setFragmentResult(
                RESULT_REVIEW,
                Bundle().apply {
                    putInt(RESULT_RATING, rating.rating.toInt())
                    putString(
                        RESULT_COMMENT,
                        view
                            .findViewById<EditText>(R.id.book_review_sheet_comment)
                            .text
                            .toString()
                            .trim(),
                    )
                },
            )
            dismiss()
        }
    }

    companion object {
        const val TAG = "BookReviewBottomSheet"
        const val RESULT_REVIEW = "book_review_result"
        const val RESULT_RATING = "rating"
        const val RESULT_COMMENT = "comment"
        private const val ARG_BOOK_ID = "book_id"

        fun newInstance(bookId: String): BookReviewBottomSheet =
            BookReviewBottomSheet().apply {
                arguments = Bundle().apply { putString(ARG_BOOK_ID, bookId) }
            }
    }
}
