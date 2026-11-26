package com.tangkinhcac.app

import android.app.Application
import android.os.Build
import com.google.gson.Gson
import com.tangkinhcac.app.data.local.TangKinhCacDatabase
import com.tangkinhcac.app.data.local.auth.AuthTokenStore
import com.tangkinhcac.app.data.local.auth.KeystoreAuthTokenStore
import com.tangkinhcac.app.data.remote.auth.AuthApi
import com.tangkinhcac.app.data.remote.auth.AuthRequestInterceptor
import com.tangkinhcac.app.data.remote.auth.RetrofitAuthGateway
import com.tangkinhcac.app.data.repository.AuthRepository
import com.tangkinhcac.app.data.repository.BackendAuthRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TangKinhCacApplication : Application() {
    // Storage services stay lazy so application startup does not open resources before a repository needs them.
    internal val localDatabase: TangKinhCacDatabase by lazy {
        TangKinhCacDatabase.create(applicationContext)
    }

    private val authTokenStore: AuthTokenStore by lazy {
        KeystoreAuthTokenStore(applicationContext)
    }

    private val gson: Gson by lazy { Gson() }

    private val authApi: AuthApi by lazy {
        val httpClient =
            OkHttpClient
                .Builder()
                .addInterceptor(AuthRequestInterceptor(authTokenStore))
                .build()
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AuthApi::class.java)
    }

    // A single repository owns token lifecycle and keeps HTTP details outside the UI layer.
    val authRepository: AuthRepository by lazy {
        BackendAuthRepository(
            gateway = RetrofitAuthGateway(authApi, gson),
            tokenStore = authTokenStore,
            deviceName = deviceName(),
        )
    }

    private fun deviceName(): String {
        // The API requires a stable human-readable label no longer than 100 characters.
        return "${Build.MANUFACTURER} ${Build.MODEL}".trim().ifEmpty { "Android" }.take(100)
    }
}
