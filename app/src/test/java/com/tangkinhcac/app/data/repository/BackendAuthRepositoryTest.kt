package com.tangkinhcac.app.data.repository

import com.tangkinhcac.app.data.local.auth.AuthTokenStore
import com.tangkinhcac.app.data.model.RegistrationRequest
import com.tangkinhcac.app.data.model.UserAccount
import com.tangkinhcac.app.data.remote.auth.AuthGateway
import com.tangkinhcac.app.data.remote.auth.AuthGatewayResult
import com.tangkinhcac.app.data.remote.auth.AuthSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackendAuthRepositoryTest {
    private val user =
        UserAccount(
            id = 1L,
            fullName = "Nguyen Van An",
            username = "reader.one",
            birthDate = "1990-06-15",
            phoneNumber = "+84901234567",
            email = "reader@example.com",
            role = "reader",
        )
    private val gateway = FakeAuthGateway(user)
    private val tokenStore = FakeAuthTokenStore()
    private val repository = BackendAuthRepository(gateway, tokenStore, "JUnit device")

    @Test
    fun `successful login persists token before authenticating user`() {
        gateway.loginResult = AuthGatewayResult.Success(AuthSession("secret-token", user))

        var result: AuthRepository.SignInResult? = null
        repository.signIn("reader@example.com", "password") { result = it }

        assertEquals(AuthRepository.SignInResult.Success, result)
        assertEquals("secret-token", tokenStore.token)
        assertTrue(repository.hasAuthenticatedUser)
        assertEquals("JUnit device", gateway.lastDeviceName)
    }

    @Test
    fun `invalid login credentials do not create a local session`() {
        gateway.loginResult =
            AuthGatewayResult.Failure(
                errorCode = "INVALID_CREDENTIALS",
                httpStatus = 401,
            )

        var result: AuthRepository.SignInResult? = null
        repository.signIn("reader@example.com", "password") { result = it }

        assertEquals(
            AuthRepository.SignInResult.Failure(AuthRepository.FailureReason.INVALID_CREDENTIALS),
            result,
        )
        assertNull(tokenStore.token)
        assertFalse(repository.hasAuthenticatedUser)
    }

    @Test
    fun `registration validation fields remain machine readable`() {
        gateway.registrationResult =
            AuthGatewayResult.Failure(
                errorCode = "VALIDATION_ERROR",
                httpStatus = 422,
                invalidFields = setOf("email", "password_confirmation"),
            )

        var result: AuthRepository.RegistrationResult? = null
        repository.registerAccount(validRegistrationRequest()) { result = it }

        assertEquals(
            AuthRepository.RegistrationResult.Failure(
                reason = AuthRepository.RegistrationFailureReason.VALIDATION,
                invalidFields =
                    setOf(
                        AuthRepository.RegistrationField.EMAIL,
                        AuthRepository.RegistrationField.PASSWORD_CONFIRMATION,
                    ),
            ),
            result,
        )
    }

    @Test
    fun `unauthenticated current user response clears revoked token`() {
        tokenStore.save("revoked-token")
        gateway.currentUserResult =
            AuthGatewayResult.Failure(
                errorCode = "UNAUTHENTICATED",
                httpStatus = 401,
            )

        var result: AuthRepository.CurrentUserResult? = null
        repository.getCurrentUser { result = it }

        assertEquals(AuthRepository.CurrentUserResult.Unauthenticated, result)
        assertNull(tokenStore.token)
    }

    @Test
    fun `logout clears local token even when network revocation fails`() {
        tokenStore.save("active-token")
        gateway.logoutResult = AuthGatewayResult.Failure(isNetworkError = true)

        var result: AuthRepository.SignOutResult? = null
        repository.signOut { result = it }

        assertEquals(
            AuthRepository.SignOutResult.Failure(AuthRepository.SessionFailureReason.NETWORK),
            result,
        )
        assertNull(tokenStore.token)
    }

    private fun validRegistrationRequest(): RegistrationRequest =
        RegistrationRequest(
            fullName = "Nguyen Van An",
            username = "reader.one",
            birthDate = "1990-06-15",
            phoneNumber = "+84901234567",
            email = "reader@example.com",
            password = "password",
        )

    private class FakeAuthTokenStore : AuthTokenStore {
        override var token: String? = null
            private set

        override fun save(token: String): Boolean {
            this.token = token
            return true
        }

        override fun clear() {
            token = null
        }
    }

    private class FakeAuthGateway(
        private val user: UserAccount,
    ) : AuthGateway {
        var registrationResult: AuthGatewayResult<UserAccount> = AuthGatewayResult.Success(user)
        var loginResult: AuthGatewayResult<AuthSession> = AuthGatewayResult.Success(AuthSession("token", user))
        var currentUserResult: AuthGatewayResult<UserAccount> = AuthGatewayResult.Success(user)
        var logoutResult: AuthGatewayResult<Unit> = AuthGatewayResult.Success(Unit)
        var lastDeviceName: String? = null
            private set

        override fun register(
            request: RegistrationRequest,
            onResult: (AuthGatewayResult<UserAccount>) -> Unit,
        ) {
            onResult(registrationResult)
        }

        override fun login(
            email: String,
            password: String,
            deviceName: String,
            onResult: (AuthGatewayResult<AuthSession>) -> Unit,
        ) {
            lastDeviceName = deviceName
            onResult(loginResult)
        }

        override fun getCurrentUser(onResult: (AuthGatewayResult<UserAccount>) -> Unit) {
            onResult(currentUserResult)
        }

        override fun logout(onResult: (AuthGatewayResult<Unit>) -> Unit) {
            onResult(logoutResult)
        }
    }
}
