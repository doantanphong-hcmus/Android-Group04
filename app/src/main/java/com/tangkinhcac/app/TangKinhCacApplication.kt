package com.tangkinhcac.app

import android.app.Application
import com.google.firebase.storage.FirebaseStorage
import com.tangkinhcac.app.data.local.TangKinhCacDatabase
import com.tangkinhcac.app.data.repository.AuthRepository
import com.tangkinhcac.app.data.repository.UnavailableAuthRepository

class TangKinhCacApplication : Application() {
    // Storage services stay lazy so application startup does not open resources before a repository needs them.
    internal val localDatabase: TangKinhCacDatabase by lazy {
        TangKinhCacDatabase.create(applicationContext)
    }

    internal val firebaseStorage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    // Keep one repository instance at the application boundary until the backend client is connected.
    val authRepository: AuthRepository by lazy {
        UnavailableAuthRepository()
    }
}
