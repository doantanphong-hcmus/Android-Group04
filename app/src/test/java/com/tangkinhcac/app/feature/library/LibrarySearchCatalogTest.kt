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
