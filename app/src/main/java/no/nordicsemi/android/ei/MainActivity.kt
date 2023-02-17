/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.ui.LoadingProgressIndicator
import no.nordicsemi.android.ei.ui.theme.NordicTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up the splash screen.
        //
        // On Android 12+ the splash screen will be animated, while on 6 - 11 will present a still
        // image. See more: https://developer.android.com/guide/topics/ui/splash-screen/
        val splashScreen = installSplashScreen()

        // Animated Vector Drawable is only supported on API 31+.
        var splashScreenVisible by mutableStateOf(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && coldStart) {
            coldStart = false
            val then = System.currentTimeMillis()
            splashScreen.setKeepOnScreenCondition {
                val now = System.currentTimeMillis()
                splashScreenVisible = now < then + 900
                splashScreenVisible
            }
        } else {
            splashScreenVisible = false
        }

        setContent {
            NordicTheme {
                // Loading indicator is shown behind the splash screen. Otherwise, in case the
                // user is not logged in, LoginActivity would be displayed immediately.
                if (splashScreenVisible) {
                    LoadingProgressIndicator()
                } else {
                    Navigation(
                        onCancelled = { finish() }
                    )
                }
            }
        }
    }

    companion object {
        // This flag is false when the app is first started (cold start).
        // In this case, the animation will be fully shown (1 sec).
        // Subsequent launches will display it only briefly.
        // It is only used on API 31+
        private var coldStart = true
    }
}

fun showSnackbar(
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    message: String,
) {
    coroutineScope.launch {
        snackbarHostState.showSnackbar(message = message)
    }
}

@Composable
fun ShowDialog(
    @DrawableRes drawableRes: Int,
    title: String,
    onDismissed: () -> Unit,
    properties: DialogProperties,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissed,
        properties = properties,
        content = {
            Surface(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(shape = RoundedCornerShape(4.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 8.dp, bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = drawableRes),
                            contentDescription = null,
                            tint = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            color = MaterialTheme.colors.onSurface,
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    content()
                }
            }
        }
    )
}

@Composable
fun ShowDialog(
    imageVector: ImageVector,
    title: String,
    onDismissRequest: () -> Unit,
    properties: DialogProperties,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
        content = {
            Surface(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(shape = RoundedCornerShape(4.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 8.dp, bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = imageVector,
                            contentDescription = null,
                            tint = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            color = MaterialTheme.colors.onSurface,
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    content()
                }
            }
        }
    )
}

@Composable
fun ShowDataAcquisitionDialog(
    imageVector: ImageVector,
    title: String,
    onDismissRequest: () -> Unit,
    properties: DialogProperties,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
        content = {
            Surface(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(shape = RoundedCornerShape(4.dp))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(start = 24.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = imageVector,
                            contentDescription = null,
                            tint = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            color = MaterialTheme.colors.onSurface,
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    content()
                }
            }
        }
    )
}