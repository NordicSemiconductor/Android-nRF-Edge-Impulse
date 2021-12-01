package no.nordicsemi.android.ei.ui

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.account.AccountHelper
import no.nordicsemi.android.ei.ui.theme.NordicTheme
import no.nordicsemi.android.ei.viewmodels.SplashscreenViewModel
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@Composable
fun SplashScreen(
    viewModel: SplashscreenViewModel = viewModel(),
    onLoggedIn: () -> Unit = {},
    onError: () -> Unit = {}
) {
    val isLargeScreen =
        LocalConfiguration.current.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val offset by remember {
        mutableStateOf(
            if (isLandscape && !isLargeScreen) {
                -25
            } else {
                -100
            }
        )
    }
    var progressMessage by rememberSaveable { mutableStateOf("") }
    LoadUserData(
        viewModel = viewModel,
        onProgressChanged = {
            progressMessage = it
        },
        onLoggedIn = onLoggedIn,
        onError = onError
    )
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.colorNordicLogoTop))
    ) {
        Image(
            painter = painterResource(id = R.drawable.nordic_logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Text(
            text = progressMessage,
            modifier = Modifier.offset(y = (offset).dp),
            color = MaterialTheme.colors.onSurface
        )
    }
}

@Composable
private fun LoadUserData(
    viewModel: SplashscreenViewModel = viewModel(),
    onProgressChanged: (message: String) -> Unit = {},
    onLoggedIn: () -> Unit = {},
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
                onLoggedIn()
            } catch (e: UnknownHostException) {
                onProgressChanged(activity.getString(R.string.error_no_internet))
            } catch (e: SocketTimeoutException) {
                onProgressChanged(activity.getString(R.string.error_timeout))
            } catch (e: HttpException) {
                if (e.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    AccountHelper.invalidateAuthToken(token, activity)
                    continue
                } else {
                    onProgressChanged(
                        e.message() ?: activity.getString(R.string.error_obtaining_user_data_failed)
                    )
                }
            }
            break
        }
    }
}

@Preview(name = "Light")
@Composable
fun SplashscreenPreviewLight() {
    NordicTheme(darkTheme = false) {
        SplashScreen()
    }
}