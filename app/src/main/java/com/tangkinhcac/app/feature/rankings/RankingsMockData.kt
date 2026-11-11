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
    private data class BookScore(
        val bookId: String,
        val readCount: Int,
        val listenCount: Int,
    )

    private val scores =
        listOf(
            BookScore("truyen-kieu", readCount = 12_400, listenCount = 3_800),
            BookScore("tam-quoc-dien-nghia", readCount = 10_800, listenCount = 4_100),
            BookScore("tay-du-ky", readCount = 9_700, listenCount = 3_600),
            BookScore("truyen-ky-man-luc", readCount = 8_900, listenCount = 2_700),
            BookScore("thuy-hu", readCount = 8_100, listenCount = 3_200),
            BookScore("hoang-le-nhat-thong-chi", readCount = 6_900, listenCount = 2_400),
            BookScore("kim-ngao-tan-thoai", readCount = 6_300, listenCount = 2_200),
            BookScore("hong-cat-dong-truyen", readCount = 5_800, listenCount = 2_100),
        )

    fun rankings(
        period: RankingsPeriod,
        metric: RankingsMetric,
    ): List<RankingEntry> =
        scores
            .map { score ->
                val baseCount =
                    when (metric) {
                        RankingsMetric.READ -> score.readCount
                        RankingsMetric.LISTEN -> score.listenCount
                    }
                RankingEntry(
                    bookId = score.bookId,
                    count = (baseCount * period.multiplier).roundToInt(),
                )
            }.sortedByDescending(RankingEntry::count)
}
