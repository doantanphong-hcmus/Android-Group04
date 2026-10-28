package com.tangkinhcac.app

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.tangkinhcac.app.data.repository.AuthRepository
import com.tangkinhcac.app.data.repository.FirebaseAuthRepository

class TangKinhCacApplication : Application() {
    // A single repository instance keeps Firebase construction out of Activities, Fragments, and ViewModels.
    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(FirebaseAuth.getInstance())
    }
}
