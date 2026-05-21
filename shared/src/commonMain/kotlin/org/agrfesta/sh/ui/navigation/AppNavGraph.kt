package org.agrfesta.sh.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.agrfesta.sh.ui.auth.AuthScreen
import org.agrfesta.sh.ui.home.HomeScreen

@Composable
fun AppNavGraph(startDestination: String = Routes.HOME) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.HOME) { HomeScreen() }
        composable(Routes.AUTH) { AuthScreen() }
    }
}
