package com.tangkinhcac.app.feature.library

import org.junit.Assert.assertEquals
import org.junit.Test

class BookReviewRatingTest {
    @Test
    fun `stars reflect submitted rating and clamp invalid values`() {
        assertEquals("☆☆☆☆☆", reviewStars(-1))
        assertEquals("★★★☆☆", reviewStars(3))
        assertEquals("★★★★★", reviewStars(6))
    }
}
