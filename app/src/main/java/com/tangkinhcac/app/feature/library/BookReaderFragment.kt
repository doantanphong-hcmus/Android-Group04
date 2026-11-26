package com.tangkinhcac.app.feature.library

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tangkinhcac.app.R
import com.tangkinhcac.app.TangKinhCacApplication
import com.tangkinhcac.app.data.repository.ReaderChapter
import com.tangkinhcac.app.data.repository.ReaderFailure
import com.tangkinhcac.app.ui.applySystemBarPadding
import kotlinx.coroutines.launch

class BookReaderFragment : Fragment(R.layout.fragment_book_reader) {
    private val bookId get() = requireArguments().getString(ARG_BOOK_ID).orEmpty()
    private val bookTitle get() = requireArguments().getString(ARG_BOOK_TITLE).orEmpty()
    private val viewModel: BookReaderViewModel by viewModels {
        val application = requireActivity().application as TangKinhCacApplication
        BookReaderViewModel.factory(application.bookReaderRepository, bookId)
    }
    private val preferences by lazy {
        requireContext().getSharedPreferences("reader_preferences", Context.MODE_PRIVATE)
    }
    private var chapters: List<ReaderChapter> = emptyList()
    private var chapterIndex = 0
    private var endPage = false
    private var fontSize = 20
    private var wideSpacing = false
    private var readerTheme = 1

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setBottomNavigationVisible(false)
        view.findViewById<View>(R.id.reader_root).applySystemBarPadding()
        view.findViewById<TextView>(R.id.reader_book_title).text = bookTitle
        view.findViewById<TextView>(R.id.reader_end_title).text = getString(R.string.reader_end_of_book, bookTitle)
        fontSize = preferences.getInt("font_size", 20)
        wideSpacing = preferences.getBoolean("wide_spacing", false)
        readerTheme = preferences.getInt("theme", 1)
        chapterIndex = preferences.getInt("$bookId.chapter", 0)
        applyReadingStyle(view)

