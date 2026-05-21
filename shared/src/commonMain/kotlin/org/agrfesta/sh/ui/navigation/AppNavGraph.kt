package org.agrfesta.sh.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.agrfesta.sh.ui.auth.AuthScreen
import org.agrfesta.sh.ui.auth.AuthViewModel
import org.agrfesta.sh.ui.home.HomeScreen
import org.agrfesta.sh.ui.home.HomeViewModel

@Composable
fun AppNavGraph(
    startDestination: String = Routes.HOME,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
) {
    val navController = rememberNavController()
    var tokenInvalid by remember { mutableStateOf(false) }

    LaunchedEffect(authViewModel) {
        authViewModel.navigationEvent.collect {
            tokenInvalid = false
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.AUTH) { inclusive = true }
            }
        }
    }
    LaunchedEffect(homeViewModel) {
        homeViewModel.unauthorizedEvent.collect {
            tokenInvalid = true
            navController.navigate(Routes.AUTH) {
                popUpTo(Routes.HOME) { inclusive = true }
            }
        }
    }
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.HOME) { HomeScreen(viewModel = homeViewModel) }
        composable(Routes.AUTH) {
            AuthScreen(viewModel = authViewModel, tokenInvalid = tokenInvalid)
        }
    }
}
