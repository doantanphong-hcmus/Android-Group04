package com.tangkinhcac.app.feature.auth

data class RegisterUiState(
    val isLoading: Boolean = false,
    val isRegistrationSuccessful: Boolean = false,
    val fieldErrors: RegisterFieldErrors = RegisterFieldErrors(),
    val generalError: GeneralError? = null,
) {
    enum class Field {
        FULL_NAME,
        USERNAME,
        BIRTH_DATE,
        PHONE_NUMBER,
        EMAIL,
        PASSWORD,
        CONFIRM_PASSWORD,
    }

    enum class FieldError {
        REQUIRED,
        FULL_NAME_TOO_SHORT,
        USERNAME_INVALID,
        USERNAME_ALREADY_USED,
        BIRTH_DATE_INVALID,
        BIRTH_DATE_IN_FUTURE,
        PHONE_NUMBER_INVALID,
        EMAIL_INVALID,
        EMAIL_ALREADY_USED,
        PASSWORD_TOO_SHORT,
        PASSWORDS_DO_NOT_MATCH,
    }

    enum class GeneralError {
        NETWORK,
        TOO_MANY_REQUESTS,
        SERVICE_UNAVAILABLE,
        UNKNOWN,
    }
}

data class RegisterFieldErrors(
    val fullName: RegisterUiState.FieldError? = null,
    val username: RegisterUiState.FieldError? = null,
    val birthDate: RegisterUiState.FieldError? = null,
    val phoneNumber: RegisterUiState.FieldError? = null,
    val email: RegisterUiState.FieldError? = null,
    val password: RegisterUiState.FieldError? = null,
    val confirmPassword: RegisterUiState.FieldError? = null,
) {
    val hasErrors: Boolean
        get() =
            fullName != null ||
                username != null ||
                birthDate != null ||
                phoneNumber != null ||
                email != null ||
                password != null ||
                confirmPassword != null
}
