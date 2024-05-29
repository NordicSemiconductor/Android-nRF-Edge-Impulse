/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.theme.NordicActivity
import no.nordicsemi.android.common.theme.NordicTheme

@AndroidEntryPoint
class MainActivity : NordicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            NordicTheme {
                // Loading indicator is shown behind the splash screen. Otherwise, in case the
                // user is not logged in, LoginActivity would be displayed immediately.
                /*if (splashScreenVisible) {
                    LoadingProgressIndicator()
                } else {
                }*/
                Navigation(onCancelled = { finish() })
            }
        }
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
fun ShowAlertDialog(
    imageVector: ImageVector,
    title: String,
    text: @Composable () -> Unit = {},
    dismissText: String? = null,
    onDismiss: () -> Unit = {},
    confirmText: String,
    onConfirm: () -> Unit,
    properties: DialogProperties = DialogProperties(),
) {
    AlertDialog(
        icon = { Icon(imageVector = imageVector, contentDescription = null) },
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        text = text,
        onDismissRequest = onDismiss,
        dismissButton = {
            dismissText?.let {
                TextButton(onClick = onDismiss) {
                    Text(text = dismissText)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText)
            }
        },
        properties = properties
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
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge,
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
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    content()
                }
            }
        }
    )
}