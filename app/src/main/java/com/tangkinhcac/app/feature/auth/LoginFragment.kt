package com.tangkinhcac.app.feature.auth

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.tangkinhcac.app.R
import com.tangkinhcac.app.TangKinhCacApplication
import com.tangkinhcac.app.ui.applySystemBarPadding
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {
    private val viewModel: LoginViewModel by viewModels {
        val application = requireActivity().application as TangKinhCacApplication
        LoginViewModel.factory(application.authRepository)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val content = view.findViewById<View>(R.id.login_content)
        val emailInput = view.findViewById<TextInputLayout>(R.id.email_input)
        val passwordInput = view.findViewById<TextInputLayout>(R.id.password_input)
        val emailEditText = view.findViewById<TextInputEditText>(R.id.email_edit_text)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.password_edit_text)
        val loginButton = view.findViewById<AppCompatButton>(R.id.login_button)
        val loginProgress = view.findViewById<ProgressBar>(R.id.login_progress)
        val loginError = view.findViewById<TextView>(R.id.login_error)
        val registerLink = view.findViewById<TextView>(R.id.register_link)

        content.applySystemBarPadding()

        emailEditText.doAfterTextChanged { viewModel.clearError() }
        passwordEditText.doAfterTextChanged { viewModel.clearError() }

        loginButton.setOnClickListener {
            viewModel.signIn(
                email = emailEditText.text?.toString().orEmpty(),
                password = passwordEditText.text?.toString().orEmpty(),
            )
        }

        registerLink.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginButton.performClick()
                true
            } else {
                false
            }
        }

        // Collection stops with the Fragment view lifecycle, preventing updates to a destroyed view.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    emailInput.isEnabled = !state.isLoading
                    passwordInput.isEnabled = !state.isLoading
                    loginButton.isEnabled = !state.isLoading
                    loginButton.text =
                        if (state.isLoading) {
                            ""
                        } else {
                            getString(R.string.login_button)
                        }
                    loginProgress.isVisible = state.isLoading

                    loginError.text = state.error?.toMessage()
                    loginError.isVisible = state.error != null

                    if (
                        state.isAuthenticated &&
                        findNavController().currentDestination?.id == R.id.loginFragment
                    ) {
                        // The navigation action removes Login from the back stack after authentication.
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                }
            }
        }
    }

    private fun LoginUiState.Error.toMessage(): String =
        getString(
            when (this) {
                LoginUiState.Error.EMAIL_REQUIRED -> R.string.login_error_email_required
                LoginUiState.Error.EMAIL_INVALID -> R.string.login_error_email_invalid
                LoginUiState.Error.PASSWORD_REQUIRED -> R.string.login_error_password_required
                LoginUiState.Error.PASSWORD_TOO_SHORT -> R.string.login_error_password_short
                LoginUiState.Error.INVALID_CREDENTIALS -> R.string.login_error_invalid_credentials
                LoginUiState.Error.NETWORK -> R.string.login_error_network
                LoginUiState.Error.TOO_MANY_REQUESTS -> R.string.login_error_too_many_requests
                LoginUiState.Error.UNKNOWN -> R.string.login_error_unknown
            },
        )
}
