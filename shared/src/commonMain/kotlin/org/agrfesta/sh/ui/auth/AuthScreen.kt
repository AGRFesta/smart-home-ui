package org.agrfesta.sh.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    Box(modifier = Modifier.testTag("auth_screen")) {
        AuthContent(onTokenSaved = viewModel::saveToken)
    }
}

@Composable
expect fun AuthContent(onTokenSaved: (String) -> Unit)
