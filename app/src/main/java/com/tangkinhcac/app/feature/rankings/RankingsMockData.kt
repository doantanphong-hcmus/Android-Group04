package com.tangkinhcac.app.feature.rankings

import kotlin.math.roundToInt

internal enum class RankingsPeriod(
    val multiplier: Double,
) {
    DAY(0.08),
    WEEK(0.35),
    MONTH(1.0),
}

internal enum class RankingsMetric {
    READ,
    LISTEN,
}

internal data class RankingEntry(
    val bookId: String,
    val count: Int,
)

internal object RankingsMockData {
    private val readOrder =
        listOf(
            "truyen-kieu",
            "tam-quoc-dien-nghia",
            "tay-du-ky",
            "truyen-ky-man-luc",
            "thuy-hu",
            "hoang-le-nhat-thong-chi",
            "kim-ngao-tan-thoai",
            "hong-cat-dong-truyen",
        )
    private val listenOrder =
        listOf(
            "tam-quoc-dien-nghia",
            "truyen-kieu",
            "tay-du-ky",
            "thuy-hu",
            "truyen-ky-man-luc",
            "hoang-le-nhat-thong-chi",
            "kim-ngao-tan-thoai",
            "hong-cat-dong-truyen",
        )

    fun rankings(
        period: RankingsPeriod,
        metric: RankingsMetric,
    ): List<RankingEntry> {
        val bookOrder = if (metric == RankingsMetric.READ) readOrder else listenOrder
        val startingCount = if (metric == RankingsMetric.READ) 12_400 else 4_100
        val countStep = if (metric == RankingsMetric.READ) 420 else 120

        // shortcut: cycle eight supplied books to exercise 20 rows, stop cycling when the catalog has 20 titles.
        return List(RANKING_SIZE) { index ->
            RankingEntry(
                bookId = bookOrder[index % bookOrder.size],
                count = ((startingCount - index * countStep) * period.multiplier).roundToInt(),
            )
        }
    }

    private const val RANKING_SIZE = 20
}
