package com.tangkinhcac.app.data.remote.auth

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

internal interface AuthApi {
    @POST("auth/register")
    fun register(
        @Body request: RegisterApiRequest,
    ): Call<RegisterApiResponse>

    @POST("auth/login")
    fun login(
        @Body request: LoginApiRequest,
    ): Call<LoginApiResponse>

    @GET("auth/me")
    fun getCurrentUser(): Call<CurrentUserApiResponse>

    @POST("auth/logout")
    fun logout(): Call<Unit>
}
