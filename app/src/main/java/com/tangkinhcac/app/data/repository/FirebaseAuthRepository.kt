package com.tangkinhcac.app.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

/**
 * Implements authentication through Firebase while keeping Firebase-specific
 * errors out of the presentation layer.
 */
class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
) : AuthRepository {
    /**
     * Firebase restores the current user automatically when a valid session exists.
     */
    override val hasAuthenticatedUser: Boolean
        get() = firebaseAuth.currentUser != null

    override fun signIn(
        email: String,
        password: String,
        onResult: (AuthRepository.SignInResult) -> Unit,
    ) {
        // Firebase completes authentication asynchronously, so callers receive the result via this callback.
        firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(AuthRepository.SignInResult.Success)
                    return@addOnCompleteListener
                }

                // Keep Firebase exception types inside the data layer so the rest of the app stays provider-agnostic.
                val reason =
                    when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException,
                        is FirebaseAuthInvalidUserException,
                        -> AuthRepository.FailureReason.INVALID_CREDENTIALS

                        is FirebaseNetworkException ->
                            AuthRepository.FailureReason.NETWORK

                        is FirebaseTooManyRequestsException ->
                            AuthRepository.FailureReason.TOO_MANY_REQUESTS

                        else ->
                            AuthRepository.FailureReason.UNKNOWN
                    }

                onResult(
                    AuthRepository.SignInResult.Failure(reason),
                )
            }
    }
}
