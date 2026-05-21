package org.agrfesta.sh.ui.navigation

import androidx.compose.runtime.Composable
import org.agrfesta.sh.ui.startup.StartupUiState

@Composable
fun PikestaApp(uiState: StartupUiState) {
    when (uiState) {
        StartupUiState.TokenPresent -> AppNavGraph(startDestination = Routes.HOME)
        StartupUiState.TokenAbsent -> AppNavGraph(startDestination = Routes.AUTH)
        StartupUiState.Loading -> {}
    }
}
