package com.tangkinhcac.app.data.local.auth

interface AuthTokenStore {
    val token: String?

    fun save(token: String): Boolean

    fun clear()
}
