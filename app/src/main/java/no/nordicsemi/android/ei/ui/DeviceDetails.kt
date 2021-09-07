package no.nordicsemi.android.ei.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.viewmodels.state.DeviceState
import no.nordicsemi.android.ei.viewmodels.state.DeviceState.*
import no.nordicsemi.android.ei.viewmodels.state.buttonBackgroundColor
import no.nordicsemi.android.ei.viewmodels.state.indicatorColor
import java.text.DateFormat
import java.util.*

@Composable
fun DeviceDetails(
    device: Device,
    deviceState: DeviceState?,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onRenameClick: (Device, String) -> Unit,
    onDeleteClick: (Device) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 56.dp)
    ) {
        item {
            DeviceName(
                device = device,
                onRenameClick = onRenameClick
            )
            Connectivity(
                deviceState = deviceState
            )
            Row(
                modifier = Modifier
                    .background(color = MaterialTheme.colors.background)
                    .fillMaxWidth()
                    .padding(16.dp), horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier
                        .width(120.dp),
                    onClick = {
                        when (deviceState) {
                            IN_RANGE -> onConnectClick()
                            CONNECTING,
                            AUTHENTICATING,
                            AUTHENTICATED -> onDisconnectClick()
                            else -> {
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(deviceState.buttonBackgroundColor())
                ) {
                    Row {
                        Text(
                            text = when (deviceState) {
                                IN_RANGE -> stringResource(R.string.action_connect)
                                CONNECTING,
                                AUTHENTICATING -> stringResource(id = R.string.action_cancel)
                                AUTHENTICATED -> stringResource(R.string.action_disconnect)
                                else -> ""
                            },
                            color = Color.White
                        )
                    }
                }
            }
            SensorInformation(device = device)
            Capabilities(device = device)
            DeviceInformation(device = device)
        }
        item {
            Row(
                modifier = Modifier
                    .background(color = MaterialTheme.colors.background)
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { onDeleteClick(device) },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = Color.White
                    )
                }
            }
        }
    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun DeviceName(
    device: Device,
    onRenameClick: (Device, String) -> Unit,
) {

    var onEditClick by rememberSaveable {
        mutableStateOf(false)
    }

    var deviceName by rememberSaveable(device) {
        mutableStateOf(device.name)
    }

    Crossfade(targetState = onEditClick) {
        when (it) {
            true -> {
                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = deviceName,
                        onValueChange = { deviceName = it },
                        modifier = Modifier
                            .weight(1.0f),
                        label = {
                            Text(text = stringResource(R.string.label_name))
                        },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = Icons.Outlined.Label,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        isError = deviceName.isBlank()
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(
                        onClick = {
                            deviceName = device.name
                            onEditClick = !onEditClick
                        }
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(24.dp),
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary
                        )
                    }
                    IconButton(
                        onClick = {
                            onRenameClick(device, deviceName)
                            onEditClick = !onEditClick
                        },
                        enabled = deviceName.isNotBlank()
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(24.dp),
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary
                        )
                    }

                }
            }
            false -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        modifier = Modifier
                            .size(24.dp),
                        imageVector = Icons.Outlined.Label,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onSurface
                    )
                    Column(
                        modifier = Modifier
                            .weight(1.0f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.label_name),
                            style = MaterialTheme.typography.body1
                        )
                        Text(
                            text = deviceName,
                            style = MaterialTheme.typography.body2,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { onEditClick = !onEditClick }) {
                        Icon(
                            modifier = Modifier
                                .size(24.dp),
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null
                        )
                    }
                }
                Divider()
            }
        }
    }
}

@Composable
private fun DeviceInformation(device: Device) {
    SectionTitle(text = stringResource(R.string.label_details))
    val dateTimeInstance = DateFormat.getDateTimeInstance()
    Column {
        RowItem(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Outlined.PermIdentity,
            text = stringResource(R.string.label_id).uppercase(
                Locale.US
            ),
            subText = device.deviceId
        )
        RowItem(
            drawableRes = R.drawable.ic_devices,
            text = stringResource(R.string.label_type),
            subText = device.deviceType
        )
        RowItem(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Outlined.History,
            text = stringResource(R.string.label_created_at),
            subText = dateTimeInstance.format(
                Date(
                    Instant.parse(device.created).toEpochMilliseconds()
                )
            )
        )
        RowItem(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Outlined.Visibility,
            text = stringResource(R.string.label_last_seen),
            subText = dateTimeInstance.format(
                Date(
                    Instant.parse(device.lastSeen).toEpochMilliseconds()
                )
            )
        )
    }
}

@Composable
private fun Connectivity(
    deviceState: DeviceState?,
) {
    SectionTitle(text = stringResource(R.string.label_connectivity))
    RowItem(
        modifier = Modifier
            .size(24.dp)
            .animateContentSize(),
        imageVector = Icons.Outlined.Cloud,
        tint = deviceState?.indicatorColor() ?: MaterialTheme.colors.onSurface,
        text = stringResource(R.string.label_connected_to_remote_management),
        subText = when (deviceState) {
            NOT_IN_RANGE, IN_RANGE -> stringResource(R.string.label_disconnected)
            CONNECTING -> stringResource(R.string.label_connecting)
            AUTHENTICATING -> stringResource(R.string.label_authenticating)
            AUTHENTICATED -> stringResource(R.string.label_connected)
            else -> stringResource(id = R.string.unknown)
        }
    )
}

@Composable
private fun Capabilities(device: Device) {
    SectionTitle(text = stringResource(R.string.label_capabilities))
    RowItem(
        modifier = Modifier.size(24.dp),
        imageVector = Icons.Outlined.SettingsEthernet,
        text = stringResource(R.string.label_snapshot_streaming),
        subText = when (device.supportsSnapshotStreaming) {
            true -> stringResource(R.string.label_supported)
            false -> stringResource(R.string.label_unsupported)
        }
    )
}


@Composable
private fun SensorInformation(device: Device) {
    device.sensors.takeIf { sensors -> sensors.isNotEmpty() }?.onEach { sensor ->
        SectionTitle(text = sensor.name)
        RowItem(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Outlined.Timer,
            text = stringResource(R.string.label_max_sample_length),
            subText = stringResource(id = R.string.label_sample_duration, sensor.maxSampleLengths)
        )
        RowItem(
            drawableRes = R.drawable.ic_waveform,
            text = stringResource(R.string.label_frequencies),
            subText = sensor.frequencies.joinToString(
                separator = stringResource(R.string.label_frequencies_separator),
                postfix = stringResource(
                    R.string.label_unit_hertz
                )
            )
        )
    }
}

@Composable
private fun SectionTitle(text: String, content: @Composable () -> Unit = {}) {
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier
                .weight(1.0f),
            text = text,
            style = MaterialTheme.typography.h6
        )
        content()
    }
}

@Composable
private fun RowItem(
    modifier: Modifier,
    imageVector: ImageVector,
    tint: Color = MaterialTheme.colors.onSurface,
    text: String,
    subText: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            modifier = modifier,
            imageVector = imageVector,
            contentDescription = null,
            tint = tint
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.body1
            )
            subText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
    Divider()
}

@Composable
private fun RowItem(@DrawableRes drawableRes: Int, text: String, subText: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            modifier = Modifier
                .size(24.dp),
            painter = painterResource(id = drawableRes),
            contentDescription = null,
            tint = MaterialTheme.colors.onSurface
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.body1
            )
            subText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
    Divider()
}