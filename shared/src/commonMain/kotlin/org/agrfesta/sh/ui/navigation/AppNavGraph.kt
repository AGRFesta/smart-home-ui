package org.agrfesta.sh.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.agrfesta.sh.ui.auth.AuthScreen
import org.agrfesta.sh.ui.auth.AuthViewModel
import org.agrfesta.sh.ui.home.HomeScreen

@Composable
fun AppNavGraph(
    startDestination: String = Routes.HOME,
    authViewModel: AuthViewModel,
) {
    val navController = rememberNavController()
    LaunchedEffect(authViewModel) {
        authViewModel.navigationEvent.collect {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.AUTH) { inclusive = true }
            }
        }
    }
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.HOME) { HomeScreen() }
        composable(Routes.AUTH) { AuthScreen(viewModel = authViewModel) }
    }
}
