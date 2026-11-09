package com.tangkinhcac.app.feature.community

import org.junit.Assert.assertEquals
import org.junit.Test

class CommunityPostAudienceTest {
    @Test
    fun `only public and private audiences are available`() {
        assertEquals(
            listOf(CommunityPostAudience.PUBLIC, CommunityPostAudience.PRIVATE),
            CommunityPostAudience.entries,
        )
    }
}
