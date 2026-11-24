package com.tangkinhcac.app.feature.auth

import com.tangkinhcac.app.data.model.RegistrationRequest
import com.tangkinhcac.app.data.repository.AuthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterViewModelTest {
    private val repository = FakeAuthRepository()
    private val viewModel = RegisterViewModel(repository)

    @Test
    fun `empty form reports an error for every field`() {
        register(
            fullName = "",
            username = "",
            birthDate = "",
            phoneNumber = "",
            email = "",
            password = "",
            confirmPassword = "",
        )

        val errors = viewModel.uiState.value.fieldErrors
        assertEquals(RegisterUiState.FieldError.REQUIRED, errors.fullName)
        assertEquals(RegisterUiState.FieldError.REQUIRED, errors.username)
        assertEquals(RegisterUiState.FieldError.REQUIRED, errors.birthDate)
        assertEquals(RegisterUiState.FieldError.REQUIRED, errors.phoneNumber)
        assertEquals(RegisterUiState.FieldError.REQUIRED, errors.email)
        assertEquals(RegisterUiState.FieldError.REQUIRED, errors.password)
        assertEquals(RegisterUiState.FieldError.REQUIRED, errors.confirmPassword)
        assertEquals(0, repository.registrationCalls)
    }

    @Test
    fun `invalid values report errors on their matching fields`() {
        register(
            fullName = "A",
            username = "invalid username",
            birthDate = "31/02/2020",
            phoneNumber = "123",
            email = "invalid-email",
            password = "123",
            confirmPassword = "456",
        )

        val errors = viewModel.uiState.value.fieldErrors
        assertEquals(RegisterUiState.FieldError.FULL_NAME_TOO_SHORT, errors.fullName)
        assertEquals(RegisterUiState.FieldError.USERNAME_INVALID, errors.username)
        assertEquals(RegisterUiState.FieldError.BIRTH_DATE_INVALID, errors.birthDate)
        assertEquals(RegisterUiState.FieldError.PHONE_NUMBER_INVALID, errors.phoneNumber)
        assertEquals(RegisterUiState.FieldError.EMAIL_INVALID, errors.email)
        assertEquals(RegisterUiState.FieldError.PASSWORD_TOO_SHORT, errors.password)
        assertEquals(RegisterUiState.FieldError.PASSWORDS_DO_NOT_MATCH, errors.confirmPassword)
    }

    @Test
    fun `values above backend limits are rejected before calling repository`() {
        register(
            fullName = "A".repeat(101),
            email = "a".repeat(256),
            password = "p".repeat(129),
            confirmPassword = "p".repeat(129),
        )

        val errors = viewModel.uiState.value.fieldErrors
        assertEquals(RegisterUiState.FieldError.FULL_NAME_TOO_LONG, errors.fullName)
        assertEquals(RegisterUiState.FieldError.EMAIL_TOO_LONG, errors.email)
        assertEquals(RegisterUiState.FieldError.PASSWORD_TOO_LONG, errors.password)
        assertEquals(0, repository.registrationCalls)
    }

    @Test
    fun `future birth date is rejected`() {
        register(birthDate = "01/01/2999")

        assertEquals(
            RegisterUiState.FieldError.BIRTH_DATE_IN_FUTURE,
            viewModel.uiState.value.fieldErrors.birthDate,
        )
        assertEquals(0, repository.registrationCalls)
    }

    @Test
    fun `phone number containing letters is rejected`() {
        register(phoneNumber = "abc0901234567")

        assertEquals(
            RegisterUiState.FieldError.PHONE_NUMBER_INVALID,
            viewModel.uiState.value.fieldErrors.phoneNumber,
        )
        assertEquals(0, repository.registrationCalls)
    }

    @Test
    fun `valid form normalizes account fields and starts one request`() {
        register(
            fullName = "  Nguyễn Văn An  ",
            username = "  Reader.One  ",
            phoneNumber = "+84 901-234-567",
            email = "  READER@example.com  ",
        )
        register()

        val request = checkNotNull(repository.lastRequest)
        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(1, repository.registrationCalls)
        assertEquals("Nguyễn Văn An", request.fullName)
        assertEquals("Reader.One", request.username)
        assertEquals("1990-06-15", request.birthDate)
        assertEquals("+84901234567", request.phoneNumber)
        assertEquals("reader@example.com", request.email)
    }

    @Test
    fun `username conflict appears on username field`() {
        register()
        repository.complete(
            AuthRepository.RegistrationResult.Failure(
                AuthRepository.RegistrationFailureReason.USERNAME_ALREADY_USED,
            ),
        )

        val state = viewModel.uiState.value
        assertEquals(RegisterUiState.FieldError.USERNAME_ALREADY_USED, state.fieldErrors.username)
        assertFalse(state.isLoading)
        assertNull(state.generalError)
    }

    @Test
    fun `email conflict appears on email field`() {
        register()
        repository.complete(
            AuthRepository.RegistrationResult.Failure(
                AuthRepository.RegistrationFailureReason.EMAIL_ALREADY_USED,
            ),
        )

        assertEquals(
            RegisterUiState.FieldError.EMAIL_ALREADY_USED,
            viewModel.uiState.value.fieldErrors.email,
        )
    }

    @Test
    fun `backend validation fields appear on matching inputs`() {
        register()
        repository.complete(
            AuthRepository.RegistrationResult.Failure(
                reason = AuthRepository.RegistrationFailureReason.VALIDATION,
                invalidFields =
                    setOf(
                        AuthRepository.RegistrationField.EMAIL,
                        AuthRepository.RegistrationField.PASSWORD_CONFIRMATION,
                    ),
            ),
        )

        val errors = viewModel.uiState.value.fieldErrors
        assertEquals(RegisterUiState.FieldError.INVALID, errors.email)
        assertEquals(RegisterUiState.FieldError.INVALID, errors.confirmPassword)
    }

    @Test
    fun `network failure becomes a general error`() {
        register()
        repository.complete(
            AuthRepository.RegistrationResult.Failure(
                AuthRepository.RegistrationFailureReason.NETWORK,
            ),
        )

        assertEquals(RegisterUiState.GeneralError.NETWORK, viewModel.uiState.value.generalError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `backend configuration failure becomes a service unavailable error`() {
        register()
        repository.complete(
            AuthRepository.RegistrationResult.Failure(
                AuthRepository.RegistrationFailureReason.SERVICE_UNAVAILABLE,
            ),
        )

        assertEquals(
            RegisterUiState.GeneralError.SERVICE_UNAVAILABLE,
            viewModel.uiState.value.generalError,
        )
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `successful registration marks flow as completed`() {
        register()
        repository.complete(AuthRepository.RegistrationResult.Success)

        val state = viewModel.uiState.value
        assertTrue(state.isRegistrationSuccessful)
        assertFalse(state.isLoading)
        assertFalse(state.fieldErrors.hasErrors)
        assertNull(state.generalError)
    }

    private fun register(
        fullName: String = "Nguyễn Văn An",
        username: String = "reader.one",
        birthDate: String = "15/06/1990",
        phoneNumber: String = "0901234567",
        email: String = "reader@example.com",
        password: String = "12345678",
        confirmPassword: String = "12345678",
    ) {
        viewModel.register(
            fullName = fullName,
            username = username,
            birthDate = birthDate,
            phoneNumber = phoneNumber,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
        )
    }

    private class FakeAuthRepository : AuthRepository {
        override val hasAuthenticatedUser = false

        var registrationCalls = 0
            private set

        var lastRequest: RegistrationRequest? = null
            private set

        private var registrationCallback: ((AuthRepository.RegistrationResult) -> Unit)? = null

        override fun signIn(
            email: String,
            password: String,
            onResult: (AuthRepository.SignInResult) -> Unit,
        ) {
            error("Login is not used by RegisterViewModel tests")
        }

        override fun registerAccount(
            request: RegistrationRequest,
            onResult: (AuthRepository.RegistrationResult) -> Unit,
        ) {
            registrationCalls += 1
            lastRequest = request
            registrationCallback = onResult
        }

        override fun getCurrentUser(onResult: (AuthRepository.CurrentUserResult) -> Unit) {
            error("Current user is not used by RegisterViewModel tests")
        }

        override fun signOut(onResult: (AuthRepository.SignOutResult) -> Unit) {
            error("Logout is not used by RegisterViewModel tests")
        }

        fun complete(result: AuthRepository.RegistrationResult) {
            checkNotNull(registrationCallback)(result)
        }
    }
}
