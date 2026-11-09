package com.tangkinhcac.app.feature.community

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityFeedCatalogTest {
    @Test
    fun `feed contains three complete posts with unique ids`() {
        val posts = CommunityFeedCatalog.posts

        assertEquals(3, posts.size)
        assertEquals(posts.size, posts.map { it.id }.distinct().size)
        assertTrue(posts.all { it.likeCount >= 0 && it.commentCount >= 0 && it.repostCount >= 0 })
    }

    @Test
    fun `unknown post id falls back to the first post`() {
        assertEquals(CommunityFeedCatalog.posts.first(), CommunityFeedCatalog.findPost("missing"))
    }
}
