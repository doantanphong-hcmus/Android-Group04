package com.tangkinhcac.app.feature.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LibrarySearchCatalogTest {
    @Test
    fun `search ignores Vietnamese accents and letter case`() {
        val results = LibrarySearchCatalog.search("TAM QUOC")

        assertEquals(1, results.size)
        assertEquals("Tam Quốc Diễn Nghĩa", results.single().title)
    }

    @Test
    fun `search matches alternative titles and original author names`() {
        assertEquals("Tây Du Ký", LibrarySearchCatalog.search("Journey West").single().title)
        assertEquals("Hồng Cát Đồng Truyện", LibrarySearchCatalog.search("Heo Gyun").single().title)
    }

    @Test
    fun `search matches characters genres and countries in any word order`() {
        assertEquals("Thủy Hử", LibrarySearchCatalog.search("luong son 108").single().title)
        assertEquals("Kim Ngao Tân Thoại", LibrarySearchCatalog.search("5 truyen han quoc").single().title)
    }

    @Test
    fun `catalog exposes detail-ready metadata`() {
        val taleOfKieu = LibrarySearchCatalog.books.single { it.id == "truyen-kieu" }

        assertEquals("斷腸新聲 (Đoạn trường tân thanh)", taleOfKieu.originalTitle)
        assertEquals("Tiếng Việt, chữ Nôm", taleOfKieu.originalLanguage)
        assertEquals("3.254 câu thơ lục bát", taleOfKieu.structure)
        assertTrue(taleOfKieu.synopsis.isNotBlank())
    }

    @Test
    fun `all supplied books have unique ids and complete metadata`() {
        val books = LibrarySearchCatalog.books

        assertEquals(8, books.map { it.id }.distinct().size)
        books.forEach { book ->
            assertTrue(book.title, book.originalTitle.isNotBlank())
            assertTrue(book.title, book.author.isNotBlank())
            assertTrue(book.title, book.authorOriginalName.isNotBlank())
            assertTrue(book.title, book.country.isNotBlank())
            assertTrue(book.title, book.originalLanguage.isNotBlank())
            assertTrue(book.title, book.publicationPeriod.isNotBlank())
            assertTrue(book.title, book.genres.isNotEmpty())
            assertTrue(book.title, book.structure.isNotBlank())
            assertTrue(book.title, book.historicalSetting.isNotBlank())
            assertTrue(book.title, book.synopsis.isNotBlank())
            assertTrue(book.title, book.keywords.isNotEmpty())
        }
    }

    @Test
    fun `filters books by country and access type`() {
        assertEquals(
            listOf("truyen-ky-man-luc", "truyen-kieu", "hoang-le-nhat-thong-chi"),
            LibrarySearchCatalog.filterBooks(country = "Việt Nam").map { it.id },
        )
        assertEquals(3, LibrarySearchCatalog.filterBooks(isVip = true).size)
        assertEquals(5, LibrarySearchCatalog.filterBooks(isVip = false).size)
        assertEquals(
            listOf("hong-cat-dong-truyen"),
            LibrarySearchCatalog.filterBooks(country = "Hàn Quốc", isVip = true).map { it.id },
        )
        assertTrue(LibrarySearchCatalog.filterBooks(country = "Nhật Bản").isEmpty())
    }

    @Test
    fun `search returns no result for an unknown title`() {
        val results = LibrarySearchCatalog.search("Dạ Hành Lục")

        assertTrue(results.isEmpty())
    }

    @Test
    fun `blank search only returns the supplied books`() {
        val results = LibrarySearchCatalog.search("")

        assertEquals(8, results.size)
        assertTrue(results.none { it.title.contains("placeholder", ignoreCase = true) })
    }
}
