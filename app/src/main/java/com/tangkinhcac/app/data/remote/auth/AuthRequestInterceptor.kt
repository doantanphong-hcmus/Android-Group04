package com.tangkinhcac.app.data.remote.auth

import com.tangkinhcac.app.data.local.auth.AuthTokenStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthRequestInterceptor(
    private val tokenStore: AuthTokenStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder =
            chain
                .request()
                .newBuilder()
                .header(ACCEPT_HEADER, JSON_MEDIA_TYPE)
        tokenStore.token?.let { token ->
            builder.header(AUTHORIZATION_HEADER, "Bearer $token")
        }
        return chain.proceed(builder.build())
    }

    private companion object {
        const val ACCEPT_HEADER = "Accept"
        const val AUTHORIZATION_HEADER = "Authorization"
        const val JSON_MEDIA_TYPE = "application/json"
    }
}
