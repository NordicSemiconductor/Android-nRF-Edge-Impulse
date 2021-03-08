package no.nordicsemi.android.ei

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import no.nordicsemi.android.ei.ui.Login

@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    refreshToolbar: (visible: Boolean, title: String?) -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            refreshToolbar(false, "Login")
            Login(
                modifier = modifier,
            )
        }

        composable("projects") {
            refreshToolbar(false, "Projects")
            Text(text = "Projects")
        }
    }
}