package org.agrfesta.sh.ui.navigation

import androidx.compose.runtime.Composable
import org.agrfesta.sh.ui.auth.AuthViewModel
import org.agrfesta.sh.ui.startup.StartupUiState

@Composable
fun PikestaApp(uiState: StartupUiState, authViewModel: AuthViewModel) {
    when (uiState) {
        StartupUiState.TokenPresent -> AppNavGraph(startDestination = Routes.HOME, authViewModel = authViewModel)
        StartupUiState.TokenAbsent -> AppNavGraph(startDestination = Routes.AUTH, authViewModel = authViewModel)
        StartupUiState.Loading -> {}
    }
}
