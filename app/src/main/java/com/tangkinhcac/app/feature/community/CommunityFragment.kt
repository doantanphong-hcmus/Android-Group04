package com.tangkinhcac.app.feature.community

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.imageview.ShapeableImageView
import com.tangkinhcac.app.R

class CommunityFragment : Fragment(R.layout.fragment_community) {
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val postsContainer = view.findViewById<LinearLayout>(R.id.community_posts_container)
        CommunityFeedCatalog.posts.forEach { post ->
            postsContainer.addView(createPostView(postsContainer, post))
        }
    }

    private fun createPostView(
        parent: LinearLayout,
        post: CommunityPost,
    ): View {
        val postView =
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_community_post, parent, false)

        postView.findViewById<ShapeableImageView>(R.id.community_post_avatar).setImageResource(post.avatarResId)
        postView.findViewById<TextView>(R.id.community_post_author).setText(post.authorResId)
        postView.findViewById<TextView>(R.id.community_post_time).setText(post.timeResId)
        postView.findViewById<TextView>(R.id.community_post_content).text = post.styledContent()
        postView.findViewById<TextView>(R.id.community_post_like_count).text = formattedCount(post.likeCount)
        postView.findViewById<TextView>(R.id.community_post_comment_count).text = formattedCount(post.commentCount)
        postView.findViewById<TextView>(R.id.community_post_repost_count).text = formattedCount(post.repostCount)

        postView.findViewById<ImageView>(R.id.community_post_image).apply {
            isVisible = post.imageResId != null
            post.imageResId?.let(::setImageResource)
        }
        postView.findViewById<LinearLayout>(R.id.community_post_tags).apply {
            isVisible = post.bookTagResId != null || post.requestTagResId != null
        }
        postView.findViewById<TextView>(R.id.community_post_book_tag).apply {
            isVisible = post.bookTagResId != null
            post.bookTagResId?.let(::setText)
        }
        postView.findViewById<TextView>(R.id.community_post_request_tag).apply {
            isVisible = post.requestTagResId != null
            post.requestTagResId?.let(::setText)
        }

        postView.setOnClickListener {
            requireActivity().findNavController(R.id.main_nav_host).navigate(
                R.id.communityPostDetailFragment,
                Bundle().apply { putString(CommunityPostDetailFragment.ARG_POST_ID, post.id) },
            )
        }
        return postView
    }

    private fun CommunityPost.styledContent(): CharSequence {
        val content = SpannableStringBuilder(getString(contentResId))
        val highlight = highlightResId?.let(::getString) ?: return content
        content.append(' ').append(highlight)
        content.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.community_accent)),
            content.length - highlight.length,
            content.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        return content
    }

    private fun formattedCount(count: Int): String = getString(R.string.community_count_value, count)
}
