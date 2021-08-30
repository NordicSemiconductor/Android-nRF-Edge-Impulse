package no.nordicsemi.android.ei.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.BottomNavigationScreen
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.ble.DiscoveredBluetoothDevice
import no.nordicsemi.android.ei.ble.rssiAsPercent
import no.nordicsemi.android.ei.ble.state.ScannerState
import no.nordicsemi.android.ei.ble.state.ScanningState
import no.nordicsemi.android.ei.ble.state.ScanningState.Stopped.Reason
import no.nordicsemi.android.ei.comms.DataAcquisitionManager
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.ui.layouts.*
import no.nordicsemi.android.ei.util.Utils
import no.nordicsemi.android.ei.util.exhaustive
import no.nordicsemi.android.ei.viewmodels.DevicesViewModel
import no.nordicsemi.android.ei.viewmodels.state.DeviceState
import no.nordicsemi.android.ei.viewmodels.state.indicatorColor

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Devices(
    scope: CoroutineScope,
    viewModel: DevicesViewModel,
    modifier: Modifier = Modifier,
    configuredDevices: List<Device>,
    activeDevices: Map<String, DataAcquisitionManager>,
    refreshingState: Boolean,
    onRefresh: () -> Unit,
    scannerState: ScannerState,
    onScannerStarted: () -> Unit,
    screen: BottomNavigationScreen,
    connect: (DiscoveredBluetoothDevice) -> Unit,
    disconnect: (DiscoveredBluetoothDevice) -> Unit,
    onRenameClick: (Device, String) -> Unit,
    onDeleteClick: (Device) -> Unit,
) {
    val scanningState = scannerState.scanningState
    val backdropScaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed)

    if (screen != BottomNavigationScreen.DEVICES) {
        animateBottomSheet(
            scope = scope,
            scaffoldState = backdropScaffoldState,
            BackdropValue.Revealed
        )
    }
    BackdropScaffold(
        scaffoldState = backdropScaffoldState,
        appBar = {
            TopAppBar(
                content = {
                    Text(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1.0f),
                        text = stringResource(R.string.label_device_information),
                        style = MaterialTheme.typography.h6
                    )
                    IconButton(onClick = {
                        animateBottomSheet(
                            scope = scope,
                            scaffoldState = backdropScaffoldState,
                            BackdropValue.Revealed
                        )
                    }) {
                        Icon(
                            modifier = Modifier
                                .padding(end = 16.dp),
                            imageVector = Icons.Outlined.ExpandMore,
                            contentDescription = null
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.dp
            )
        },
        backLayerContent = {
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
                                state = viewModel.deviceState(
                                    configuredDevice = configuredDevice,
                                    activeDevices = activeDevices
                                ),
                                onDeviceClicked = { device ->
                                    viewModel.onDeviceSelected(device)
                                    animateBottomSheet(
                                        scope = scope,
                                        scaffoldState = backdropScaffoldState,
                                        BackdropValue.Concealed
                                    )
                                }
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
                                        configuredDevice.deviceId == discoveredDevice.bluetoothDevice.address
                                    } == null
                                }
                                // Display only if at least one was found.
                                .takeIf { it.isNotEmpty() }?.let { discoveredDevices ->
                                    items(
                                        items = discoveredDevices,
                                        key = { it.bluetoothDevice.address }
                                    ) { discoveredDevice ->
                                        DiscoveredDeviceRow(
                                            device = discoveredDevice,
                                            state = activeDevices[discoveredDevice.deviceId]?.state
                                                ?: DeviceState.IN_RANGE,
                                            onDeviceClicked = { connect(it) },
                                            onDeviceAuthenticated = { onRefresh() }
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
        },
        persistentAppBar = false,
        frontLayerElevation = 6.dp,
        frontLayerShape = MaterialTheme.shapes.large,
        frontLayerContent = {
            viewModel.device?.let { device ->
                DeviceDetails(
                    device = device,
                    deviceState = viewModel.deviceState(
                        configuredDevice = device,
                        activeDevices = activeDevices
                    ),
                    onConnectClick = {
                        animateBottomSheet(
                            scope = scope,
                            scaffoldState = backdropScaffoldState,
                            targetValue = BackdropValue.Revealed
                        )
                        viewModel.discoveredBluetoothDevice(device)?.let(connect)
                    },
                    onDisconnectClick = {
                        animateBottomSheet(
                            scope = scope,
                            scaffoldState = backdropScaffoldState,
                            targetValue = BackdropValue.Revealed
                        )
                        viewModel.discoveredBluetoothDevice(device)?.let(disconnect)
                    },
                    onRenameClick = onRenameClick,
                    onDeleteClick = {
                        onDeleteClick(it)
                        animateBottomSheet(
                            scope = scope,
                            scaffoldState = backdropScaffoldState,
                            BackdropValue.Revealed
                        )
                    },
                )
            }
        },
        headerHeight = 0.dp,
        backLayerBackgroundColor = MaterialTheme.colors.surface
    )
}

@Composable
fun ConfiguredDeviceRow(
    device: Device,
    state: DeviceState,
    onDeviceClicked: (Device) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface)
            .clickable(
                enabled = state == DeviceState.IN_RANGE || state == DeviceState.AUTHENTICATED,
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
                    color = state.indicatorColor(),
                    shape = CircleShape
                )
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            modifier = Modifier.weight(1.0f),
            text = device.name,
            color = MaterialTheme.colors.onSurface,
            style = MaterialTheme.typography.body1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(16.dp))
        if (state == DeviceState.CONNECTING) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
fun DiscoveredDeviceRow(
    device: DiscoveredBluetoothDevice,
    state: DeviceState,
    onDeviceClicked: (DiscoveredBluetoothDevice) -> Unit,
    onDeviceAuthenticated: () -> Unit,
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
                text = device.bluetoothDevice.address,
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.caption
            )
        }
        Spacer(modifier = Modifier.width(width = 16.dp))
        when (state) {
            // RSSI image can be displayed even when not in range
            DeviceState.IN_RANGE, DeviceState.NOT_IN_RANGE -> {
                Image(
                    painter = painterResource(id = getRssiRes(device.rssiAsPercent())),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            DeviceState.CONNECTING,
            DeviceState.AUTHENTICATING -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            DeviceState.AUTHENTICATED -> {
                // Once the device is authenticated we should refresh the list of devices.
                onDeviceAuthenticated()
            }
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
    is Reason.BluetoothDisabled -> {
        // TODO https://developer.android.com/about/versions/12/features/bluetooth-permissions
        // States that android:maxSdkVersion="30" is required for the legacy android:name="android.permission.BLUETOOTH"
        // However adding this makes the app crash on Android 12. Seems like a platform bug so let's wait on that.
        // P.S. do not remove the following snippet for now.
        /* if(Utils.isAndroidS()){
            if(DevicesViewModel.isBluetoothScanPermissionGranted(LocalContext.current)){
                BluetoothDisabledInfo()
            } else {
                BluetoothPermissionInfo(
                    modifier = modifier,
                    onScanningStarted = onScanningStarted
                )
            }
        } else {
        } */
        BluetoothDisabledInfo(modifier)
    }
    is Reason.BluetoothScanPermissionNotGranted -> {
        if (Utils.isAndroidS()) {
            BluetoothPermissionInfo(
                modifier = modifier,
                onScanningStarted = onScanningStarted
            )
        } else {
        }
    }
    is Reason.LocationTurnedOff -> LocationTurnedOffInfo(modifier)
    is Reason.LocationPermissionNotGranted -> {
        LocationPermissionInfo(
            modifier = modifier,
            onScanningStarted = onScanningStarted
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun animateBottomSheet(
    scope: CoroutineScope,
    scaffoldState: BackdropScaffoldState,
    targetValue: BackdropValue
) {
    scope.launch {
        scaffoldState.animateTo(targetValue)
    }
}