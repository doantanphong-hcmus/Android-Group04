package com.tangkinhcac.app.data.repository

import com.tangkinhcac.app.data.model.RegistrationRequest

/** Keeps the Auth screens safe while the backend implementation is being connected. */
class UnavailableAuthRepository : AuthRepository {
    override val hasAuthenticatedUser = false

    override fun signIn(
        email: String,
        password: String,
        onResult: (AuthRepository.SignInResult) -> Unit,
    ) {
        onResult(
            AuthRepository.SignInResult.Failure(
                AuthRepository.FailureReason.SERVICE_UNAVAILABLE,
            ),
        )
    }

    override fun registerAccount(
        request: RegistrationRequest,
        onResult: (AuthRepository.RegistrationResult) -> Unit,
    ) {
        onResult(
            AuthRepository.RegistrationResult.Failure(
                AuthRepository.RegistrationFailureReason.SERVICE_UNAVAILABLE,
            ),
        )
    }
}
