package no.nordicsemi.android.ei.ui

import android.bluetooth.BluetoothDevice
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import no.nordicsemi.android.ei.comms.CommsManager
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.ui.layouts.*
import no.nordicsemi.android.ei.util.exhaustive
import no.nordicsemi.android.ei.viewmodels.state.DeviceState
import no.nordicsemi.android.ei.viewmodels.state.indicatorColor

@Composable
fun Devices(
    modifier: Modifier = Modifier,
    configuredDevices: List<Device>,
    activeDevices: Map<BluetoothDevice, CommsManager>,
    refreshingState: Boolean,
    onRefresh: () -> Unit,
    scannerState: ScannerState,
    onScannerStarted: () -> Unit,
    connect: (BluetoothDevice) -> Unit
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

            configuredDevices.takeIf { it.isNotEmpty() }?.let { configuredDevices ->
                items(
                    items = configuredDevices,
                    key = { it.deviceId }
                ) { configuredDevice ->
                    ConfiguredDeviceRow(
                        device = configuredDevice,
                        state = DeviceState.IN_RANGE, // TODO implement
                        onDeviceClicked = { }, // TODO implement
                    )
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
                is ScanningState.Initializing -> {
                }
                is ScanningState.Started -> {
                    scannerState.discoveredDevices
                        // Filter only devices that have not been configured.
                        .filter { discoveredDevice ->
                            configuredDevices.find { configuredDevice ->
                                configuredDevice.deviceId == discoveredDevice.device.address
                            } == null
                        }
                        // Display only if at least one was found.
                        .takeIf { it.isNotEmpty() }?.let { discoveredDevices ->
                            items(
                                items = discoveredDevices,
                                key = { it.device.address }
                            ) { discoveredDevice ->
                                DiscoveredDeviceRow(
                                    device = discoveredDevice,
                                    isConnecting = activeDevices.containsKey(discoveredDevice.device),
                                    onDeviceClicked = { connect(it.device) },
                                )
                                Divider()
                            }
                        }
                        // Else, show a placeholder.
                        ?: item {
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
            }.exhaustive
        }
    }
}

@Composable
fun ConfiguredDeviceRow(
    device: Device,
    state: DeviceState,
    onDeviceClicked: (Device) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface)
            .clickable(
                enabled = state == DeviceState.IN_RANGE,
                onClick = { onDeviceClicked(device) },
            )
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
        when (state) {
            DeviceState.CONNECTING -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            else -> {
                Surface(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(8.dp),
                    color = state.indicatorColor(),
                    shape = CircleShape
                ) {}
            }
        }
    }
}

@Composable
fun DiscoveredDeviceRow(
    device: DiscoveredBluetoothDevice,
    isConnecting: Boolean,
    onDeviceClicked: (DiscoveredBluetoothDevice) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface)
            .clickable(
                enabled = !isConnecting,
                onClick = { onDeviceClicked(device) },
            )
            .padding(16.dp),
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
        if (isConnecting) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            )
        } else {
            Image(
                painter = painterResource(id = getRssiRes(device.rssiAsPercent())),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            )
        }
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