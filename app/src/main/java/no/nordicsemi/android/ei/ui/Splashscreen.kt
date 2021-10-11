package no.nordicsemi.android.ei.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.ui.theme.NordicTheme

@Composable
fun Splashscreen(
    progressMessage: String? = null
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
        progressMessage?.let { text ->
            Text(
                text = text,
                modifier = Modifier.offset(y = (offset).dp),
                color = MaterialTheme.colors.onSurface
            )
        }
    }
}

@Preview(name = "Light")
@Composable
fun SplashscreenPreviewLight() {
    NordicTheme(darkTheme = false) {
        Splashscreen(stringResource(id = R.string.label_logging_in))
    }
}