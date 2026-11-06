package com.tangkinhcac.app.feature.library

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.tangkinhcac.app.R

class LibraryFragment : Fragment(R.layout.fragment_library) {
    private var selectedCountry: String? = null
    private var selectedVip: Boolean? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        bindFilters(view)
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

    private fun bindFilters(root: View) {
        val countryFilters =
            listOf<Pair<TextView, String?>>(
                root.findViewById<TextView>(R.id.library_country_all) to null,
                root.findViewById<TextView>(R.id.library_country_vietnam) to
                    getString(R.string.library_country_vietnam),
                root.findViewById<TextView>(R.id.library_country_china) to
                    getString(R.string.library_country_china),
                root.findViewById<TextView>(R.id.library_country_japan) to
                    getString(R.string.library_country_japan),
                root.findViewById<TextView>(R.id.library_country_korea) to
                    getString(R.string.library_country_korea),
            )

        fun updateCountrySelection() {
            countryFilters.forEach { (countryChip, countryValue) ->
                val isSelected = countryValue == selectedCountry
                countryChip.isSelected = isSelected
                countryChip.setBackgroundResource(
                    if (isSelected) R.drawable.bg_library_chip_selected else R.drawable.bg_library_chip,
                )
                countryChip.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isSelected) android.R.color.white else R.color.library_text_primary,
                    ),
                )
                countryChip.setTypeface(countryChip.typeface, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
            }
        }

        countryFilters.forEach { (chip, country) ->
            chip.setOnClickListener {
                selectedCountry = country
                updateCountrySelection()
                renderBooks(root)
            }
        }

        val freeFilter = root.findViewById<View>(R.id.library_filter_free)
        val vipFilter = root.findViewById<View>(R.id.library_filter_vip)

        fun updateAccessSelection() {
            freeFilter.isSelected = selectedVip == false
            vipFilter.isSelected = selectedVip == true
            freeFilter.alpha = if (selectedVip == true) UNSELECTED_FILTER_ALPHA else 1f
            vipFilter.alpha = if (selectedVip == false) UNSELECTED_FILTER_ALPHA else 1f
        }

        listOf(freeFilter to false, vipFilter to true).forEach { (filter, isVip) ->
            filter.setOnClickListener {
                selectedVip = if (selectedVip == isVip) null else isVip
                updateAccessSelection()
                renderBooks(root)
            }
        }

        updateCountrySelection()
        updateAccessSelection()
    }

    private fun renderBooks(root: View) {
        val books = LibrarySearchCatalog.filterBooks(selectedCountry, selectedVip)
        val booksContainer = root.findViewById<LinearLayout>(R.id.library_books_container)
        booksContainer.removeAllViews()
        root.findViewById<View>(R.id.library_books_scroll).isVisible = books.isNotEmpty()
        root.findViewById<View>(R.id.library_books_empty).isVisible = books.isEmpty()

        books.forEach { book ->
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

    companion object {
        private const val UNSELECTED_FILTER_ALPHA = 0.45f
    }
}
