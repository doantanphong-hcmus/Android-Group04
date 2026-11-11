package com.tangkinhcac.app.feature.rankings

import com.tangkinhcac.app.feature.library.LibrarySearchCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RankingsMockDataTest {
    @Test
    fun `every ranking contains each supplied book once in descending order`() {
        RankingsPeriod.entries.forEach { period ->
            RankingsMetric.entries.forEach { metric ->
                val ranking = RankingsMockData.rankings(period, metric)

                assertEquals(LibrarySearchCatalog.books.map { it.id }.toSet(), ranking.map { it.bookId }.toSet())
                assertEquals(ranking.size, ranking.map { it.bookId }.distinct().size)
                assertTrue(ranking.zipWithNext().all { (current, next) -> current.count >= next.count })
            }
        }
    }
}
