package com.tangkinhcac.app.feature.library

import com.tangkinhcac.app.R
import java.text.Normalizer
import java.util.Locale

internal data class LibrarySampleBook(
    val title: String,
    val author: String,
    val coverResId: Int,
    val rating: String,
    val reviewCount: String,
    val isVip: Boolean,
)

internal object LibrarySearchCatalog {
    val books =
        listOf(
            LibrarySampleBook(
                "Tây Du Ký",
                "Ngô Thừa Ân",
                R.drawable.cover_tay_du_ky,
                "4.8",
                "856",
                true,
            ),
            LibrarySampleBook(
                "Kim Ngao Tân Thoại",
                "Kim Thời Tập",
                R.drawable.cover_kim_ngao_tan_thoai,
                "4.7",
                "316",
                false,
            ),
            LibrarySampleBook(
                "Truyền Kỳ Mạn Lục",
                "Nguyễn Dữ",
                R.drawable.cover_truyen_ky_man_luc,
                "4.9",
                "1.1k",
                false,
            ),
            LibrarySampleBook(
                "Truyện Kiều",
                "Nguyễn Du",
                R.drawable.cover_truyen_kieu,
                "4.9",
                "1.2k",
                false,
            ),
            LibrarySampleBook(
                "Hoàng Lê Nhất Thống Chí",
                "Ngô Gia Văn Phái",
                R.drawable.cover_hoang_le_nhat_thong_chi,
                "4.8",
                "742",
                false,
            ),
            LibrarySampleBook(
                "Tam Quốc Diễn Nghĩa",
                "La Quán Trung",
                R.drawable.cover_tam_quoc_dien_nghia,
                "4.9",
                "2.8k",
                true,
            ),
            LibrarySampleBook(
                "Thủy Hử",
                "Thi Nại Am",
                R.drawable.cover_thuy_hu,
                "4.7",
                "692",
                false,
            ),
            LibrarySampleBook(
                "Hồng Cát Đồng Truyện",
                "Hứa Quân",
                R.drawable.cover_hong_cat_dong_truyen,
                "4.6",
                "284",
                true,
            ),
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
