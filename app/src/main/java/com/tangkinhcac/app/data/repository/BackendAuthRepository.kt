package com.tangkinhcac.app.data.repository

import com.tangkinhcac.app.data.local.auth.AuthTokenStore
import com.tangkinhcac.app.data.model.RegistrationRequest
import com.tangkinhcac.app.data.remote.auth.AuthGateway
import com.tangkinhcac.app.data.remote.auth.AuthGatewayResult

/** Owns the backend Auth session and exposes UI-oriented outcomes instead of HTTP details. */
class BackendAuthRepository(
    private val gateway: AuthGateway,
    private val tokenStore: AuthTokenStore,
    private val deviceName: String,
) : AuthRepository {
    override val hasAuthenticatedUser: Boolean
        get() = tokenStore.token != null

    override fun signIn(
        email: String,
        password: String,
        onResult: (AuthRepository.SignInResult) -> Unit,
    ) {
        gateway.login(
            email = email,
            password = password,
            deviceName = deviceName,
        ) { result ->
            onResult(
                when (result) {
                    is AuthGatewayResult.Success ->
                        if (tokenStore.save(result.value.token)) {
                            AuthRepository.SignInResult.Success
                        } else {
                            // Do not leave a previous account's token active after a failed save.
                            tokenStore.clear()
                            AuthRepository.SignInResult.Failure(AuthRepository.FailureReason.UNKNOWN)
                        }

                    is AuthGatewayResult.Failure ->
                        AuthRepository.SignInResult.Failure(result.toSignInFailureReason())
                },
            )
        }
    }

    override fun registerAccount(
        request: RegistrationRequest,
        onResult: (AuthRepository.RegistrationResult) -> Unit,
    ) {
        gateway.register(request) { result ->
            onResult(
                when (result) {
                    is AuthGatewayResult.Success -> AuthRepository.RegistrationResult.Success
                    is AuthGatewayResult.Failure ->
                        AuthRepository.RegistrationResult.Failure(
                            reason = result.toRegistrationFailureReason(),
                            invalidFields = result.invalidFields.mapNotNull(::toRegistrationField).toSet(),
                        )
                },
            )
        }
    }

    override fun getCurrentUser(onResult: (AuthRepository.CurrentUserResult) -> Unit) {
        if (!hasAuthenticatedUser) {
            onResult(AuthRepository.CurrentUserResult.Unauthenticated)
            return
        }

        gateway.getCurrentUser { result ->
            when (result) {
                is AuthGatewayResult.Success ->
                    onResult(AuthRepository.CurrentUserResult.Success(result.value))

                is AuthGatewayResult.Failure -> {
                    if (result.errorCode == ERROR_UNAUTHENTICATED || result.httpStatus == HTTP_UNAUTHORIZED) {
                        tokenStore.clear()
                        onResult(AuthRepository.CurrentUserResult.Unauthenticated)
                    } else {
                        onResult(
                            AuthRepository.CurrentUserResult.Failure(result.toSessionFailureReason()),
                        )
                    }
                }
            }
        }
    }

    override fun signOut(onResult: (AuthRepository.SignOutResult) -> Unit) {
        if (!hasAuthenticatedUser) {
            tokenStore.clear()
            onResult(AuthRepository.SignOutResult.Success)
            return
        }

        gateway.logout { result ->
            // Local logout must complete even when the server cannot revoke an already issued token.
            tokenStore.clear()
            onResult(
                when (result) {
                    is AuthGatewayResult.Success -> AuthRepository.SignOutResult.Success
                    is AuthGatewayResult.Failure ->
                        if (result.errorCode == ERROR_UNAUTHENTICATED || result.httpStatus == HTTP_UNAUTHORIZED) {
                            AuthRepository.SignOutResult.Success
                        } else {
                            AuthRepository.SignOutResult.Failure(result.toSessionFailureReason())
                        }
                },
            )
        }
    }

    private fun AuthGatewayResult.Failure.toSignInFailureReason(): AuthRepository.FailureReason =
        when {
            isNetworkError -> AuthRepository.FailureReason.NETWORK
            errorCode == ERROR_INVALID_CREDENTIALS || httpStatus == HTTP_UNAUTHORIZED ->
                AuthRepository.FailureReason.INVALID_CREDENTIALS
            errorCode == ERROR_TOO_MANY_REQUESTS || httpStatus == HTTP_TOO_MANY_REQUESTS ->
                AuthRepository.FailureReason.TOO_MANY_REQUESTS
            errorCode == ERROR_SERVICE_UNAVAILABLE || httpStatus == HTTP_SERVICE_UNAVAILABLE ->
                AuthRepository.FailureReason.SERVICE_UNAVAILABLE
            else -> AuthRepository.FailureReason.UNKNOWN
        }

    private fun AuthGatewayResult.Failure.toRegistrationFailureReason(): AuthRepository.RegistrationFailureReason =
        when {
            isNetworkError -> AuthRepository.RegistrationFailureReason.NETWORK
            errorCode == ERROR_EMAIL_ALREADY_USED -> AuthRepository.RegistrationFailureReason.EMAIL_ALREADY_USED
            errorCode == ERROR_USERNAME_ALREADY_USED -> AuthRepository.RegistrationFailureReason.USERNAME_ALREADY_USED
            errorCode == ERROR_VALIDATION -> AuthRepository.RegistrationFailureReason.VALIDATION
            errorCode == ERROR_TOO_MANY_REQUESTS || httpStatus == HTTP_TOO_MANY_REQUESTS ->
                AuthRepository.RegistrationFailureReason.TOO_MANY_REQUESTS
            errorCode == ERROR_SERVICE_UNAVAILABLE || httpStatus == HTTP_SERVICE_UNAVAILABLE ->
                AuthRepository.RegistrationFailureReason.SERVICE_UNAVAILABLE
            else -> AuthRepository.RegistrationFailureReason.UNKNOWN
        }

    private fun AuthGatewayResult.Failure.toSessionFailureReason(): AuthRepository.SessionFailureReason =
        when {
            isNetworkError -> AuthRepository.SessionFailureReason.NETWORK
            errorCode == ERROR_SERVICE_UNAVAILABLE || httpStatus == HTTP_SERVICE_UNAVAILABLE ->
                AuthRepository.SessionFailureReason.SERVICE_UNAVAILABLE
            else -> AuthRepository.SessionFailureReason.UNKNOWN
        }

    private fun toRegistrationField(fieldName: String): AuthRepository.RegistrationField? =
        when (fieldName) {
            "full_name" -> AuthRepository.RegistrationField.FULL_NAME
            "username" -> AuthRepository.RegistrationField.USERNAME
            "birth_date" -> AuthRepository.RegistrationField.BIRTH_DATE
            "phone_number" -> AuthRepository.RegistrationField.PHONE_NUMBER
            "email" -> AuthRepository.RegistrationField.EMAIL
            "password" -> AuthRepository.RegistrationField.PASSWORD
            "password_confirmation" -> AuthRepository.RegistrationField.PASSWORD_CONFIRMATION
            else -> null
        }

    private companion object {
        const val ERROR_INVALID_CREDENTIALS = "INVALID_CREDENTIALS"
        const val ERROR_UNAUTHENTICATED = "UNAUTHENTICATED"
        const val ERROR_EMAIL_ALREADY_USED = "EMAIL_ALREADY_USED"
        const val ERROR_USERNAME_ALREADY_USED = "USERNAME_ALREADY_USED"
        const val ERROR_VALIDATION = "VALIDATION_ERROR"
        const val ERROR_TOO_MANY_REQUESTS = "TOO_MANY_REQUESTS"
        const val ERROR_SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE"
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_TOO_MANY_REQUESTS = 429
        const val HTTP_SERVICE_UNAVAILABLE = 503
    }
}
