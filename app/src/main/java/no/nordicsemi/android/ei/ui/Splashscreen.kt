package no.nordicsemi.android.ei.ui

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import androidx.navigation.compose.popUpTo
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.Route
import no.nordicsemi.android.ei.account.AccountHelper
import no.nordicsemi.android.ei.ui.theme.NordicTheme
import no.nordicsemi.android.ei.viewmodels.SplashscreenViewModel
import retrofit2.HttpException

@Composable
fun Splashscreen(
    navController: NavHostController,
    viewModel: SplashscreenViewModel
) {
    var progressMessage by rememberSaveable { mutableStateOf("") }
    val activity = LocalContext.current as Activity

    LaunchedEffect(key1 = "logging in") {
        val account = AccountHelper.getOrCreateAccount(activity).getOrElse {
            activity.finish()
            return@LaunchedEffect
        }
        while (true) {
            progressMessage = activity.getString(R.string.label_obtaining_token)
            val token = AccountHelper.getAuthToken(account, activity).getOrElse {
                progressMessage = it.localizedMessage ?: activity.getString(R.string.error_obtaining_token_failed)
                return@LaunchedEffect
            }
            progressMessage = activity.getString(R.string.label_obtaining_user_data)
            try {
                viewModel.getUserData(token)
                navController.navigate(Route.projects) {
                    popUpTo(Route.splashscreen) {
                        inclusive = true
                    }
                }
                return@LaunchedEffect
            } catch (e: HttpException) {
                if (e.code() == 302) { // Moved Temporarily
                    AccountHelper.invalidateAuthToken(token, activity)
                } else {
                    progressMessage = e.message() ?: activity.getString(R.string.error_obtaining_user_data_failed)
                    return@LaunchedEffect
                }
            }
        }
    }
    SplashscreenView(
        progressMessage = progressMessage
    )
}

@Composable
private fun SplashscreenView(
    progressMessage: String? = null
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.nordic_logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        progressMessage?.let { text ->
            Text(
                text = text,
                modifier = Modifier.offset(y = (-100).dp)
            )
        }
    }
}

@Preview(name = "Light")
@Composable
fun SplashscreenPreviewLight() {
    NordicTheme(darkTheme = false) {
        SplashscreenView("Logging In...")
    }
}

@Preview(name = "Dark")
@Composable
fun SplashscreenPreviewDark() {
    NordicTheme(darkTheme = true) {
        SplashscreenView("Logging In...")
    }
}