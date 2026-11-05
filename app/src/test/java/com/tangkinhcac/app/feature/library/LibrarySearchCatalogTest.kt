package com.tangkinhcac.app.feature.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LibrarySearchCatalogTest {
    @Test
    fun `search ignores Vietnamese accents and letter case`() {
        val results = LibrarySearchCatalog.search("TAM QUOC")

        assertEquals(4, results.size)
    }

    @Test
    fun `search returns no result for an unknown title`() {
        val results = LibrarySearchCatalog.search("Dạ Hành Lục")

        assertTrue(results.isEmpty())
    }
}
