package com.tangkinhcac.app.data.remote.auth

import com.google.gson.annotations.SerializedName

internal data class RegisterApiRequest(
    @SerializedName("full_name") val fullName: String,
    val username: String,
    @SerializedName("birth_date") val birthDate: String,
    @SerializedName("phone_number") val phoneNumber: String,
    val email: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
)

internal data class LoginApiRequest(
    val email: String,
    val password: String,
    @SerializedName("device_name") val deviceName: String,
)

internal data class RegisterApiResponse(
    val data: RegisterApiData,
)

internal data class RegisterApiData(
    val user: UserApiModel,
)

internal data class LoginApiResponse(
    val data: LoginApiData,
)

internal data class LoginApiData(
    val token: String,
    @SerializedName("token_type") val tokenType: String,
    val user: UserApiModel,
)

internal data class CurrentUserApiResponse(
    val data: UserApiModel,
)

internal data class UserApiModel(
    val id: Long,
    @SerializedName("full_name") val fullName: String,
    val username: String,
    @SerializedName("birth_date") val birthDate: String,
    @SerializedName("phone_number") val phoneNumber: String,
    val email: String,
    val role: String,
)

internal data class ErrorApiResponse(
    val error: ErrorApiModel,
)

internal data class ErrorApiModel(
    val code: String,
    val message: String,
    val fields: Map<String, List<String>>? = null,
    @SerializedName("request_id") val requestId: String,
)
