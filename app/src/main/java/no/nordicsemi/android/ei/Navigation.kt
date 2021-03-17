package no.nordicsemi.android.ei

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import no.nordicsemi.android.ei.ui.Projects
import no.nordicsemi.android.ei.ui.Splashscreen
import no.nordicsemi.android.ei.viewmodels.ProjectsViewModel
import no.nordicsemi.android.ei.viewmodels.SplashscreenViewModel

@ExperimentalCoroutinesApi
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
            refreshToolbar(true, "Projects")
            val viewModel: ProjectsViewModel = viewModel(
                key = "Projects",
                HiltViewModelFactory(
                    LocalContext.current,
                    it
                )
            )
            val projects by viewModel.projects.observeAsState(listOf())
            val refreshingState by viewModel.pullToRefresh.observeAsState(false)

            Projects(
                modifier = modifier,
                projects = projects,
                refreshingState = refreshingState,
                onRefresh = {
                    viewModel.loadProjects(true)
                },
                onItemClick = { projectId ->
                    viewModel.getDevelopmentKeys(projectId)
                }
            )
        }
    }
}