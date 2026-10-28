package com.tangkinhcac.app.data.repository

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
}
