package com.tangkinhcac.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

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
}
