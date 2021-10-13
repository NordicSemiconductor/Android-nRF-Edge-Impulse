package no.nordicsemi.android.ei.ui.layouts

import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BluetoothDisabled
import androidx.compose.material.icons.rounded.BluetoothSearching
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.util.Utils
import java.util.*

@Composable
fun InfoLayout(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun InfoLayout(
    iconPainter: Painter,
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.h6
            )
        }
    }
}

@Composable
fun NoConfiguredDevicesInfo(
    modifier: Modifier = Modifier
) {
    InfoLayout(
        modifier = modifier,
        imageVector = Icons.Rounded.DeveloperBoard
    ) {
        Text(
            text = stringResource(R.string.label_no_devices_connected),
            style = MaterialTheme.typography.h6
        )
    }
}

@Composable
fun NoDevicesInRangeInfo(
    modifier: Modifier = Modifier
) {
    InfoLayout(
        modifier = modifier,
        imageVector = Icons.Rounded.BluetoothSearching
    ) {
        Text(
            text = stringResource(id = R.string.thingy_guide_title),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.thingy_guide_info),
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun BluetoothDisabledInfo(
    modifier: Modifier = Modifier,
    onBluetoothEnabled: () -> Unit
) {
    InfoLayout(
        modifier = modifier,
        imageVector = Icons.Rounded.BluetoothDisabled
    ) {
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                onBluetoothEnabled()
            }
        }
        Text(
            text = stringResource(id = R.string.bluetooth_disabled_title),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.bluetooth_disabled_info),
            style = MaterialTheme.typography.body1
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                if (!Utils.isBluetoothEnabled(context = context)) {
                    launcher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                }
            }
        ) {
            Text(
                text = stringResource(R.string.action_enable).uppercase(Locale.US),
                style = MaterialTheme.typography.button
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PermissionNotGrantedContent(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    onRequestPermission: () -> Unit
) {
    InfoLayout(
        modifier = modifier,
        imageVector = Icons.Rounded.LocationOff
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.body1
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onRequestPermission() }
        ) {
            Text(
                text = stringResource(R.string.action_grant_permission).uppercase(Locale.US),
                style = MaterialTheme.typography.button
            )
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PermissionDeniedContent(
    modifier: Modifier = Modifier,
    title: String,
    text: String
) {
    val context = LocalContext.current
    val packageName = context.packageName
    InfoLayout(
        modifier = modifier,
        imageVector = Icons.Rounded.LocationOff
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.body1
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", packageName, null)
                    )
                )
            }
        ) {
            Text(
                text = stringResource(R.string.action_permission_settings).uppercase(Locale.US),
                style = MaterialTheme.typography.button
            )
        }
    }
}

@Composable
fun LocationTurnedOffInfo(
    modifier: Modifier = Modifier
) {
    InfoLayout(
        modifier = modifier.padding(bottom = 56.dp),
        imageVector = Icons.Rounded.LocationOff
    ) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {}
        Text(
            text = stringResource(id = R.string.location_turned_off_title),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                launcher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        ) {
            Text(
                text = stringResource(R.string.action_permission_settings)
                    .uppercase(Locale.US),
                style = MaterialTheme.typography.button
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(id = R.string.location_turned_off_info),
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun NavigateToSettings() {
    val context = LocalContext.current
    val packageName = LocalContext.current.packageName
    startActivity(
        context,
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        ),
        null
    )
}