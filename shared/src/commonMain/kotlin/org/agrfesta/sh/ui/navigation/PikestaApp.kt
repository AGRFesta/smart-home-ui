package org.agrfesta.sh.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.agrfesta.sh.ui.auth.AuthViewModel
import org.agrfesta.sh.ui.startup.StartupUiState

@Composable
fun PikestaApp(uiState: StartupUiState, authViewModel: AuthViewModel) {
    when (uiState) {
        StartupUiState.TokenPresent -> AppNavGraph(startDestination = Routes.HOME, authViewModel = authViewModel)
        StartupUiState.TokenAbsent -> AppNavGraph(startDestination = Routes.AUTH, authViewModel = authViewModel)
        StartupUiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.testTag("loading_indicator"))
        }
    }
}
