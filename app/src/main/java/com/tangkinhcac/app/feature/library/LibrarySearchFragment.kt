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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.tangkinhcac.app.R

class LibrarySearchFragment : Fragment(R.layout.fragment_library_search) {
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val searchInput = view.findViewById<EditText>(R.id.search_query_input)
        val initialQuery = arguments?.getString(ARG_QUERY).orEmpty()
        searchInput.setText(initialQuery)
        searchInput.setSelection(initialQuery.length)

        view.findViewById<ImageButton>(R.id.search_back_button).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        view.findViewById<ImageButton>(R.id.search_clear_button).setOnClickListener {
            searchInput.text.clear()
            searchInput.requestFocus()
            renderResults(view, "")
        }
        view.findViewById<TextView>(R.id.search_retry_button).setOnClickListener {
            searchInput.text.clear()
            searchInput.requestFocus()
            renderResults(view, "")
        }
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                renderResults(view, searchInput.text.toString())
                true
            } else {
                false
            }
        }

        renderResults(view, initialQuery)
    }

    private fun renderResults(
        root: View,
        query: String,
    ) {
        val results = LibrarySearchCatalog.search(query.trim())
        val resultsContainer = root.findViewById<LinearLayout>(R.id.search_results_container)
        val emptyContainer = root.findViewById<LinearLayout>(R.id.search_empty_container)
        val tabs = root.findViewById<LinearLayout>(R.id.search_tabs)

        resultsContainer.removeAllViews()
        resultsContainer.isVisible = results.isNotEmpty()
        emptyContainer.isVisible = results.isEmpty()
        tabs.isVisible = results.isNotEmpty()

        root.findViewById<TextView>(R.id.search_tab_all).text =
            getString(R.string.library_search_tab_all, results.size)
        root.findViewById<TextView>(R.id.search_tab_books).text =
            getString(R.string.library_search_tab_books, results.size)
        root.findViewById<TextView>(R.id.search_tab_authors).text =
            getString(R.string.library_search_tab_authors, results.map { it.author }.distinct().size)
        root.findViewById<TextView>(R.id.search_empty_message).text =
            getString(R.string.library_no_results_message, query.trim())

        results.forEach { book ->
            resultsContainer.addView(createResultView(resultsContainer, book))
        }
    }

    private fun createResultView(
        parent: LinearLayout,
        book: LibraryBook,
    ): View {
        val resultView =
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_library_search_result, parent, false)
        resultView
            .findViewById<ImageView>(R.id.search_result_cover)
            .setImageResource(book.coverResId)
        resultView.findViewById<TextView>(R.id.search_result_title).text = book.title
        resultView.findViewById<TextView>(R.id.search_result_author).text = book.author
        resultView.findViewById<TextView>(R.id.search_result_rating).text =
            getString(R.string.library_rating_format, book.rating, book.reviewCount)
        resultView.findViewById<TextView>(R.id.search_result_badge).apply {
            setText(if (book.isVip) R.string.library_badge_vip else R.string.library_badge_free)
            setBackgroundResource(
                if (book.isVip) R.drawable.bg_library_badge_vip else R.drawable.bg_library_badge_free,
            )
        }
        return resultView
    }

    companion object {
        private const val ARG_QUERY = "query"

        fun newInstance(query: String): LibrarySearchFragment =
            LibrarySearchFragment().apply {
                arguments = Bundle().apply { putString(ARG_QUERY, query) }
            }
    }
}
