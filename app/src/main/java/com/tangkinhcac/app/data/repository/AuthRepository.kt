package com.tangkinhcac.app.data.repository

import com.tangkinhcac.app.data.model.RegistrationRequest
import com.tangkinhcac.app.data.model.UserAccount

/**
 * Defines authentication operations without exposing transport or provider types to the UI layer.
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

    fun getCurrentUser(onResult: (CurrentUserResult) -> Unit)

    fun signOut(onResult: (SignOutResult) -> Unit)

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
        SERVICE_UNAVAILABLE,
        UNKNOWN,
    }

    sealed interface RegistrationResult {
        data object Success : RegistrationResult

        data class Failure(
            val reason: RegistrationFailureReason,
            val invalidFields: Set<RegistrationField> = emptySet(),
        ) : RegistrationResult
    }

    enum class RegistrationField {
        FULL_NAME,
        USERNAME,
        BIRTH_DATE,
        PHONE_NUMBER,
        EMAIL,
        PASSWORD,
        PASSWORD_CONFIRMATION,
    }

    enum class RegistrationFailureReason {
        EMAIL_ALREADY_USED,
        USERNAME_ALREADY_USED,
        WEAK_PASSWORD,
        VALIDATION,
        NETWORK,
        TOO_MANY_REQUESTS,
        SERVICE_UNAVAILABLE,
        UNKNOWN,
    }

    sealed interface CurrentUserResult {
        data class Success(
            val user: UserAccount,
        ) : CurrentUserResult

        data object Unauthenticated : CurrentUserResult

        data class Failure(
            val reason: SessionFailureReason,
        ) : CurrentUserResult
    }

    sealed interface SignOutResult {
        data object Success : SignOutResult

        data class Failure(
            val reason: SessionFailureReason,
        ) : SignOutResult
    }

    enum class SessionFailureReason {
        NETWORK,
        SERVICE_UNAVAILABLE,
        UNKNOWN,
    }
}
