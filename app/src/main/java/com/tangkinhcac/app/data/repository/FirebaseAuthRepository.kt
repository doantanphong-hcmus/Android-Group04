package com.tangkinhcac.app.data.repository

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.tangkinhcac.app.data.model.RegistrationRequest

/**
 * Implements authentication through Firebase while keeping Firebase-specific
 * errors out of the presentation layer.
 */
class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
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

    override fun registerAccount(
        request: RegistrationRequest,
        onResult: (AuthRepository.RegistrationResult) -> Unit,
    ) {
        firebaseAuth
            .createUserWithEmailAndPassword(request.email, request.password)
            .addOnCompleteListener { authTask ->
                if (!authTask.isSuccessful) {
                    Log.e(TAG, "Firebase account creation failed.", authTask.exception)
                    onResult(
                        AuthRepository.RegistrationResult.Failure(
                            authTask.exception.toRegistrationFailureReason(),
                        ),
                    )
                    return@addOnCompleteListener
                }

                val user = authTask.result.user
                if (user == null) {
                    firebaseAuth.signOut()
                    onResult(
                        AuthRepository.RegistrationResult.Failure(
                            AuthRepository.RegistrationFailureReason.UNKNOWN,
                        ),
                    )
                    return@addOnCompleteListener
                }

                createProfile(
                    user = user,
                    request = request,
                    onResult = onResult,
                )
            }
    }

    private fun createProfile(
        user: FirebaseUser,
        request: RegistrationRequest,
        onResult: (AuthRepository.RegistrationResult) -> Unit,
    ) {
        val userReference = firestore.collection(USERS_COLLECTION).document(user.uid)
        val usernameReference =
            firestore.collection(USERNAMES_COLLECTION).document(request.normalizedUsername)
        val profile =
            mapOf(
                "fullName" to request.fullName,
                "username" to request.username,
                "usernameNormalized" to request.normalizedUsername,
                "birthDate" to request.birthDate,
                "phoneNumber" to request.phoneNumber,
                "email" to request.email,
                "provider" to PASSWORD_PROVIDER,
                "createdAt" to FieldValue.serverTimestamp(),
            )

        // Reserve the normalized username and create its profile atomically to prevent concurrent claims.
        firestore
            .runTransaction { transaction ->
                if (transaction.get(usernameReference).exists()) {
                    throw UsernameAlreadyExistsException()
                }

                transaction.set(usernameReference, mapOf("uid" to user.uid))
                transaction.set(userReference, profile)
            }.addOnCompleteListener { profileTask ->
                if (profileTask.isSuccessful) {
                    // Firebase signs in newly created users, but this flow intentionally returns them to Login.
                    firebaseAuth.signOut()
                    onResult(AuthRepository.RegistrationResult.Success)
                    return@addOnCompleteListener
                }

                Log.e(TAG, "Firestore profile creation failed.", profileTask.exception)
                val failureReason = profileTask.exception.toRegistrationFailureReason()
                removeIncompleteAccount(
                    user = user,
                    failureReason = failureReason,
                    onResult = onResult,
                )
            }
    }

    private fun removeIncompleteAccount(
        user: FirebaseUser,
        failureReason: AuthRepository.RegistrationFailureReason,
        onResult: (AuthRepository.RegistrationResult) -> Unit,
    ) {
        // Remove the Auth record when profile creation fails so a retry does not leave a partial account.
        user.delete().addOnCompleteListener { deletionTask ->
            if (!deletionTask.isSuccessful) {
                Log.e(TAG, "Failed to remove an incomplete Firebase account.", deletionTask.exception)
            }
            firebaseAuth.signOut()
            onResult(AuthRepository.RegistrationResult.Failure(failureReason))
        }
    }

    private fun Throwable?.toRegistrationFailureReason(): AuthRepository.RegistrationFailureReason =
        when {
            this is UsernameAlreadyExistsException || this?.cause is UsernameAlreadyExistsException ->
                AuthRepository.RegistrationFailureReason.USERNAME_ALREADY_USED

            this is FirebaseAuthUserCollisionException ->
                AuthRepository.RegistrationFailureReason.EMAIL_ALREADY_USED

            this is FirebaseAuthWeakPasswordException ->
                AuthRepository.RegistrationFailureReason.WEAK_PASSWORD

            this is FirebaseNetworkException ->
                AuthRepository.RegistrationFailureReason.NETWORK

            this is FirebaseTooManyRequestsException ->
                AuthRepository.RegistrationFailureReason.TOO_MANY_REQUESTS

            this is FirebaseAuthException && errorCode == "ERROR_OPERATION_NOT_ALLOWED" ->
                AuthRepository.RegistrationFailureReason.SERVICE_UNAVAILABLE

            this is FirebaseFirestoreException &&
                code in
                setOf(
                    FirebaseFirestoreException.Code.UNAVAILABLE,
                    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
                ) ->
                AuthRepository.RegistrationFailureReason.NETWORK

            this is FirebaseFirestoreException &&
                code in
                setOf(
                    FirebaseFirestoreException.Code.PERMISSION_DENIED,
                    FirebaseFirestoreException.Code.FAILED_PRECONDITION,
                    FirebaseFirestoreException.Code.NOT_FOUND,
                    FirebaseFirestoreException.Code.UNAUTHENTICATED,
                ) -> AuthRepository.RegistrationFailureReason.SERVICE_UNAVAILABLE

            else -> AuthRepository.RegistrationFailureReason.UNKNOWN
        }

    private class UsernameAlreadyExistsException : IllegalStateException()

    private companion object {
        const val USERS_COLLECTION = "users"
        const val USERNAMES_COLLECTION = "usernames"
        const val PASSWORD_PROVIDER = "password"
        const val TAG = "FirebaseAuthRepository"
    }
}
