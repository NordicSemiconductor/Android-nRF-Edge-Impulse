package no.nordicsemi.android.ei

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.service.param.LoginResponse
import no.nordicsemi.android.ei.ui.Login
import no.nordicsemi.android.ei.ui.Projects
import no.nordicsemi.android.ei.viewmodels.LoginViewModel
import no.nordicsemi.android.ei.viewmodels.ProjectsViewModel

@ExperimentalCoroutinesApi
@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    refreshToolbar: (visible: Boolean, title: String?) -> Unit
) {
    val navController = rememberNavController()
    var token by remember { mutableStateOf("") }
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { backStackEntry ->
            refreshToolbar(false, "Login")
            val viewModel: LoginViewModel = viewModel(
                key = "Login",
                HiltViewModelFactory(LocalContext.current, backStackEntry)
            )
            val username: String by viewModel.username.observeAsState("")
            val password: String by viewModel.password.observeAsState("")
            val loginResponse: LoginResponse by viewModel.loginResponse.observeAsState(LoginResponse())
            if (loginResponse.success) {
                token = loginResponse.token!!
                navController.navigate("projects")
            }
            Login(
                modifier = modifier,
                username = username,
                onUsernameChange = { viewModel.onUsernameChange(username = it) },
                password = password,
                onPasswordChange = { viewModel.onPasswordChange(password = it) },
                onLogin = { u, p ->
                    viewModel.login(
                        username = u, password = p
                    )
                }
            )
        }

        composable("projects") {
            refreshToolbar(true, "Projects")
            val viewModel: ProjectsViewModel = viewModel(
                key = "Projects",
                HiltViewModelFactory(
                    LocalContext.current,
                    navController.previousBackStackEntry!!
                )
            )
            val projects: List<Project> by viewModel.projects.observeAsState(listOf())
            viewModel.projects(token = token)
            Projects(
                modifier = modifier,
                token = token,
                onRefresh = {
                    viewModel.projects(token = it)
                },
                projects = projects
            )

        }
    }
}