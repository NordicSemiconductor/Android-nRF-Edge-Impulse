package no.nordicsemi.android.ei.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.ble.DiscoveredBluetoothDevice
import no.nordicsemi.android.ei.ble.rssiAsPercent
import no.nordicsemi.android.ei.ble.state.ScannerState
import no.nordicsemi.android.ei.ble.state.ScanningState
import no.nordicsemi.android.ei.ble.state.ScanningState.Stopped.Reason
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.ui.theme.NordicRed
import no.nordicsemi.android.ei.util.Utils.isBluetoothEnabled
import java.util.*

@Composable
fun Devices(
    modifier: Modifier = Modifier,
    configuredDevices: List<Device>,
    refreshingState: Boolean,
    onRefresh: () -> Unit,
    scannerState: ScannerState,
    onScannerStarted: () -> Unit
) {
    val scanningState = scannerState.scanningState

    SwipeRefresh(
        state = rememberSwipeRefreshState(refreshingState),
        onRefresh = onRefresh,
        modifier = modifier,
        // TODO After Compose is stable, try removing this and swiping in Scanner tab.
        // Those 3 properties below copy the default values from SwipeRefresh.
        // Without them, the Scanner page crashes when devices are displayed and Swipe is used.
        indicator = { s, trigger ->
            SwipeRefreshIndicator(s, trigger)
        },
        indicatorAlignment = Alignment.TopCenter,
        indicatorPadding = PaddingValues(0.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    text = stringResource(R.string.label_devices),
                    style = MaterialTheme.typography.h6
                )
            }

            configuredDevices.takeIf { it.isNotEmpty() }?.let { isNotEmptyList ->
                items(
                    items = isNotEmptyList,
                    key = { it.deviceId }
                ) {
                    ConfiguredDeviceRow(device = it)
                    Divider()
                }
            } ?: item {
                NoConfiguredDevicesInfo(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .weight(1.0f),
                        text = stringResource(R.string.label_scanner),
                        style = MaterialTheme.typography.h6
                    )
                    if (scanningState == ScanningState.Started) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            when (scanningState) {
                is ScanningState.Started -> {
                    scannerState.discoveredDevices
                        .takeIf { it.isNotEmpty() }
                        ?.let { isNotEmptyList ->
                            items(
                                items = isNotEmptyList,
                                key = { it.device.address }
                            ) {
                                DiscoveredDeviceRow(device = it)
                                Divider()
                            }
                        } ?: item {
                            NoDevicesInRangeInfo(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )
                        }
                }
                is ScanningState.Stopped -> {
                    item {
                        ScanningStoppedInfo(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            reason = scanningState.reason,
                            onScanningStarted = onScannerStarted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConfiguredDeviceRow(device: Device) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_devices),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colors.primary,
                    shape = CircleShape
                )
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = device.name,
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.body1
            )
            Text(
                text = device.deviceId,
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.caption
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Surface(
            modifier = Modifier
                .padding(end = 16.dp)
                .size(8.dp),
            //TODO Add green for connected devices
            color = NordicRed,
            shape = CircleShape
        ) {}
    }
}

@Composable
fun DiscoveredDeviceRow(device: DiscoveredBluetoothDevice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface)
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_uart),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colors.primary,
                    shape = CircleShape
                )
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.width(width = 16.dp))
        Column(modifier = Modifier.weight(weight = 1.0f)) {
            Text(
                text = device.name ?: stringResource(id = R.string.unknown),
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.body1
            )
            Text(
                text = device.device.address,
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.caption
            )
        }
        Spacer(modifier = Modifier.width(width = 16.dp))
        Image(
            painter = painterResource(id = getRssiRes(device.rssiAsPercent())),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterVertically)
        )
    }
}

@DrawableRes
private fun getRssiRes(rssi: Int): Int = when (rssi) {
    in 0..20 -> R.drawable.ic_signal_0_bar
    in 21..40 -> R.drawable.ic_signal_1_bar
    in 41..60 -> R.drawable.ic_signal_2_bar
    in 61..80 -> R.drawable.ic_signal_3_bar
    else -> R.drawable.ic_signal_4_bar
}

@Composable
fun InfoLayout(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun NoConfiguredDevicesInfo(
    modifier: Modifier = Modifier
) {
    InfoLayout(
        modifier = modifier,
        icon = R.drawable.ic_devices
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
        icon = R.drawable.ic_bluetooth_searching
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
fun ScanningStoppedInfo(
    modifier: Modifier,
    reason: Reason,
    onScanningStarted: () -> Unit
) = when (reason) {
    is Reason.BluetoothDisabled -> BluetoothDisabledInfo(modifier)
    is Reason.LocationTurnedOff -> LocationTurnedOffInfo(modifier)
    is Reason.LocationPermissionNotGranted -> {
        LocationPermissionInfo(
            modifier = modifier,
            onScanningStarted = onScanningStarted
        )
    }
}

@Composable
fun BluetoothDisabledInfo(
    modifier: Modifier = Modifier
) {
    InfoLayout(
        modifier = modifier,
        icon = R.drawable.ic_bluetooth_disabled
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
                if (!isBluetoothEnabled()) {
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
        icon = R.drawable.ic_location_off
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
                    text = stringResource(R.string.action_show_location_rationale).toUpperCase(Locale.US),
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
        icon = R.drawable.ic_location_off
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
                text = stringResource(R.string.action_location_permission_settings).toUpperCase(Locale.US),
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