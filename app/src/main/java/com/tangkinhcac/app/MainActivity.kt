package com.tangkinhcac.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {
    private val localNetworkPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // A denied permission is handled by the existing connection error in Auth screens.
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        requestDebugLocalNetworkPermission()

        if (savedInstanceState != null) {
            return
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_main)
        val application = application as TangKinhCacApplication

        // Choose the first screen before attaching the graph to avoid briefly flashing the login screen.
        navGraph.setStartDestination(
            if (application.authRepository.hasAuthenticatedUser) {
                R.id.homeFragment
            } else {
                R.id.loginFragment
            },
        )
        navController.graph = navGraph
    }

    private fun requestDebugLocalNetworkPermission() {
        if (!BuildConfig.DEBUG || Build.VERSION.SDK_INT < LOCAL_NETWORK_PERMISSION_API) {
            return
        }

        val permission = Manifest.permission.ACCESS_LOCAL_NETWORK
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            localNetworkPermissionLauncher.launch(permission)
        }
    }

    private companion object {
        const val LOCAL_NETWORK_PERMISSION_API = 37
    }
}