        view.findViewById<ImageButton>(R.id.reader_back).setOnClickListener { parentFragmentManager.popBackStack() }
        view.findViewById<View>(R.id.reader_error_back).setOnClickListener { parentFragmentManager.popBackStack() }
        view.findViewById<View>(R.id.reader_retry).setOnClickListener { viewModel.retry() }
        view.findViewById<View>(R.id.reader_settings).setOnClickListener { showSettings() }
        view.findViewById<View>(R.id.reader_contents).setOnClickListener { showContents() }
        view.findViewById<View>(R.id.reader_listen).setOnClickListener {
            Toast.makeText(requireContext(), R.string.book_detail_content_unavailable, Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.reader_book_details).setOnClickListener { parentFragmentManager.popBackStack() }
        view.findViewById<View>(R.id.reader_rate_book).setOnClickListener {
            val fragment = BookReviewsFragment.newInstance(bookId, true)
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.main_content_container, fragment)
                .setPrimaryNavigationFragment(fragment)
                .addToBackStack(null)
                .commit()
        }
        val readerScroll = view.findViewById<ReaderScrollView>(R.id.reader_scroll)
        readerScroll.setOnClickListener { toggleControls() }
        readerScroll.onPageTurn = { direction -> turnPage(readerScroll, direction) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    view.findViewById<View>(R.id.reader_loading).isVisible = state == ReaderState.Loading
                    view.findViewById<View>(R.id.reader_error).isVisible = state is ReaderState.Error
                    view.findViewById<View>(R.id.reader_screen).isVisible = state is ReaderState.Ready
                    if (state is ReaderState.Error) {
                        val message =
                            when (state.reason) {
                                ReaderFailure.SIGN_IN -> R.string.reader_error_sign_in
                                ReaderFailure.UNAVAILABLE -> R.string.reader_error_unavailable
                                ReaderFailure.ACCESS_DENIED -> R.string.reader_error_access_denied
                                ReaderFailure.NETWORK -> R.string.reader_error_message
                                ReaderFailure.INVALID_FILE -> R.string.reader_error_invalid_file
                                ReaderFailure.SERVER -> R.string.reader_error_server
                            }
                        view.findViewById<TextView>(R.id.reader_error_message).setText(message)
                    }
                    if (state is ReaderState.Ready && chapters !== state.chapters) {
                        chapters = state.chapters
                        chapterIndex = chapterIndex.coerceIn(chapters.indices)
                        showChapter(restorePosition = true)
                    }
                }
            }
        }
    }

    override fun onPause() {
        if (chapters.isNotEmpty()) {
            preferences.edit {
                putInt("$bookId.chapter", chapterIndex)
                putInt("$bookId.scroll", requireView().findViewById<ScrollView>(R.id.reader_scroll).scrollY)
            }
        }
        super.onPause()
    }

    override fun onDestroyView() {
        val window = requireActivity().window
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        setBottomNavigationVisible(parentFragmentManager.backStackEntryCount == 0)
        super.onDestroyView()
    }

    private fun showChapter(restorePosition: Boolean = false) {
        val view = requireView()
        endPage = false
        view.findViewById<LinearLayout>(R.id.reader_content).gravity = Gravity.CENTER_HORIZONTAL
        view.findViewById<View>(R.id.reader_toolbar).isVisible = true
        view.findViewById<View>(R.id.reader_toolbar_divider).isVisible = true
        view.findViewById<View>(R.id.reader_footer).isVisible = true
        view.findViewById<View>(R.id.reader_chapter_title).isVisible = true
        view.findViewById<View>(R.id.reader_chapter_text).isVisible = true
        view.findViewById<View>(R.id.reader_end_actions).isVisible = false
        view.findViewById<TextView>(R.id.reader_chapter_title).text = chapters[chapterIndex].title
        view.findViewById<TextView>(R.id.reader_chapter_text).text = chapters[chapterIndex].text
        view.findViewById<View>(R.id.reader_listen).isVisible = true
        val scroll = view.findViewById<ScrollView>(R.id.reader_scroll)
        val position = if (restorePosition) preferences.getInt("$bookId.scroll", 0) else 0
        scroll.post { scroll.scrollTo(0, position) }
        if (restorePosition && position > 0) {
            Toast.makeText(requireContext(), R.string.reader_resumed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun nextChapter() {
        if (chapters.isEmpty() || endPage) return
        if (chapterIndex < chapters.lastIndex) {
            chapterIndex++
            showChapter()
        } else {
            endPage = true
            requireView().findViewById<LinearLayout>(R.id.reader_content).gravity = Gravity.CENTER
            requireView().findViewById<View>(R.id.reader_chapter_title).isVisible = false
            requireView().findViewById<View>(R.id.reader_chapter_text).isVisible = false
            requireView().findViewById<View>(R.id.reader_end_actions).isVisible = true
            requireView().findViewById<ScrollView>(R.id.reader_scroll).scrollTo(0, 0)
            requireView().findViewById<View>(R.id.reader_listen).isVisible = false
            requireView().findViewById<View>(R.id.reader_toolbar).isVisible = false
            requireView().findViewById<View>(R.id.reader_toolbar_divider).isVisible = false
            requireView().findViewById<View>(R.id.reader_footer).isVisible = false
        }
    }

    private fun turnPage(
        scroll: ScrollView,
        direction: Int,
    ) {
        if (direction > 0) {
            if (scroll.canScrollVertically(1)) {
                scroll.smoothScrollBy(0, scroll.height)
            } else {
                nextChapter()
            }
        } else if (scroll.canScrollVertically(-1)) {
            scroll.smoothScrollBy(0, -scroll.height)
        } else if (endPage) {
            showChapter()
            scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
        } else if (chapterIndex > 0) {
            chapterIndex--
            showChapter()
            scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun toggleControls() {
        val view = requireView()
        val visible = !view.findViewById<View>(R.id.reader_toolbar).isVisible
        view.findViewById<View>(R.id.reader_toolbar).isVisible = visible
        view.findViewById<View>(R.id.reader_toolbar_divider).isVisible = visible
        view.findViewById<View>(R.id.reader_footer).isVisible = visible
    }

    private fun showContents() {
        val dialog = BottomSheetDialog(requireContext())
        val list = sheetColumn()
        list.addView(sheetTitle(getString(R.string.reader_contents_title)))
        chapters.forEachIndexed { index, chapter ->
            val row = sheetButton(chapter.title)
            if (index == chapterIndex) {
                row.setTextColor(Color.rgb(169, 65, 42))
                row.background = choiceBackground(true)
                row.text =
                    getString(
                        R.string.reader_current_chapter_format,
                        chapter.title,
                        getString(R.string.reader_current_chapter),
                    )
            }
            row.setOnClickListener {
                chapterIndex = index
                showChapter()
                dialog.dismiss()
            }
            list.addView(row)
            if (index != chapters.lastIndex) {
                list.addView(
                    View(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1.dp)
                        setBackgroundColor(Color.rgb(226, 206, 183))
                    },
                )
            }
        }
        dialog.setContentView(ScrollView(requireContext()).apply { addView(list) })
        dialog.show()
    }

    private fun showSettings() {
        val dialog = BottomSheetDialog(requireContext())
        val column = sheetColumn()
        column.addView(sheetTitle(getString(R.string.reader_settings)))
        val sizeRow = sheetRow()
        sizeRow.addView(sheetLabel(getString(R.string.reader_font_size)), LinearLayout.LayoutParams(0, 48.dp, 1f))
        val smaller = sheetButton("−")
        val sizeValue = sheetLabel(fontSize.toString())
        val larger = sheetButton("+")
        listOf(smaller, sizeValue, larger).forEach { sizeRow.addView(it, LinearLayout.LayoutParams(48.dp, 48.dp)) }
        smaller.setOnClickListener {
            fontSize = (fontSize - 1).coerceAtLeast(14)
            sizeValue.text = "$fontSize"
            saveStyle()
        }
        larger.setOnClickListener {
            fontSize = (fontSize + 1).coerceAtMost(30)
            sizeValue.text = "$fontSize"
            saveStyle()
        }
        column.addView(sizeRow)

        val spacingRow = sheetRow()
        spacingRow.addView(sheetLabel(getString(R.string.reader_line_spacing)), LinearLayout.LayoutParams(0, 48.dp, 1f))
        val normal = sheetButton(getString(R.string.reader_spacing_normal))
        val wide = sheetButton(getString(R.string.reader_spacing_wide))
        normal.background = choiceBackground(!wideSpacing)
        wide.background = choiceBackground(wideSpacing)
        spacingRow.addView(normal)
        spacingRow.addView(wide)
        normal.setOnClickListener {
            wideSpacing = false
            saveStyle()
            dialog.dismiss()
            showSettings()
        }
        wide.setOnClickListener {
            wideSpacing = true
            saveStyle()
            dialog.dismiss()
            showSettings()
        }
        column.addView(spacingRow)

        val themeRow = sheetRow()
        listOf(R.string.reader_theme_light, R.string.reader_theme_sepia, R.string.reader_theme_dark)
            .forEachIndexed { index, label ->
                val button = sheetButton(getString(label))
                button.background = choiceBackground(index == readerTheme, index == 2)
                if (index == 2) button.setTextColor(Color.WHITE)
                button.setOnClickListener {
                    readerTheme = index
                    saveStyle()
                    dialog.dismiss()
                    showSettings()
                }
                themeRow.addView(button, LinearLayout.LayoutParams(0, 56.dp, 1f))
            }
        column.addView(themeRow)
        dialog.setContentView(column)
        dialog.show()
    }

    private fun saveStyle() {
        preferences.edit {
            putInt("font_size", fontSize)
            putBoolean("wide_spacing", wideSpacing)
            putInt("theme", readerTheme)
        }
        applyReadingStyle(requireView())
    }

    private fun applyReadingStyle(view: View) {
        val background =
            when (readerTheme) {
                0 -> Color.rgb(255, 251, 242)
                2 -> Color.rgb(49, 27, 19)
                else -> Color.rgb(251, 239, 218)
            }
        val foreground = if (readerTheme == 2) Color.rgb(255, 244, 227) else Color.rgb(54, 36, 27)
        view.findViewById<View>(R.id.reader_root).background =
            GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(background, if (readerTheme == 2) Color.rgb(62, 35, 25) else Color.rgb(255, 248, 232)),
            ).apply {
                gradientType = GradientDrawable.RADIAL_GRADIENT
                gradientRadius = resources.displayMetrics.widthPixels.toFloat()
            }
        val window = requireActivity().window
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = readerTheme != 2
        listOf(
            R.id.reader_book_title,
            R.id.reader_settings,
            R.id.reader_chapter_title,
            R.id.reader_chapter_text,
            R.id.reader_contents,
            R.id.reader_listen,
            R.id.reader_end_title,
            R.id.reader_loading_message,
            R.id.reader_error_title,
            R.id.reader_error_message,
        ).forEach { view.findViewById<TextView>(it).setTextColor(foreground) }
        view.findViewById<ImageButton>(R.id.reader_back).imageTintList = ColorStateList.valueOf(foreground)
        view.findViewById<TextView>(R.id.reader_chapter_text).apply {
            textSize = fontSize.toFloat()
            setLineSpacing(if (wideSpacing) 22.dp.toFloat() else 14.dp.toFloat(), 1f)
        }
    }

    private fun sheetColumn() =
        LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22.dp, 22.dp, 22.dp, 28.dp)
            background =
                GradientDrawable().apply {
                    setColor(Color.rgb(251, 239, 218))
                    cornerRadii =
                        floatArrayOf(22.dp.toFloat(), 22.dp.toFloat(), 22.dp.toFloat(), 22.dp.toFloat(), 0f, 0f, 0f, 0f)
                }
        }

    private fun sheetRow() =
        LinearLayout(requireContext()).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }

    private fun sheetTitle(text: String) =
        sheetLabel(text).apply {
            gravity = Gravity.CENTER
            textSize = 25f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 0, 0, 18.dp)
        }

    private fun sheetLabel(text: String) =
        TextView(requireContext()).apply {
            this.text = text
            textSize = 17f
            gravity = Gravity.CENTER_VERTICAL
            typeface = Typeface.SERIF
            setTextColor(Color.rgb(54, 36, 27))
        }

    private fun sheetButton(text: String) =
        sheetLabel(text).apply {
            gravity = Gravity.CENTER
            minHeight = 55.dp
            setPadding(10.dp, 8.dp, 10.dp, 8.dp)
        }

    private fun choiceBackground(
        selected: Boolean,
        dark: Boolean = false,
    ) = GradientDrawable().apply {
        cornerRadius = 9.dp.toFloat()
        setColor(if (dark) Color.rgb(49, 27, 19) else Color.TRANSPARENT)
        setStroke(1.dp, if (selected) Color.rgb(169, 65, 42) else Color.rgb(206, 183, 159))
    }

    private val Int.dp get() = (this * resources.displayMetrics.density).toInt()

    private fun setBottomNavigationVisible(visible: Boolean) {
        parentFragment?.view?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.isVisible = visible
    }

    companion object {
        private const val ARG_BOOK_ID = "book_id"
        private const val ARG_BOOK_TITLE = "book_title"

        fun newInstance(
            bookId: String,
            title: String,
        ) = BookReaderFragment().apply {
            arguments =
                Bundle().apply {
                    putString(ARG_BOOK_ID, bookId)
                    putString(ARG_BOOK_TITLE, title)
                }
        }
    }
}
