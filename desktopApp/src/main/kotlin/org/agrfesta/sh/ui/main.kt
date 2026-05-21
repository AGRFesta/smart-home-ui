package org.agrfesta.sh.ui

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.MainScope
import org.agrfesta.sh.ui.api.HomeApiClient
import org.agrfesta.sh.ui.api.HomeApiResult
import org.agrfesta.sh.ui.auth.AuthViewModel
import org.agrfesta.sh.ui.home.HomeViewModel
import org.agrfesta.sh.ui.navigation.PikestaApp
import org.agrfesta.sh.ui.platform.DesktopTokenRepository
import org.agrfesta.sh.ui.startup.StartupViewModel
import java.nio.file.Path

fun main() = application {
    val scope = remember { MainScope() }
    val tokenRepository = remember {
        DesktopTokenRepository(Path.of(System.getProperty("user.home"), ".pikesta"))
    }
    val homeApiClient = remember {
        object : HomeApiClient {
            override suspend fun fetchHome(token: String): HomeApiResult =
                throw RuntimeException("HomeApiClient not yet implemented — see follow-up issue")
        }
    }
    val startupViewModel = remember { StartupViewModel(tokenRepository, scope) }
    val authViewModel = remember { AuthViewModel(tokenRepository, scope) }
    val homeViewModel = remember { HomeViewModel(homeApiClient, tokenRepository, scope) }
    startupViewModel.checkToken()

    val uiState by startupViewModel.uiState.collectAsState()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Pikesta",
    ) {
        PikestaApp(uiState = uiState, authViewModel = authViewModel, homeViewModel = homeViewModel)
    }
}
