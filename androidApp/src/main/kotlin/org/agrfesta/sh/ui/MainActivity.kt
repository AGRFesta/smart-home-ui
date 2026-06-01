package org.agrfesta.sh.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import org.agrfesta.sh.ui.api.KtorHomeStreamApiClient
import org.agrfesta.sh.ui.auth.AuthViewModel
import org.agrfesta.sh.ui.home.HomeViewModel
import org.agrfesta.sh.ui.navigation.PikestaApp
import org.agrfesta.sh.ui.platform.AndroidTokenRepository
import org.agrfesta.sh.ui.startup.StartupViewModel

class MainActivity : ComponentActivity() {

    companion object {
        internal var dependencyFactory: ((Context) -> AppDependencies)? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val deps = dependencyFactory?.invoke(applicationContext) ?: AppDependencies(
            tokenRepository = AndroidTokenRepository(applicationContext),
            homeStreamApiClient = KtorHomeStreamApiClient(baseUrl = BuildConfig.BASE_URL),
        )

        val startupViewModel = StartupViewModel(deps.tokenRepository, lifecycleScope)
        val authViewModel = AuthViewModel(deps.tokenRepository, lifecycleScope)
        val homeViewModel = HomeViewModel(deps.homeStreamApiClient, deps.tokenRepository, lifecycleScope)
        startupViewModel.checkToken()

        setContent {
            val uiState by startupViewModel.uiState.collectAsState()
            PikestaApp(uiState = uiState, authViewModel = authViewModel, homeViewModel = homeViewModel)
        }
    }
}
