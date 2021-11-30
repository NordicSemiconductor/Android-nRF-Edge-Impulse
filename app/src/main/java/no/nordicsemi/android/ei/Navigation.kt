package no.nordicsemi.android.ei

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import no.nordicsemi.android.ei.ui.Dashboard
import no.nordicsemi.android.ei.ui.Project
import no.nordicsemi.android.ei.ui.SplashScreen

@Composable
fun Navigation(
    onError: () -> Unit = {},
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Route.splashscreen) {
        composable(Route.splashscreen) {
            SplashScreen(
                viewModel = hiltViewModel(),
                onLoggedIn = {
                    navController.navigate(Route.dashboard) {
                        popUpTo(Route.splashscreen) {
                            inclusive = true
                        }
                    }
                },
                onError = onError,
            )
        }

        composable(Route.dashboard) {
            Dashboard(
                viewModel = hiltViewModel(),
                onProjectSelected = {
                    navController.navigate(Route.project)
                },
                onLogout = {
                    navController.navigate(Route.splashscreen) {
                        popUpTo(Route.dashboard) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(Route.project) {
            Project(
                viewModel = hiltViewModel(),
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}
