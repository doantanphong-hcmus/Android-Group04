package com.tangkinhcac.app.feature.community

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tangkinhcac.app.R
import com.tangkinhcac.app.ui.applySystemBarPadding

class CommunityCreatePostFragment : Fragment(R.layout.fragment_community_create_post) {
    private var audience = CommunityPostAudience.PUBLIC
    private var selectedImageUri: Uri? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let(::showSelectedImage)
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        view.applySystemBarPadding()

        view.findViewById<View>(R.id.community_create_back).setOnClickListener {
            findNavController().navigateUp()
        }
        view.findViewById<View>(R.id.community_create_photo_option).setOnClickListener {
            imagePicker.launch("image/*")
        }
        view.findViewById<View>(R.id.community_create_remove_photo).setOnClickListener {
            clearSelectedImage()
        }
        view.findViewById<View>(R.id.community_create_audience_option).setOnClickListener {
            showAudiencePicker()
        }
        view.findViewById<TextView>(R.id.community_create_content).doAfterTextChanged {
            updatePreviewVisibility()
        }
    }

    private fun showSelectedImage(uri: Uri) {
        selectedImageUri = uri
        requireView().findViewById<ImageView>(R.id.community_create_photo_preview).setImageURI(uri)
        requireView().findViewById<View>(R.id.community_create_photo_container).isVisible = true
        requireView().findViewById<TextView>(R.id.community_create_photo_value).setText(
            R.string.community_create_photo_added,
        )
        updatePreviewVisibility()
    }

    private fun clearSelectedImage() {
        selectedImageUri = null
        requireView().findViewById<View>(R.id.community_create_photo_container).isVisible = false
        requireView().findViewById<TextView>(R.id.community_create_photo_value).setText(
            R.string.community_create_add_photo,
        )
        updatePreviewVisibility()
    }

    private fun showAudiencePicker() {
        val options = CommunityPostAudience.entries
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.community_create_audience)
            .setSingleChoiceItems(
                options.map { getString(it.labelResId) }.toTypedArray(),
                options.indexOf(audience),
            ) { dialog, index ->
                audience = options[index]
                requireView().findViewById<TextView>(R.id.community_create_audience_value).setText(
                    audience.labelResId,
                )
                dialog.dismiss()
            }.show()
    }

    private fun updatePreviewVisibility() {
        val content = requireView().findViewById<TextView>(R.id.community_create_content).text
        requireView().findViewById<View>(R.id.community_create_preview).isVisible =
            content.isNotBlank() ||
            selectedImageUri != null
    }
}
