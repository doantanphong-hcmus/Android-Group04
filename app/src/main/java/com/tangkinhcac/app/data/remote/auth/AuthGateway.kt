package com.tangkinhcac.app.data.remote.auth

import com.tangkinhcac.app.data.model.RegistrationRequest
import com.tangkinhcac.app.data.model.UserAccount

interface AuthGateway {
    fun register(
        request: RegistrationRequest,
        onResult: (AuthGatewayResult<UserAccount>) -> Unit,
    )

    fun login(
        email: String,
        password: String,
        deviceName: String,
        onResult: (AuthGatewayResult<AuthSession>) -> Unit,
    )

    fun getCurrentUser(onResult: (AuthGatewayResult<UserAccount>) -> Unit)

    fun logout(onResult: (AuthGatewayResult<Unit>) -> Unit)
}

data class AuthSession(
    val token: String,
    val user: UserAccount,
)

sealed interface AuthGatewayResult<out ValueT> {
    data class Success<ValueT>(
        val value: ValueT,
    ) : AuthGatewayResult<ValueT>

    data class Failure(
        val errorCode: String? = null,
        val httpStatus: Int? = null,
        val invalidFields: Set<String> = emptySet(),
        val requestId: String? = null,
        val isNetworkError: Boolean = false,
    ) : AuthGatewayResult<Nothing>
}
