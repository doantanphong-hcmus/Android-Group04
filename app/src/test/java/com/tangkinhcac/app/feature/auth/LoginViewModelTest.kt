package com.tangkinhcac.app.feature.auth

import com.tangkinhcac.app.data.model.RegistrationRequest
import com.tangkinhcac.app.data.repository.AuthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginViewModelTest {
    private val repository = FakeAuthRepository()
    private val viewModel = LoginViewModel(repository)

    @Test
    fun `invalid email is rejected before calling repository`() {
        viewModel.signIn(
            email = "not-an-email",
            password = "12345678",
        )

        assertEquals(LoginUiState.Error.EMAIL_INVALID, viewModel.uiState.value.error)
        assertEquals(0, repository.signInCalls)
    }

    @Test
    fun `short password is forwarded because login policy belongs to backend`() {
        viewModel.signIn(
            email = "reader@example.com",
            password = "123",
        )

        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(1, repository.signInCalls)
    }

    @Test
    fun `valid credentials start one login request`() {
        viewModel.signIn(
            email = "  reader@example.com  ",
            password = "12345678",
        )
        viewModel.signIn(
            email = "reader@example.com",
            password = "12345678",
        )

        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(1, repository.signInCalls)
        assertEquals("reader@example.com", repository.lastEmail)
    }

    @Test
    fun `successful login marks user as authenticated`() {
        viewModel.signIn(
            email = "reader@example.com",
            password = "12345678",
        )

        repository.complete(AuthRepository.SignInResult.Success)

        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `network failure becomes user interface network error`() {
        viewModel.signIn(
            email = "reader@example.com",
            password = "12345678",
        )

        repository.complete(
            AuthRepository.SignInResult.Failure(
                AuthRepository.FailureReason.NETWORK,
            ),
        )

        assertEquals(LoginUiState.Error.NETWORK, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isAuthenticated)
    }

    @Test
    fun `unavailable service becomes user interface service error`() {
        viewModel.signIn(
            email = "reader@example.com",
            password = "12345678",
        )

        repository.complete(
            AuthRepository.SignInResult.Failure(
                AuthRepository.FailureReason.SERVICE_UNAVAILABLE,
            ),
        )

        assertEquals(LoginUiState.Error.SERVICE_UNAVAILABLE, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    private class FakeAuthRepository : AuthRepository {
        override val hasAuthenticatedUser = false

        var signInCalls = 0
            private set

        var lastEmail: String? = null
            private set

        private var callback: ((AuthRepository.SignInResult) -> Unit)? = null

        override fun signIn(
            email: String,
            password: String,
            onResult: (AuthRepository.SignInResult) -> Unit,
        ) {
            signInCalls += 1
            lastEmail = email
            callback = onResult
        }

        override fun registerAccount(
            request: RegistrationRequest,
            onResult: (AuthRepository.RegistrationResult) -> Unit,
        ) {
            error("Registration is not used by LoginViewModel tests")
        }

        override fun getCurrentUser(onResult: (AuthRepository.CurrentUserResult) -> Unit) {
            error("Current user is not used by LoginViewModel tests")
        }

        override fun signOut(onResult: (AuthRepository.SignOutResult) -> Unit) {
            error("Logout is not used by LoginViewModel tests")
        }

        fun complete(result: AuthRepository.SignInResult) {
            checkNotNull(callback)(result)
        }
    }
}
