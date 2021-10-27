package no.nordicsemi.android.ei.ui

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*
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
import no.nordicsemi.android.ei.comms.DataAcquisitionManager
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.ui.layouts.*
import no.nordicsemi.android.ei.util.Utils
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
    screen: BottomNavigationScreen,
    backdropScaffoldState: BackdropScaffoldState,
    onBluetoothStateChanged: (Boolean) -> Unit,
    connect: (DiscoveredBluetoothDevice) -> Unit,
    disconnect: (DiscoveredBluetoothDevice) -> Unit,
    onRenameClick: (Device, String) -> Unit,
    onDeleteClick: (Device) -> Unit
) {
    val scanningState = scannerState.scanningState
    BackHandler(
        enabled = backdropScaffoldState.isConcealed,
        onBack = {
            animateBottomSheet(
                scope = scope,
                scaffoldState = backdropScaffoldState,
                BackdropValue.Revealed
            )
        }
    )
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
                            imageVector = Icons.Rounded.ExpandMore,
                            contentDescription = null
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.background,
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
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colors.background)
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
                    item {
                        when {
                            Utils.isSorAbove() -> BluetoothPermissionsRequired(
                                modifier = modifier,
                                scannerState = scannerState,
                                onBluetoothStateChanged = onBluetoothStateChanged
                            )
                            Utils.isBetweenMarshmallowAndS() -> LocationPermissionRequired(
                                modifier = modifier,
                                scannerState = scannerState,
                                onBluetoothStateChanged = onBluetoothStateChanged
                            )
                            else -> BluetoothRequired(
                                modifier = modifier,
                                scannerState = scannerState,
                                onBluetoothStateChanged = onBluetoothStateChanged
                            )
                        }
                    }
                    if (scannerState.scanningState is ScanningState.Started) {
                        scannerState.discoveredDevices
                            // Filter only devices that have not been configured.
                            .filter { discoveredDevice ->
                                configuredDevices.find { configuredDevice ->
                                    configuredDevice.deviceId == discoveredDevice.bluetoothDevice.address
                                } == null
                            }
                            // Display only if at least one was found.
                            .takeIf { it.isNotEmpty() }?.let { discoveredDevices ->
                                this@LazyColumn.items(
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
                            ?: this@LazyColumn.item {
                                NoDevicesInRangeInfo(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                )
                            }
                    }

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
                        viewModel.discoveredBluetoothDevice(device)?.let(connect)
                    },
                    onDisconnectClick = {
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

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun BluetoothPermissionsRequired(
    modifier: Modifier,
    scannerState: ScannerState,
    onBluetoothStateChanged: (Boolean) -> Unit
) {
    val bluetoothPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT
        )
    )
    PermissionsRequired(
        multiplePermissionsState = bluetoothPermissionsState,
        permissionsNotGrantedContent = {
            PermissionNotGrantedContent(
                modifier = modifier,
                title = stringResource(id = R.string.bluetooth_scan_connect_permission_required_title),
                text = stringResource(id = R.string.bluetooth_scan_connect_permission_info),
                onRequestPermission = { bluetoothPermissionsState.launchMultiplePermissionRequest() }
            )
        },
        permissionsNotAvailableContent = {
            PermissionDeniedContent(
                modifier = modifier,
                title = stringResource(id = R.string.bluetooth_scan_connect_permission_denied_title),
                text = stringResource(id = R.string.bluetooth_scan_connect_permission_denied_info)
            )
        },
        content = {
            BluetoothRequired(
                modifier = modifier,
                scannerState = scannerState
            ) {
                onBluetoothStateChanged(it)
            }
        }
    )
}

