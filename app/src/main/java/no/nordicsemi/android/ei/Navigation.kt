package no.nordicsemi.android.ei

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import no.nordicsemi.android.ei.ui.*

@Composable
fun Navigation(
    onCancelled: () -> Unit = {},
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Route.login) {
        composable(Route.login) {
            Login(
                viewModel = hiltViewModel(),
                onLoggedIn = {
                    navController.navigate(Route.dashboard) {
                        popUpTo(Route.login) {
                            inclusive = true
                        }
                    }
                },
                onCancelled = onCancelled,
                onError = { onCancelled() },
            )
        }

        composable(Route.dashboard) {
            Dashboard(
                viewModel = hiltViewModel(),
                onProjectSelected = {
                    navController.navigate(Route.project)
                },
                onLogout = {
                    navController.navigate(Route.login) {
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
