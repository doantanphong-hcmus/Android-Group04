package com.tangkinhcac.app.feature.auth

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.tangkinhcac.app.R
import com.tangkinhcac.app.TangKinhCacApplication
import com.tangkinhcac.app.ui.applySystemBarPadding
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private val viewModel: RegisterViewModel by viewModels {
        val application = requireActivity().application as TangKinhCacApplication
        RegisterViewModel.factory(application.authRepository)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val content = view.findViewById<View>(R.id.register_content)
        val scroll = view.findViewById<NestedScrollView>(R.id.register_scroll)
        val backButton = view.findViewById<ImageButton>(R.id.register_back_button)
        val fullNameInput = view.findViewById<TextInputLayout>(R.id.full_name_input)
        val usernameInput = view.findViewById<TextInputLayout>(R.id.username_input)
        val birthDateInput = view.findViewById<TextInputLayout>(R.id.birth_date_input)
        val phoneNumberInput = view.findViewById<TextInputLayout>(R.id.phone_number_input)
        val emailInput = view.findViewById<TextInputLayout>(R.id.register_email_input)
        val passwordInput = view.findViewById<TextInputLayout>(R.id.register_password_input)
        val confirmPasswordInput = view.findViewById<TextInputLayout>(R.id.confirm_password_input)
        val fullNameEditText = view.findViewById<TextInputEditText>(R.id.full_name_edit_text)
        val usernameEditText = view.findViewById<TextInputEditText>(R.id.username_edit_text)
        val birthDateEditText = view.findViewById<TextInputEditText>(R.id.birth_date_edit_text)
        val phoneNumberEditText = view.findViewById<TextInputEditText>(R.id.phone_number_edit_text)
        val emailEditText = view.findViewById<TextInputEditText>(R.id.register_email_edit_text)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.register_password_edit_text)
        val confirmPasswordEditText = view.findViewById<TextInputEditText>(R.id.confirm_password_edit_text)
        val registerButton = view.findViewById<AppCompatButton>(R.id.register_button)
        val registerProgress = view.findViewById<ProgressBar>(R.id.register_progress)
        val generalError = view.findViewById<TextView>(R.id.register_general_error)

        content.applySystemBarPadding()
        backButton.setOnClickListener { findNavController().navigateUp() }
        birthDateEditText.setOnClickListener { showBirthDatePicker(birthDateEditText) }

        fullNameEditText.clearErrorAfterChange(RegisterUiState.Field.FULL_NAME)
        usernameEditText.clearErrorAfterChange(RegisterUiState.Field.USERNAME)
        phoneNumberEditText.clearErrorAfterChange(RegisterUiState.Field.PHONE_NUMBER)
        emailEditText.clearErrorAfterChange(RegisterUiState.Field.EMAIL)
        passwordEditText.doAfterTextChanged {
            viewModel.clearError(RegisterUiState.Field.PASSWORD)
            viewModel.clearError(RegisterUiState.Field.CONFIRM_PASSWORD)
        }
        confirmPasswordEditText.clearErrorAfterChange(RegisterUiState.Field.CONFIRM_PASSWORD)

        registerButton.setOnClickListener {
            viewModel.register(
                fullName = fullNameEditText.text?.toString().orEmpty(),
                username = usernameEditText.text?.toString().orEmpty(),
                birthDate = birthDateEditText.text?.toString().orEmpty(),
                phoneNumber = phoneNumberEditText.text?.toString().orEmpty(),
                email = emailEditText.text?.toString().orEmpty(),
                password = passwordEditText.text?.toString().orEmpty(),
                confirmPassword = confirmPasswordEditText.text?.toString().orEmpty(),
            )
        }

        confirmPasswordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                registerButton.performClick()
                true
            } else {
                false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    fullNameInput.error = state.fieldErrors.fullName?.toMessage()
                    usernameInput.error = state.fieldErrors.username?.toMessage()
                    birthDateInput.error = state.fieldErrors.birthDate?.toMessage()
                    phoneNumberInput.error = state.fieldErrors.phoneNumber?.toMessage()
                    emailInput.error = state.fieldErrors.email?.toMessage()
                    passwordInput.error = state.fieldErrors.password?.toMessage()
                    confirmPasswordInput.error = state.fieldErrors.confirmPassword?.toMessage()

                    val inputs =
                        listOf(
                            fullNameInput,
                            usernameInput,
                            birthDateInput,
                            phoneNumberInput,
                            emailInput,
                            passwordInput,
                            confirmPasswordInput,
                        )
                    inputs.forEach { input -> input.isEnabled = !state.isLoading }
                    backButton.isEnabled = !state.isLoading
                    registerButton.isEnabled = !state.isLoading
                    registerButton.text = if (state.isLoading) "" else getString(R.string.register_button)
                    registerProgress.isVisible = state.isLoading

                    generalError.text = state.generalError?.toMessage()
                    generalError.isVisible = state.generalError != null

                    scrollToFirstError(
                        scroll = scroll,
                        fieldErrors = state.fieldErrors,
                        inputs = inputs,
                    )

                    if (
                        state.isRegistrationSuccessful &&
                        findNavController().currentDestination?.id == R.id.registerFragment
                    ) {
                        findNavController().navigate(
                            R.id.action_registerFragment_to_registrationSuccessFragment,
                        )
                    }
                }
            }
        }
    }

    private fun TextInputEditText.clearErrorAfterChange(field: RegisterUiState.Field) {
        doAfterTextChanged { viewModel.clearError(field) }
    }

    private fun showBirthDatePicker(birthDateEditText: TextInputEditText) {
        val constraints =
            CalendarConstraints
                .Builder()
                .setValidator(DateValidatorPointBackward.now())
                .build()
        val picker =
            MaterialDatePicker.Builder
                .datePicker()
                .setTitleText(R.string.register_birth_date_picker_title)
                .setCalendarConstraints(constraints)
                .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate()
            birthDateEditText.setText(selectedDate.format(BIRTH_DATE_FORMATTER))
            viewModel.clearError(RegisterUiState.Field.BIRTH_DATE)
        }
        picker.show(parentFragmentManager, BIRTH_DATE_PICKER_TAG)
    }

    private fun scrollToFirstError(
        scroll: NestedScrollView,
        fieldErrors: RegisterFieldErrors,
        inputs: List<TextInputLayout>,
    ) {
        val errors =
            listOf(
                fieldErrors.fullName,
                fieldErrors.username,
                fieldErrors.birthDate,
                fieldErrors.phoneNumber,
                fieldErrors.email,
                fieldErrors.password,
                fieldErrors.confirmPassword,
            )
        val firstErrorIndex = errors.indexOfFirst { error -> error != null }
        if (firstErrorIndex < 0) {
            return
        }

        val firstInvalidInput = inputs[firstErrorIndex]
        scroll.post {
            scroll.smoothScrollTo(0, firstInvalidInput.top)
            firstInvalidInput.editText?.requestFocus()
        }
    }

    private fun RegisterUiState.FieldError.toMessage(): String =
        getString(
            when (this) {
                RegisterUiState.FieldError.REQUIRED -> R.string.register_error_required
                RegisterUiState.FieldError.FULL_NAME_TOO_SHORT -> R.string.register_error_full_name_short
                RegisterUiState.FieldError.USERNAME_INVALID -> R.string.register_error_username_invalid
                RegisterUiState.FieldError.USERNAME_ALREADY_USED -> R.string.register_error_username_used
                RegisterUiState.FieldError.BIRTH_DATE_INVALID -> R.string.register_error_birth_date_invalid
                RegisterUiState.FieldError.BIRTH_DATE_IN_FUTURE -> R.string.register_error_birth_date_future
                RegisterUiState.FieldError.PHONE_NUMBER_INVALID -> R.string.register_error_phone_invalid
                RegisterUiState.FieldError.EMAIL_INVALID -> R.string.register_error_email_invalid
                RegisterUiState.FieldError.EMAIL_ALREADY_USED -> R.string.register_error_email_used
                RegisterUiState.FieldError.PASSWORD_TOO_SHORT -> R.string.register_error_password_short
                RegisterUiState.FieldError.PASSWORDS_DO_NOT_MATCH -> R.string.register_error_password_mismatch
            },
        )

    private fun RegisterUiState.GeneralError.toMessage(): String =
        getString(
            when (this) {
                RegisterUiState.GeneralError.NETWORK -> R.string.register_error_network
                RegisterUiState.GeneralError.TOO_MANY_REQUESTS -> R.string.register_error_too_many_requests
                RegisterUiState.GeneralError.SERVICE_UNAVAILABLE -> R.string.register_error_service_unavailable
                RegisterUiState.GeneralError.UNKNOWN -> R.string.register_error_unknown
            },
        )

    companion object {
        private const val BIRTH_DATE_PICKER_TAG = "birth_date_picker"
        private val BIRTH_DATE_FORMATTER = DateTimeFormatter.ofPattern(BIRTH_DATE_PATTERN)
    }
}
