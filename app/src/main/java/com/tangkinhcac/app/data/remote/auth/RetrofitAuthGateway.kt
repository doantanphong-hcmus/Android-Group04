package com.tangkinhcac.app.data.remote.auth

import com.google.gson.Gson
import com.tangkinhcac.app.data.model.RegistrationRequest
import com.tangkinhcac.app.data.model.UserAccount
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

/** Adapts Retrofit callbacks and the server envelope into an app-facing Auth contract. */
internal class RetrofitAuthGateway(
    private val api: AuthApi,
    private val gson: Gson,
) : AuthGateway {
    override fun register(
        request: RegistrationRequest,
        onResult: (AuthGatewayResult<UserAccount>) -> Unit,
    ) {
        api
            .register(
                RegisterApiRequest(
                    fullName = request.fullName,
                    username = request.username,
                    birthDate = request.birthDate,
                    phoneNumber = request.phoneNumber,
                    email = request.email,
                    password = request.password,
                    passwordConfirmation = request.password,
                ),
            ).enqueueMapped(
                transform = { response -> response.data.user.toUserAccount() },
                onResult = onResult,
            )
    }

    override fun login(
        email: String,
        password: String,
        deviceName: String,
        onResult: (AuthGatewayResult<AuthSession>) -> Unit,
    ) {
        api
            .login(
                LoginApiRequest(
                    email = email,
                    password = password,
                    deviceName = deviceName,
                ),
            ).enqueueMapped(
                transform = { response ->
                    require(response.data.tokenType == BEARER_TOKEN_TYPE)
                    require(response.data.token.isNotBlank())
                    AuthSession(
                        token = response.data.token,
                        user = response.data.user.toUserAccount(),
                    )
                },
                onResult = onResult,
            )
    }

    override fun getCurrentUser(onResult: (AuthGatewayResult<UserAccount>) -> Unit) {
        api.getCurrentUser().enqueueMapped(
            transform = { response -> response.data.toUserAccount() },
            onResult = onResult,
        )
    }

    override fun logout(onResult: (AuthGatewayResult<Unit>) -> Unit) {
        api.logout().enqueue(
            object : Callback<Unit> {
                override fun onResponse(
                    call: Call<Unit>,
                    response: Response<Unit>,
                ) {
                    if (response.isSuccessful) {
                        onResult(AuthGatewayResult.Success(Unit))
                    } else {
                        onResult(response.toFailure())
                    }
                }

                override fun onFailure(
                    call: Call<Unit>,
                    throwable: Throwable,
                ) {
                    onResult(throwable.toFailure())
                }
            },
        )
    }

    private fun <ResponseT, ValueT> Call<ResponseT>.enqueueMapped(
        transform: (ResponseT) -> ValueT,
        onResult: (AuthGatewayResult<ValueT>) -> Unit,
    ) {
        enqueue(
            object : Callback<ResponseT> {
                override fun onResponse(
                    call: Call<ResponseT>,
                    response: Response<ResponseT>,
                ) {
                    val body = response.body()
                    if (!response.isSuccessful || body == null) {
                        onResult(response.toFailure())
                        return
                    }

                    val value = runCatching { transform(body) }.getOrNull()
                    onResult(
                        if (value != null) {
                            AuthGatewayResult.Success(value)
                        } else {
                            AuthGatewayResult.Failure(httpStatus = response.code())
                        },
                    )
                }

                override fun onFailure(
                    call: Call<ResponseT>,
                    throwable: Throwable,
                ) {
                    onResult(throwable.toFailure())
                }
            },
        )
    }

    private fun Response<*>.toFailure(): AuthGatewayResult.Failure {
        val parsedError =
            errorBody()?.use { body ->
                runCatching { gson.fromJson(body.charStream(), ErrorApiResponse::class.java) }.getOrNull()
            }
        return AuthGatewayResult.Failure(
            errorCode = parsedError?.error?.code,
            httpStatus = code(),
            invalidFields =
                parsedError
                    ?.error
                    ?.fields
                    ?.keys
                    .orEmpty(),
            requestId = parsedError?.error?.requestId ?: headers()[REQUEST_ID_HEADER],
        )
    }

    private fun Throwable.toFailure(): AuthGatewayResult.Failure =
        AuthGatewayResult.Failure(isNetworkError = this is IOException)

    private fun UserApiModel.toUserAccount(): UserAccount =
        UserAccount(
            id = id,
            fullName = fullName,
            username = username,
            birthDate = birthDate,
            phoneNumber = phoneNumber,
            email = email,
            role = role,
        )

    private companion object {
        const val REQUEST_ID_HEADER = "X-Request-ID"
        const val BEARER_TOKEN_TYPE = "Bearer"
    }
}
