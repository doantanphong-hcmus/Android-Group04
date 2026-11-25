package com.tangkinhcac.app.feature.auth

data class LoginUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: Error? = null,
) {
    enum class Error {
        EMAIL_REQUIRED,
        EMAIL_INVALID,
        PASSWORD_REQUIRED,
        INVALID_CREDENTIALS,
        NETWORK,
        TOO_MANY_REQUESTS,
        SERVICE_UNAVAILABLE,
        UNKNOWN,
    }
}
