package com.tangkinhcac.app.feature.library

import java.text.Normalizer
import java.util.Locale

internal data class LibrarySampleBook(
    val title: String,
    val author: String,
    val rating: String,
    val reviewCount: String,
    val isVip: Boolean,
)

internal object LibrarySearchCatalog {
    val books =
        listOf(
            LibrarySampleBook("Tam Quốc Diễn Nghĩa", "La Quán Trung", "4.9", "2.8k", true),
            LibrarySampleBook("Tam Quốc Chí", "Trần Thọ", "4.8", "1.1k", false),
            LibrarySampleBook("Diễn Nghĩa Tam Quốc", "Mao Tôn Cương", "4.7", "368", false),
            LibrarySampleBook("Bình Giảng Tam Quốc", "Lê Đông Phương", "4.6", "210", true),
        )

    fun search(query: String): List<LibrarySampleBook> {
        val normalizedQuery = query.normalizedForSearch()
        if (normalizedQuery.isBlank()) {
            return books
        }

        return books.filter { book ->
            book.title.normalizedForSearch().contains(normalizedQuery) ||
                book.author.normalizedForSearch().contains(normalizedQuery)
        }
    }

    private fun String.normalizedForSearch(): String =
        Normalizer
            .normalize(lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(COMBINING_MARKS, "")
            .replace('đ', 'd')

    private val COMBINING_MARKS = Regex("\\p{M}+")
}
