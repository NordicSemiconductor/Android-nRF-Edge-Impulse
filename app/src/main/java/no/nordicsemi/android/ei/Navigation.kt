package no.nordicsemi.android.ei

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import no.nordicsemi.android.ei.ui.Splashscreen
import no.nordicsemi.android.ei.viewmodels.SplashscreenViewModel

@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    refreshToolbar: (visible: Boolean, title: String?) -> Unit,
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splashscreen") {

        composable(Route.splashscreen) { backStackEntry ->
            refreshToolbar(false, "Splashscreen")
            val viewModel: SplashscreenViewModel = viewModel(
                factory = HiltViewModelFactory(LocalContext.current, backStackEntry)
            )
            Splashscreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Route.projects) {
            refreshToolbar(false, "Projects")
            Text(text = "Projects")
        }
    }
}