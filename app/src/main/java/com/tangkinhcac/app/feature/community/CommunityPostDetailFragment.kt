package com.tangkinhcac.app.feature.community

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.imageview.ShapeableImageView
import com.tangkinhcac.app.R
import com.tangkinhcac.app.ui.applySystemBarPadding

class CommunityPostDetailFragment : Fragment(R.layout.fragment_community_post_detail) {
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        view.applySystemBarPadding()

        val post = CommunityFeedCatalog.findPost(arguments?.getString(ARG_POST_ID))
        view.findViewById<View>(R.id.community_detail_back).setOnClickListener {
            findNavController().navigateUp()
        }
        view.findViewById<ShapeableImageView>(R.id.community_detail_avatar).setImageResource(post.avatarResId)
        view.findViewById<ShapeableImageView>(R.id.community_detail_input_avatar).setImageResource(post.avatarResId)
        view.findViewById<TextView>(R.id.community_detail_author).setText(post.authorResId)
        view.findViewById<TextView>(R.id.community_detail_time).setText(post.timeResId)
        view.findViewById<TextView>(R.id.community_detail_content).text = post.styledContent()
        view.findViewById<TextView>(R.id.community_detail_like_count).text = formattedCount(post.likeCount)
        view.findViewById<TextView>(R.id.community_detail_comment_count).text = formattedCount(post.commentCount)
        view.findViewById<TextView>(R.id.community_detail_repost_count).text = formattedCount(post.repostCount)
        view.findViewById<TextView>(R.id.community_detail_comment_title).text =
            getString(R.string.community_comments_count, post.commentCount)

        val commentsContainer = view.findViewById<LinearLayout>(R.id.community_comments_container)
        CommunityFeedCatalog.comments.forEach { comment ->
            commentsContainer.addView(createCommentView(commentsContainer, comment))
        }
    }

    private fun createCommentView(
        parent: LinearLayout,
        comment: CommunityComment,
    ): View =
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_community_comment, parent, false)
            .apply {
                findViewById<ShapeableImageView>(R.id.community_comment_avatar).setImageResource(comment.avatarResId)
                findViewById<TextView>(R.id.community_comment_author).setText(comment.authorResId)
                findViewById<TextView>(R.id.community_comment_time).setText(comment.timeResId)
                findViewById<TextView>(R.id.community_comment_content).setText(comment.contentResId)
                findViewById<TextView>(R.id.community_comment_like_count).text = formattedCount(comment.likeCount)
            }

    private fun CommunityPost.styledContent(): CharSequence {
        val content = SpannableStringBuilder(getString(contentResId))
        val highlight = highlightResId?.let(::getString) ?: return content
        content.append('\n').append(highlight)
        content.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.community_accent)),
            content.length - highlight.length,
            content.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        return content
    }

    private fun formattedCount(count: Int): String = getString(R.string.community_count_value, count)

    companion object {
        const val ARG_POST_ID = "post_id"
    }
}
