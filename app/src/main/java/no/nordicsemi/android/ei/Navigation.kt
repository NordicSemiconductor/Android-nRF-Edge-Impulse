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
import no.nordicsemi.android.ei.ui.Login
import no.nordicsemi.android.ei.viewmodels.LoginViewModel

@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    refreshToolbar: (visible: Boolean, title: String?) -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { backStackEntry ->
            refreshToolbar(false, "Login")
            val viewModel: LoginViewModel = viewModel(
                key = "Login",
                HiltViewModelFactory(LocalContext.current, backStackEntry)
            )
            Login(
                modifier = modifier,
                viewModel = viewModel
            )
        }

        composable("projects") {
            refreshToolbar(false, "Projects")
            Text(text = "Projects")
        }
    }
}