@Composable
private fun BluetoothRequired(
    modifier: Modifier,
    scannerState: ScannerState,
    onBluetoothStateChanged: (Boolean) -> Unit
) {
    val localContext = LocalContext.current
    var isBluetoothEnabled by remember { mutableStateOf(Utils.isBluetoothEnabled(context = localContext)) }
    SystemBroadcastReceiver(
        systemAction = BluetoothAdapter.ACTION_STATE_CHANGED
    ) { intent ->
        val currentState = intent?.getIntExtra(
            BluetoothAdapter.EXTRA_STATE,
            BluetoothAdapter.STATE_OFF
        )
        val previousState = intent?.getIntExtra(
            BluetoothAdapter.EXTRA_PREVIOUS_STATE,
            BluetoothAdapter.STATE_OFF
        )
        when (currentState) {
            BluetoothAdapter.STATE_ON -> {
                isBluetoothEnabled = true
                onBluetoothStateChanged(isBluetoothEnabled)
            }
            BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF -> {
                if (previousState != BluetoothAdapter.STATE_TURNING_OFF &&
                    previousState != BluetoothAdapter.STATE_OFF
                ) {
                    isBluetoothEnabled = false
                    onBluetoothStateChanged(isBluetoothEnabled)
                }
            }
        }
    }

    if (isBluetoothEnabled) {
        onBluetoothStateChanged(isBluetoothEnabled)
    } else {
        BluetoothDisabledInfo(modifier = modifier, onBluetoothEnabled = {
            isBluetoothEnabled = true
        })
        scannerState.onBluetoothDisabled()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LocationPermissionRequired(
    modifier: Modifier,
    scannerState: ScannerState,
    onBluetoothStateChanged: (Boolean) -> Unit
) {
    val locationPermission = rememberPermissionState(
        permission = android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    PermissionRequired(
        permissionState = locationPermission,
        permissionNotGrantedContent = {
            PermissionNotGrantedContent(
                modifier = modifier,
                title = stringResource(id = R.string.location_permission_title),
                text = stringResource(id = R.string.location_permission_info),
                onRequestPermission = { locationPermission.launchPermissionRequest() }
            )
        },
        permissionNotAvailableContent = {
            PermissionDeniedContent(
                modifier = modifier,
                title = stringResource(id = R.string.location_permission_denied_title),
                text = stringResource(id = R.string.location_permission_denied_info)
            )
        },
        content = {
            LocationRequired(
                modifier = modifier,
                scannerState = scannerState,
                onBluetoothStateChanged = onBluetoothStateChanged
            )
        }
    )
}

@Composable
private fun LocationRequired(
    modifier: Modifier,
    scannerState: ScannerState,
    onBluetoothStateChanged: (Boolean) -> Unit
) {
    val localContext = LocalContext.current
    var isLocationEnabled by remember { mutableStateOf(Utils.isLocationEnabled(context = localContext)) }
    SystemBroadcastReceiver(
        systemAction = LocationManager.MODE_CHANGED_ACTION
    ) {
        isLocationEnabled = Utils.isLocationEnabled(context = localContext)
    }
    if (!isLocationEnabled) {
        scannerState.onLocationTurnedOff()
        LocationTurnedOffInfo(modifier = modifier)
    } else {
        BluetoothRequired(
            modifier = modifier,
            scannerState = scannerState,
            onBluetoothStateChanged = onBluetoothStateChanged
        )
    }
}

@Composable
private fun SystemBroadcastReceiver(
    systemAction: String,
    onSystemEvent: (Intent?) -> Unit
) {
    val context = LocalContext.current
    val currentOnSystemEvent by rememberUpdatedState(systemAction)

    // If either context or systemAction changes, unregister and register again
    DisposableEffect(context, currentOnSystemEvent) {
        val intentFilter = IntentFilter(systemAction)
        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(context1: Context?, intent: Intent?) {
                onSystemEvent(intent)
            }
        }
        context.registerReceiver(broadcast, intentFilter)
        // When the effect leaves the Composition, remove the callback
        onDispose {
            context.unregisterReceiver(broadcast)
        }
    }
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
                /*enabled = state == DeviceState.IN_RANGE || state == DeviceState.AUTHENTICATED,*/
                onClick = { onDeviceClicked(device) },
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = Icons.Rounded.DeveloperBoard,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = state.indicatorColor(),
                    shape = CircleShape
                )
                .padding(8.dp),
            colorFilter = ColorFilter.tint(Color.White)
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
            imageVector = Icons.Rounded.DeveloperBoard,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colors.primary,
                    shape = CircleShape
                )
                .padding(8.dp),
            colorFilter = ColorFilter.tint(Color.White)
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

@OptIn(ExperimentalMaterialApi::class)
fun animateBottomSheet(
    scope: CoroutineScope,
    scaffoldState: BackdropScaffoldState,
    targetValue: BackdropValue
) {
    scope.launch {
        scaffoldState.animateTo(targetValue)
    }
}