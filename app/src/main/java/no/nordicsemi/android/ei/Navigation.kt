package no.nordicsemi.android.ei

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import no.nordicsemi.android.ei.account.AccountHelper
import no.nordicsemi.android.ei.ui.Dashboard
import no.nordicsemi.android.ei.ui.Splashscreen
import no.nordicsemi.android.ei.viewmodels.SplashscreenViewModel
import no.nordicsemi.android.ei.viewmodels.UserViewModel
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.net.UnknownHostException

@Composable
fun Navigation(
    onError: () -> Unit = {},
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Route.splashscreen) {
        composable(Route.splashscreen) { backStackEntry ->
            var progressMessage by rememberSaveable { mutableStateOf("") }
            val viewModel: SplashscreenViewModel = viewModel(
                factory = HiltViewModelFactory(LocalContext.current, backStackEntry)
            )
            Login(
                viewModel = viewModel,
                onProgressChanged = { progressMessage = it },
                onLoggedIn = {
                    navController.navigate(Route.user) {
                        popUpTo(Route.splashscreen) {
                            inclusive = true
                        }
                    }
                },
                onError = onError,
            )
            Splashscreen(
                progressMessage = progressMessage,
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

@Composable
fun Login(
    viewModel: SplashscreenViewModel = viewModel(),
    onProgressChanged: (message: String) -> Unit = {},
    onLoggedIn: (token: String) -> Unit = {},
    onError: () -> Unit = {},
) {
    val activity = LocalContext.current as Activity

    LaunchedEffect(key1 = "logging in") {
        onProgressChanged("")
        val account = AccountHelper.getOrCreateAccount(activity).getOrElse {
            onError()
            return@LaunchedEffect
        }
        while (true) {
            onProgressChanged(activity.getString(R.string.label_logging_in))
            val token = AccountHelper.getAuthToken(account, activity).getOrElse {
                it.localizedMessage?.let { message ->
                    onProgressChanged(message)
                } ?: run {
                    onError()
                }
                return@LaunchedEffect
            }
            onProgressChanged(activity.getString(R.string.label_obtaining_user_data))
            try {
                viewModel.getUserData(token)
                onLoggedIn(token)
            } catch (e: UnknownHostException) {
                onProgressChanged(activity.getString(R.string.error_no_internet))
            } catch (e: HttpException) {
                if (e.code() == HttpURLConnection.HTTP_MOVED_TEMP) {
                    AccountHelper.invalidateAuthToken(token, activity)
                    continue
                } else {
                    onProgressChanged(e.message() ?: activity.getString(R.string.error_obtaining_user_data_failed))
                }
            }
            break
        }
    }
}