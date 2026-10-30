package com.tangkinhcac.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tangkinhcac.app.data.model.RegistrationRequest
import com.tangkinhcac.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(
        fullName: String,
        username: String,
        birthDate: String,
        phoneNumber: String,
        email: String,
        password: String,
        confirmPassword: String,
    ) {
        // Prevent duplicate Firebase accounts when the user taps the button repeatedly.
        if (_uiState.value.isLoading) {
            return
        }

        val normalizedFullName = fullName.trim()
        val normalizedUsername = username.trim().lowercase(Locale.ROOT)
        val normalizedPhoneNumber = normalizePhoneNumber(phoneNumber)
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)
        val parsedBirthDate = parseBirthDate(birthDate)
        val fieldErrors =
            validate(
                fullName = normalizedFullName,
                username = normalizedUsername,
                birthDate = birthDate,
                parsedBirthDate = parsedBirthDate,
                phoneNumber = normalizedPhoneNumber,
                email = normalizedEmail,
                password = password,
                confirmPassword = confirmPassword,
            )

        if (fieldErrors.hasErrors) {
            _uiState.value = RegisterUiState(fieldErrors = fieldErrors)
            return
        }

        val validBirthDate = checkNotNull(parsedBirthDate)
        _uiState.value = RegisterUiState(isLoading = true)

        authRepository.registerAccount(
            request =
                RegistrationRequest(
                    fullName = normalizedFullName,
                    username = username.trim(),
                    normalizedUsername = normalizedUsername,
                    birthDate = validBirthDate.toString(),
                    phoneNumber = normalizedPhoneNumber,
                    email = normalizedEmail,
                    password = password,
                ),
        ) { result ->
            _uiState.value = result.toUiState()
        }
    }

    fun clearError(field: RegisterUiState.Field) {
        val currentState = _uiState.value
        val updatedErrors =
            when (field) {
                RegisterUiState.Field.FULL_NAME -> currentState.fieldErrors.copy(fullName = null)
                RegisterUiState.Field.USERNAME -> currentState.fieldErrors.copy(username = null)
                RegisterUiState.Field.BIRTH_DATE -> currentState.fieldErrors.copy(birthDate = null)
                RegisterUiState.Field.PHONE_NUMBER -> currentState.fieldErrors.copy(phoneNumber = null)
                RegisterUiState.Field.EMAIL -> currentState.fieldErrors.copy(email = null)
                RegisterUiState.Field.PASSWORD -> currentState.fieldErrors.copy(password = null)
                RegisterUiState.Field.CONFIRM_PASSWORD -> currentState.fieldErrors.copy(confirmPassword = null)
            }

        _uiState.value = currentState.copy(fieldErrors = updatedErrors, generalError = null)
    }

    private fun validate(
        fullName: String,
        username: String,
        birthDate: String,
        parsedBirthDate: LocalDate?,
        phoneNumber: String,
        email: String,
        password: String,
        confirmPassword: String,
    ): RegisterFieldErrors =
        RegisterFieldErrors(
            fullName =
                when {
                    fullName.isEmpty() -> RegisterUiState.FieldError.REQUIRED
                    fullName.length < MIN_FULL_NAME_LENGTH -> RegisterUiState.FieldError.FULL_NAME_TOO_SHORT
                    else -> null
                },
            username =
                when {
                    username.isEmpty() -> RegisterUiState.FieldError.REQUIRED
                    !USERNAME_PATTERN.matches(username) -> RegisterUiState.FieldError.USERNAME_INVALID
                    else -> null
                },
            birthDate =
                when {
                    birthDate.isBlank() -> RegisterUiState.FieldError.REQUIRED
                    parsedBirthDate == null -> RegisterUiState.FieldError.BIRTH_DATE_INVALID
                    parsedBirthDate.isAfter(LocalDate.now()) -> RegisterUiState.FieldError.BIRTH_DATE_IN_FUTURE
                    else -> null
                },
            phoneNumber =
                when {
                    phoneNumber.isEmpty() -> RegisterUiState.FieldError.REQUIRED
                    !PHONE_NUMBER_PATTERN.matches(phoneNumber) -> RegisterUiState.FieldError.PHONE_NUMBER_INVALID
                    else -> null
                },
            email =
                when {
                    email.isEmpty() -> RegisterUiState.FieldError.REQUIRED
                    !EMAIL_PATTERN.matches(email) -> RegisterUiState.FieldError.EMAIL_INVALID
                    else -> null
                },
            password =
                when {
                    password.isEmpty() -> RegisterUiState.FieldError.REQUIRED
                    password.length < MIN_PASSWORD_LENGTH -> RegisterUiState.FieldError.PASSWORD_TOO_SHORT
                    else -> null
                },
            confirmPassword =
                when {
                    confirmPassword.isEmpty() -> RegisterUiState.FieldError.REQUIRED
                    confirmPassword != password -> RegisterUiState.FieldError.PASSWORDS_DO_NOT_MATCH
                    else -> null
                },
        )

    private fun parseBirthDate(value: String): LocalDate? =
        try {
            LocalDate.parse(value.trim(), BIRTH_DATE_FORMATTER)
        } catch (_: DateTimeParseException) {
            null
        }

    private fun normalizePhoneNumber(value: String): String = value.trim().replace(PHONE_NUMBER_SEPARATOR_PATTERN, "")

    private fun AuthRepository.RegistrationResult.toUiState(): RegisterUiState =
        when (this) {
            AuthRepository.RegistrationResult.Success ->
                RegisterUiState(isRegistrationSuccessful = true)

            is AuthRepository.RegistrationResult.Failure ->
                when (reason) {
                    AuthRepository.RegistrationFailureReason.EMAIL_ALREADY_USED ->
                        RegisterUiState(
                            fieldErrors =
                                RegisterFieldErrors(
                                    email = RegisterUiState.FieldError.EMAIL_ALREADY_USED,
                                ),
                        )

                    AuthRepository.RegistrationFailureReason.USERNAME_ALREADY_USED ->
                        RegisterUiState(
                            fieldErrors =
                                RegisterFieldErrors(
                                    username = RegisterUiState.FieldError.USERNAME_ALREADY_USED,
                                ),
                        )

                    AuthRepository.RegistrationFailureReason.WEAK_PASSWORD ->
                        RegisterUiState(
                            fieldErrors =
                                RegisterFieldErrors(
                                    password = RegisterUiState.FieldError.PASSWORD_TOO_SHORT,
                                ),
                        )

                    AuthRepository.RegistrationFailureReason.NETWORK ->
                        RegisterUiState(generalError = RegisterUiState.GeneralError.NETWORK)

                    AuthRepository.RegistrationFailureReason.TOO_MANY_REQUESTS ->
                        RegisterUiState(generalError = RegisterUiState.GeneralError.TOO_MANY_REQUESTS)

                    AuthRepository.RegistrationFailureReason.SERVICE_UNAVAILABLE ->
                        RegisterUiState(generalError = RegisterUiState.GeneralError.SERVICE_UNAVAILABLE)

                    AuthRepository.RegistrationFailureReason.UNKNOWN ->
                        RegisterUiState(generalError = RegisterUiState.GeneralError.UNKNOWN)
                }
        }

    companion object {
        private const val MIN_FULL_NAME_LENGTH = 2
        private val USERNAME_PATTERN = Regex("^[a-z0-9._]{4,20}$")
        private val PHONE_NUMBER_PATTERN = Regex("^\\+?[0-9]{9,15}$")
        private val PHONE_NUMBER_SEPARATOR_PATTERN = Regex("[\\s()-]")
        private val BIRTH_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(BIRTH_DATE_PATTERN).withResolverStyle(ResolverStyle.STRICT)

        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    RegisterViewModel(authRepository)
                }
            }
    }
}
