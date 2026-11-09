package com.tangkinhcac.app.feature.community

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.tangkinhcac.app.R

internal data class CommunityPost(
    val id: String,
    @param:StringRes val authorResId: Int,
    @param:StringRes val timeResId: Int,
    @param:StringRes val contentResId: Int,
    @param:DrawableRes val avatarResId: Int,
    val likeCount: Int,
    val commentCount: Int,
    val repostCount: Int,
    @param:StringRes val highlightResId: Int? = null,
    @param:DrawableRes val imageResId: Int? = null,
    @param:StringRes val bookTagResId: Int? = null,
    @param:StringRes val requestTagResId: Int? = null,
)

internal data class CommunityComment(
    @param:StringRes val authorResId: Int,
    @param:StringRes val timeResId: Int,
    @param:StringRes val contentResId: Int,
    @param:DrawableRes val avatarResId: Int,
    val likeCount: Int,
)

internal object CommunityFeedCatalog {
    val posts =
        listOf(
            CommunityPost(
                id = "minh-quan-tam-quoc",
                authorResId = R.string.community_author_minh_quan,
                timeResId = R.string.community_time_two_hours,
                contentResId = R.string.community_post_minh_quan,
                highlightResId = R.string.community_topic_tam_quoc,
                avatarResId = R.drawable.community_avatar_minh_quan,
                likeCount = 128,
                commentCount = 42,
                repostCount = 6,
            ),
            CommunityPost(
                id = "ha-lien-truyen-kieu",
                authorResId = R.string.community_author_ha_lien,
                timeResId = R.string.community_time_five_hours,
                contentResId = R.string.community_post_ha_lien,
                avatarResId = R.drawable.community_avatar_ha_lien,
                imageResId = R.drawable.community_post_truyen_kieu,
                likeCount = 256,
                commentCount = 18,
                repostCount = 12,
            ),
            CommunityPost(
                id = "trong-hieu-tay-du-ky",
                authorResId = R.string.community_author_trong_hieu,
                timeResId = R.string.community_time_eight_hours,
                contentResId = R.string.community_post_trong_hieu,
                avatarResId = R.drawable.community_avatar_trong_hieu,
                bookTagResId = R.string.community_book_tay_du_ky,
                requestTagResId = R.string.community_request_suggestion,
                likeCount = 64,
                commentCount = 37,
                repostCount = 3,
            ),
        )

    val comments =
        listOf(
            CommunityComment(
                authorResId = R.string.community_author_ha_lien,
                timeResId = R.string.community_time_one_hour,
                contentResId = R.string.community_comment_ha_lien,
                avatarResId = R.drawable.community_avatar_ha_lien,
                likeCount = 37,
            ),
            CommunityComment(
                authorResId = R.string.community_author_trong_hieu,
                timeResId = R.string.community_time_fifty_eight_minutes,
                contentResId = R.string.community_comment_trong_hieu,
                avatarResId = R.drawable.community_avatar_trong_hieu,
                likeCount = 21,
            ),
            CommunityComment(
                authorResId = R.string.community_author_mai_anh,
                timeResId = R.string.community_time_forty_five_minutes,
                contentResId = R.string.community_comment_mai_anh,
                avatarResId = R.drawable.community_avatar_ha_lien,
                likeCount = 15,
            ),
        )

    fun findPost(id: String?): CommunityPost = posts.firstOrNull { it.id == id } ?: posts.first()
}
