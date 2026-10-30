package com.tangkinhcac.app.feature.auth

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.tangkinhcac.app.R
import com.tangkinhcac.app.ui.applySystemBarPadding

class RegistrationSuccessFragment : Fragment(R.layout.fragment_registration_success) {
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val content = view.findViewById<View>(R.id.registration_success_content)
        val backToLoginButton = view.findViewById<AppCompatButton>(R.id.back_to_login_button)

        content.applySystemBarPadding()
        backToLoginButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}
