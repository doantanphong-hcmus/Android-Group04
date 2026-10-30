package com.tangkinhcac.app.data.repository

import com.tangkinhcac.app.data.model.RegistrationRequest

/**
 * Defines authentication operations without exposing Firebase types to the UI layer.
 */
interface AuthRepository {
    val hasAuthenticatedUser: Boolean

    fun signIn(
        email: String,
        password: String,
        onResult: (SignInResult) -> Unit,
    )

    fun registerAccount(
        request: RegistrationRequest,
        onResult: (RegistrationResult) -> Unit,
    )

    sealed interface SignInResult {
        data object Success : SignInResult

        data class Failure(
            val reason: FailureReason,
        ) : SignInResult
    }

    enum class FailureReason {
        INVALID_CREDENTIALS,
        NETWORK,
        TOO_MANY_REQUESTS,
        UNKNOWN,
    }

    sealed interface RegistrationResult {
        data object Success : RegistrationResult

        data class Failure(
            val reason: RegistrationFailureReason,
        ) : RegistrationResult
    }

    enum class RegistrationFailureReason {
        EMAIL_ALREADY_USED,
        USERNAME_ALREADY_USED,
        WEAK_PASSWORD,
        NETWORK,
        TOO_MANY_REQUESTS,
        SERVICE_UNAVAILABLE,
        UNKNOWN,
    }
}
