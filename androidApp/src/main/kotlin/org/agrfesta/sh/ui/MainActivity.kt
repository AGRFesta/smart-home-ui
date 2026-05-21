package org.agrfesta.sh.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import org.agrfesta.sh.ui.api.HomeApiClient
import org.agrfesta.sh.ui.api.HomeApiResult
import org.agrfesta.sh.ui.auth.AuthViewModel
import org.agrfesta.sh.ui.home.HomeViewModel
import org.agrfesta.sh.ui.navigation.PikestaApp
import org.agrfesta.sh.ui.platform.AndroidTokenRepository
import org.agrfesta.sh.ui.startup.StartupViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val tokenRepository = AndroidTokenRepository(applicationContext)
        val homeApiClient = object : HomeApiClient {
            override suspend fun fetchHome(token: String): HomeApiResult =
                throw RuntimeException("HomeApiClient not yet implemented — see follow-up issue")
        }
        val startupViewModel = StartupViewModel(tokenRepository, lifecycleScope)
        val authViewModel = AuthViewModel(tokenRepository, lifecycleScope)
        val homeViewModel = HomeViewModel(homeApiClient, tokenRepository, lifecycleScope)
        startupViewModel.checkToken()

        setContent {
            val uiState by startupViewModel.uiState.collectAsState()
            PikestaApp(uiState = uiState, authViewModel = authViewModel, homeViewModel = homeViewModel)
        }
    }
}
