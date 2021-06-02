package no.nordicsemi.android.ei.ui.layouts

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.util.Utils
import java.util.*

@Composable
fun InfoLayout(
    iconPainter: Painter,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
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
        modifier = modifier,
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
        iconPainter = painterResource(id = R.drawable.ic_devices)
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
        iconPainter = painterResource(id = R.drawable.ic_bluetooth_searching)
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
    modifier: Modifier = Modifier
) {
    InfoLayout(
        modifier = modifier,
        iconPainter = painterResource(id = R.drawable.ic_bluetooth_disabled)
    ) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {}
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
                if (!Utils.isBluetoothEnabled()) {
                    launcher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                }
            }
        ) {
            Text(
                text = stringResource(R.string.action_enable).toUpperCase(Locale.US),
                style = MaterialTheme.typography.button
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LocationPermissionInfo(
    modifier: Modifier = Modifier,
    onScanningStarted: () -> Unit
) {
    InfoLayout(
        modifier = modifier,
        iconPainter = painterResource(id = R.drawable.ic_location_off)
    ) {
        var showRationale by rememberSaveable { mutableStateOf(false) }
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted)
                onScanningStarted()
        }
        Text(
            text = stringResource(id = R.string.location_permission_title),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        ) {
            Text(
                text = stringResource(R.string.action_location_permission).toUpperCase(Locale.US),
                style = MaterialTheme.typography.button
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        AnimatedVisibility(visible = !showRationale) {
            Button(
                onClick = { showRationale = true }
            ) {
                Text(
                    text = stringResource(R.string.action_show_location_rationale).toUpperCase(
                        Locale.US),
                    style = MaterialTheme.typography.button
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        AnimatedVisibility(visible = showRationale) {
            Text(
                text = stringResource(id = R.string.location_permission_info),
                style = MaterialTheme.typography.body1
            )
        }
    }
}

@Composable
fun LocationTurnedOffInfo(
    modifier: Modifier = Modifier
) {
    InfoLayout(
        modifier = modifier,
        iconPainter = painterResource(id = R.drawable.ic_location_off)
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
                text = stringResource(R.string.action_location_permission_settings)
                    .toUpperCase(Locale.US),
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