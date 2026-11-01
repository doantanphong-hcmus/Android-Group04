package com.tangkinhcac.app

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tangkinhcac.app.data.local.TangKinhCacDatabase
import com.tangkinhcac.app.data.repository.AuthRepository
import com.tangkinhcac.app.data.repository.FirebaseAuthRepository

class TangKinhCacApplication : Application() {
    // Storage services stay lazy so application startup does not open resources before a repository needs them.
    internal val localDatabase: TangKinhCacDatabase by lazy {
        TangKinhCacDatabase.create(applicationContext)
    }

    internal val firebaseStorage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    // A single repository instance keeps Firebase construction out of Activities, Fragments, and ViewModels.
    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(
            firebaseAuth = FirebaseAuth.getInstance(),
            firestore = FirebaseFirestore.getInstance(),
        )
    }
}
