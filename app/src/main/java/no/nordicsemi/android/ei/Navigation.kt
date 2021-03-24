package no.nordicsemi.android.ei

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import no.nordicsemi.android.ei.ui.Splashscreen
import no.nordicsemi.android.ei.ui.Dashboard
import no.nordicsemi.android.ei.viewmodels.SplashscreenViewModel
import no.nordicsemi.android.ei.viewmodels.UserViewModel

@Composable
fun Navigation(
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Route.splashscreen) {
        composable(Route.splashscreen) { backStackEntry ->
            val viewModel: SplashscreenViewModel = viewModel(
                factory = HiltViewModelFactory(LocalContext.current, backStackEntry)
            )
            Splashscreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Route.user) { backStackEntry ->
            val viewModel: UserViewModel = viewModel(
                factory = HiltViewModelFactory(LocalContext.current, backStackEntry)
            )
            Dashboard(
                user = viewModel.user,
                refreshingState = viewModel.pullToRefresh,
                onRefresh = {
                    viewModel.refreshUser()
                },
                onCreateNewProject = {

                },
                onLogoutClick = {
                    viewModel.logout()
                    navController.navigateUp()
                }
            )
        }
    }
}