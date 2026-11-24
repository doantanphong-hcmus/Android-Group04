package com.tangkinhcac.app.data.model

data class UserAccount(
    val id: Long,
    val fullName: String,
    val username: String,
    val birthDate: String,
    val phoneNumber: String,
    val email: String,
    val role: String,
)
