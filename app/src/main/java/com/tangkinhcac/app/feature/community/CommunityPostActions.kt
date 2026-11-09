package com.tangkinhcac.app.feature.community

import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import com.tangkinhcac.app.R

internal class CommunityPostUiState(
    initialLikeCount: Int,
    initialRepostCount: Int,
) {
    var isLiked = false
        private set
    var isReposted = false
        private set
    var isBookmarked = false
        private set
    var likeCount = initialLikeCount
        private set
    var repostCount = initialRepostCount
        private set

    fun toggleLike() {
        isLiked = !isLiked
        likeCount += if (isLiked) 1 else -1
    }

    fun toggleRepost() {
        isReposted = !isReposted
        repostCount += if (isReposted) 1 else -1
    }

    fun toggleBookmark() {
        isBookmarked = !isBookmarked
    }
}

private object CommunityPostUiStateStore {
    private val states = mutableMapOf<String, CommunityPostUiState>()

    fun stateFor(post: CommunityPost): CommunityPostUiState =
        states.getOrPut(post.id) {
            CommunityPostUiState(post.likeCount, post.repostCount)
        }
}

internal fun View.bindCommunityPostActions(
    post: CommunityPost,
    @IdRes likeViewId: Int,
    @IdRes repostViewId: Int,
    @IdRes bookmarkViewId: Int,
) {
    val state = CommunityPostUiStateStore.stateFor(post)
    val likeView = findViewById<TextView>(likeViewId)
    val repostView = findViewById<TextView>(repostViewId)
    val bookmarkView = findViewById<ImageView>(bookmarkViewId)

    fun render() {
        likeView.text = resources.getString(R.string.community_count_value, state.likeCount)
        likeView.setTextColor(
            ContextCompat.getColor(
                context,
                if (state.isLiked) R.color.community_like_active else R.color.community_text_primary,
            ),
        )
        likeView.setCompoundDrawablesRelativeWithIntrinsicBounds(
            if (state.isLiked) R.drawable.ic_community_like_active else R.drawable.ic_community_like,
            0,
            0,
            0,
        )
        likeView.setContentDescription(
            resources.getString(
                if (state.isLiked) R.string.community_action_unlike else R.string.community_action_like,
            ),
        )

        repostView.text = resources.getString(R.string.community_count_value, state.repostCount)
        repostView.setTextColor(
            ContextCompat.getColor(
                context,
                if (state.isReposted) R.color.community_repost_active else R.color.community_text_primary,
            ),
        )
        repostView.setCompoundDrawablesRelativeWithIntrinsicBounds(
            if (state.isReposted) R.drawable.ic_community_repost_active else R.drawable.ic_community_repost,
            0,
            0,
            0,
        )
        repostView.setContentDescription(
            resources.getString(
                if (state.isReposted) R.string.community_action_undo_repost else R.string.community_action_repost,
            ),
        )

        bookmarkView.setImageResource(
            if (state.isBookmarked) R.drawable.ic_community_bookmark_active else R.drawable.ic_community_bookmark,
        )
        bookmarkView.setContentDescription(
            resources.getString(
                if (state.isBookmarked) {
                    R.string.community_action_remove_bookmark
                } else {
                    R.string.community_action_bookmark
                },
            ),
        )
    }

    likeView.setOnClickListener {
        state.toggleLike()
        likeView.playCommunityToggleAnimation()
        render()
    }
    repostView.setOnClickListener {
        state.toggleRepost()
        repostView.playCommunityToggleAnimation()
        render()
    }
    bookmarkView.setOnClickListener {
        state.toggleBookmark()
        bookmarkView.playCommunityToggleAnimation()
        render()
    }
    render()
}

private fun View.playCommunityToggleAnimation() {
    animate().cancel()
    scaleX = 0.82f
    scaleY = 0.82f
    animate()
        .scaleX(1f)
        .scaleY(1f)
        .setDuration(220L)
        .setInterpolator(OvershootInterpolator(2f))
        .start()
}
