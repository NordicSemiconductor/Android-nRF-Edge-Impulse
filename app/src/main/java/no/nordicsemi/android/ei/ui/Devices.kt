package no.nordicsemi.android.ei.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.ble.DiscoveredBluetoothDevice
import no.nordicsemi.android.ei.ble.state.*
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.util.Utils.isBluetoothEnabled
import no.nordicsemi.android.ei.viewmodels.DevicesViewModel
import java.util.*

@Composable
fun Devices(modifier: Modifier = Modifier, viewModel: DevicesViewModel) {
    val listState = rememberLazyListState()
    val scannerState = viewModel.scannerState
    val scanning = scannerState.scanningState
    val configuredDevices = viewModel.configuredDevices

    LazyColumn(modifier = modifier.fillMaxSize(), state = listState) {
        item {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.label_devices),
                style = MaterialTheme.typography.h6
            )
        }

        configuredDevices.takeIf {
            it.isNotEmpty()
        }?.let { isNotEmptyList ->
            items(items = isNotEmptyList, key = {
                it.deviceId
            }) {
                ConfiguredDeviceRow(device = it)
                Divider()
            }
        } ?: run {
            item {
                Column(
                    modifier = modifier
                        .fillMaxSize().padding(top = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_devices),
                            contentDescription = null,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.label_no_devices_connected),
                            style = MaterialTheme.typography.h6
                        )
                    }
                }
            }
        }

        item {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.label_scanner),
                style = MaterialTheme.typography.h6
            )
        }

        when (scanning) {
            is Scanning -> {
                scannerState.discoveredDevices.takeIf { it.isNotEmpty() }?.let { isNotEmptyList ->
                    items(items = isNotEmptyList, key = {
                        it.device.address
                    }) {
                        DiscoveredDeviceRow(device = it)
                        Divider()
                    }
                } ?: run {
                    item {
                        ShowScanningStoppedState(
                            modifier = modifier,
                            scannerState = scannerState.scanningState,
                            startScanning = {
                                viewModel.startScan()
                            }
                        )
                    }
                }
            }
            is ScanningStopped -> {
                item {
                    ShowScanningStoppedState(
                        modifier = modifier,
                        scannerState = scannerState.scanningState,
                        startScanning = {
                            viewModel.startScan()
                        }
                    )
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
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = device.name,
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.body1
            )
            Text(
                text = device.device.address,
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.caption
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Image(
            painter = painterResource(id = getRssiRes((100.0f * (127.0f + device.rssi) / (127.0f + 20.0f)).toInt())),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .padding(8.dp)
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
fun ShowScanningStoppedState(
    modifier: Modifier,
    scannerState: ScanningState,
    startScanning: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 32.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
            Icon(
                painter = painterResource(
                    id = when (scannerState) {
                        is Scanning -> {
                            R.drawable.ic_bluetooth_searching
                        }
                        is ScanningStopped -> {
                            scannerState.reason.toDrawable()
                        }
                    }
                ),
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            when (scannerState) {
                is Scanning -> {
                    DisplayScanningInfo()
                }
                is ScanningStopped -> {
                    when (scannerState.reason) {
                        is BluetoothDisabled -> {
                            DisplayBluetoothDisabledInfo()
                        }
                        is LocationPermissionNotGranted -> {
                            DisplayLocationPermissionInfo(startScanning = startScanning)
                        }
                        is LocationTurnedOff -> {
                            DisplayLocationTurnedOffInfo()
                        }
                        is Error -> {
                            (scannerState.reason as Error).throwable.localizedMessage
                        }
                        is NotStarted -> startScanning()
                    }
                }

            }
        }
    }
}

@Composable
fun DisplayScanningInfo() {
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

@Composable
fun DisplayBluetoothDisabledInfo() {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
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
    Button(onClick = {
        if (!isBluetoothEnabled()) {
            launcher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }) {
        Text(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
            text = stringResource(R.string.action_enable).toUpperCase(Locale.ROOT)
        )
    }
}

@Composable
fun DisplayLocationPermissionInfo(startScanning: () -> Unit) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted)
                startScanning()
        }
    Text(
        text = stringResource(id = R.string.location_permission_title),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.h6
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(id = R.string.location_permission_info),
        style = MaterialTheme.typography.body1
    )
    Spacer(modifier = Modifier.height(32.dp))
    Button(modifier = Modifier.defaultMinSize(minHeight = 36.dp), onClick = {
        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }) {
        Text(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
            text = stringResource(R.string.action_location_permission).toUpperCase(Locale.ROOT)
        )
    }
}

@Composable
fun DisplayLocationTurnedOffInfo() {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    Text(
        text = stringResource(id = R.string.location_turned_off_title),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.h6
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(id = R.string.location_turned_off_info),
        style = MaterialTheme.typography.body1
    )
    Spacer(modifier = Modifier.height(32.dp))
    Button(modifier = Modifier.defaultMinSize(minHeight = 36.dp), onClick = {
        launcher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }) {
        Text(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
            text = stringResource(R.string.action_location_permission_settings).toUpperCase(Locale.ROOT)
        )
    }
}

@DrawableRes
private fun Reason.toDrawable(): Int = when (this) {
    is NotStarted -> R.drawable.ic_bluetooth_disabled
    is BluetoothDisabled -> R.drawable.ic_bluetooth_disabled
    is LocationPermissionNotGranted -> R.drawable.ic_location_off
    is LocationTurnedOff -> R.drawable.ic_location_off
    is Error -> R.drawable.ic_error
}