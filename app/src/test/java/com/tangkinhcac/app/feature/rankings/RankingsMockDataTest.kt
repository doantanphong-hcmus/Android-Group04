package com.tangkinhcac.app.feature.rankings

import com.tangkinhcac.app.feature.library.LibrarySearchCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RankingsMockDataTest {
    @Test
    fun `every ranking contains twenty valid entries in descending order`() {
        RankingsPeriod.entries.forEach { period ->
            RankingsMetric.entries.forEach { metric ->
                val ranking = RankingsMockData.rankings(period, metric)
                val suppliedBookIds = LibrarySearchCatalog.books.map { it.id }.toSet()

                assertEquals(20, ranking.size)
                assertEquals(suppliedBookIds, ranking.map { it.bookId }.toSet())
                assertTrue(ranking.zipWithNext().all { (current, next) -> current.count >= next.count })
            }
        }
    }
}
