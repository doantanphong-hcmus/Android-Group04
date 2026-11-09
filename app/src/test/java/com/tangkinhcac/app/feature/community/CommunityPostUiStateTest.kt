package com.tangkinhcac.app.feature.community

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityPostUiStateTest {
    @Test
    fun `actions toggle state and counters locally`() {
        val state = CommunityPostUiState(initialLikeCount = 10, initialRepostCount = 4)

        state.toggleLike()
        state.toggleRepost()
        state.toggleBookmark()

        assertTrue(state.isLiked)
        assertTrue(state.isReposted)
        assertTrue(state.isBookmarked)
        assertEquals(11, state.likeCount)
        assertEquals(5, state.repostCount)

        state.toggleLike()
        state.toggleRepost()
        state.toggleBookmark()

        assertFalse(state.isLiked)
        assertFalse(state.isReposted)
        assertFalse(state.isBookmarked)
        assertEquals(10, state.likeCount)
        assertEquals(4, state.repostCount)
    }
}
