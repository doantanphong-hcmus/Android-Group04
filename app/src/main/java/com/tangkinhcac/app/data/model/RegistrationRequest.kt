package com.tangkinhcac.app.data.model

data class RegistrationRequest(
    val fullName: String,
    val username: String,
    val normalizedUsername: String,
    val birthDate: String,
    val phoneNumber: String,
    val email: String,
    val password: String,
)
