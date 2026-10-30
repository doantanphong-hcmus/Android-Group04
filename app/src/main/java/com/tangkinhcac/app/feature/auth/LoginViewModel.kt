package com.tangkinhcac.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tangkinhcac.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signIn(
        email: String,
        password: String,
    ) {
        // Ignore repeated taps while Firebase is still handling the current request.
        if (_uiState.value.isLoading) {
            return
        }

        val normalizedEmail = email.trim()
        val validationError = validate(normalizedEmail, password)

        if (validationError != null) {
            _uiState.value = LoginUiState(error = validationError)
            return
        }

        _uiState.value = LoginUiState(isLoading = true)

        authRepository.signIn(
            email = normalizedEmail,
            password = password,
        ) { result ->
            _uiState.value =
                when (result) {
                    AuthRepository.SignInResult.Success ->
                        LoginUiState(isAuthenticated = true)

                    is AuthRepository.SignInResult.Failure ->
                        LoginUiState(error = result.reason.toUiError())
                }
        }
    }

    fun clearError() {
        if (_uiState.value.error != null) {
            _uiState.value = _uiState.value.copy(error = null)
        }
    }

    private fun validate(
        email: String,
        password: String,
    ): LoginUiState.Error? =
        when {
            email.isEmpty() -> LoginUiState.Error.EMAIL_REQUIRED
            !EMAIL_PATTERN.matches(email) -> LoginUiState.Error.EMAIL_INVALID
            password.isEmpty() -> LoginUiState.Error.PASSWORD_REQUIRED
            password.length < MIN_PASSWORD_LENGTH -> LoginUiState.Error.PASSWORD_TOO_SHORT
            else -> null
        }

    private fun AuthRepository.FailureReason.toUiError(): LoginUiState.Error =
        when (this) {
            AuthRepository.FailureReason.INVALID_CREDENTIALS -> LoginUiState.Error.INVALID_CREDENTIALS
            AuthRepository.FailureReason.NETWORK -> LoginUiState.Error.NETWORK
            AuthRepository.FailureReason.TOO_MANY_REQUESTS -> LoginUiState.Error.TOO_MANY_REQUESTS
            AuthRepository.FailureReason.UNKNOWN -> LoginUiState.Error.UNKNOWN
        }

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    LoginViewModel(authRepository)
                }
            }
    }
}
