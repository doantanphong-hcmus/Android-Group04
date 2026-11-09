package com.tangkinhcac.app.feature.community

import androidx.annotation.StringRes
import com.tangkinhcac.app.R

enum class CommunityPostAudience(
    @param:StringRes val labelResId: Int,
) {
    PUBLIC(R.string.community_create_audience_public),
    PRIVATE(R.string.community_create_audience_private),
}
