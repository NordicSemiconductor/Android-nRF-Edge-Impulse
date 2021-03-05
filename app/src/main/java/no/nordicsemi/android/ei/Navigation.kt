package no.nordicsemi.android.ei

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import no.nordicsemi.android.ei.ui.Login

@Composable
fun Navigation(
    tabBarSpec: (visible: Boolean, title: String?) -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            tabBarSpec(false, null)
            Login(navigation = navController)
        }

        composable("projects") {
            tabBarSpec(false, "Projects")
            Text(text = "Projects")
        }
    }
}