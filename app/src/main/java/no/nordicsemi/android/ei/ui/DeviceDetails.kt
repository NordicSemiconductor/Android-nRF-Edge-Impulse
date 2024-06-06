/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PermIdentity
import androidx.compose.material.icons.outlined.SettingsEthernet
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.viewmodels.state.DeviceState
import no.nordicsemi.android.ei.viewmodels.state.DeviceState.AUTHENTICATED
import no.nordicsemi.android.ei.viewmodels.state.DeviceState.AUTHENTICATING
import no.nordicsemi.android.ei.viewmodels.state.DeviceState.CONNECTING
import no.nordicsemi.android.ei.viewmodels.state.DeviceState.IN_RANGE
import no.nordicsemi.android.ei.viewmodels.state.DeviceState.NOT_IN_RANGE
import no.nordicsemi.android.ei.viewmodels.state.buttonBackgroundColor
import no.nordicsemi.android.ei.viewmodels.state.indicatorColor
import java.text.DateFormat
import java.util.Date

@Composable
fun DeviceDetails(
    device: Device,
    deviceState: DeviceState?,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onRenameClick: (Device, String) -> Unit,
    onDeleteClick: (Device) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            DeviceName(device = device, onRenameClick = onRenameClick)
            Connectivity(deviceState = deviceState)
            Row(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        when (deviceState) {
                            IN_RANGE -> onConnectClick()
                            CONNECTING,
                            AUTHENTICATING,
                            AUTHENTICATED -> onDisconnectClick()

                            else -> {}
                        }
                    },
                    enabled = when (deviceState) {
                        IN_RANGE, CONNECTING, AUTHENTICATING, AUTHENTICATED -> true
                        else -> false
                    },
                    colors = ButtonDefaults.buttonColors(deviceState.buttonBackgroundColor())
                ) {
                    Text(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 145.dp),
                        text = when (deviceState) {
                            IN_RANGE, NOT_IN_RANGE -> stringResource(id = R.string.action_connect)
                            CONNECTING,
                            AUTHENTICATING -> stringResource(id = R.string.action_cancel)

                            AUTHENTICATED -> stringResource(id = R.string.action_disconnect)
                            else -> stringResource(id = R.string.action_connect)
                        },
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
            SensorInformation(device = device)
            Capabilities(device = device)
            DeviceInformation(device = device)
        }
        item {
            Row(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { onDeleteClick(device) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
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

@Composable
private fun DeviceName(
    device: Device,
    onRenameClick: (Device, String) -> Unit,
) {
    var onEditClick by rememberSaveable { mutableStateOf(false) }
    var deviceName by rememberSaveable(device) { mutableStateOf(device.name) }

    Crossfade(targetState = onEditClick, label = "name") {
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
                        modifier = Modifier.weight(1.0f),
                        label = { Text(text = stringResource(R.string.label_name)) },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = Icons.AutoMirrored.Outlined.Label,
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
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
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
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
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
                        imageVector = Icons.AutoMirrored.Outlined.Label,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Column(
                        modifier = Modifier
                            .weight(1.0f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.label_name),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = deviceName,
                            style = MaterialTheme.typography.bodySmall,
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
                HorizontalDivider()
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
            text = stringResource(R.string.label_id),
            subText = device.deviceId
        )
        RowItem(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Rounded.DeveloperBoard,
            text = stringResource(R.string.label_type),
            subText = device.deviceType
        )
        RowItem(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Outlined.History,
            text = stringResource(R.string.label_created_at),
            subText = dateTimeInstance.format(
                Date(Instant.parse(device.created).toEpochMilliseconds())
            )
        )
        RowItem(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Outlined.Visibility,
            text = stringResource(R.string.label_last_seen),
            subText = dateTimeInstance.format(
                Date(Instant.parse(device.lastSeen).toEpochMilliseconds())
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
        tint = deviceState?.indicatorColor() ?: MaterialTheme.colorScheme.onSurface,
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
            imageVector = Icons.Outlined.GraphicEq,
            text = stringResource(R.string.label_frequencies),
            subText = sensor.frequencies.joinToString(
                separator = stringResource(R.string.label_frequencies_separator),
                postfix = stringResource(R.string.label_unit_hertz)
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
            style = MaterialTheme.typography.titleMedium
        )
        content()
    }
}

@Composable
internal fun RowItem(
    modifier: Modifier,
    imageVector: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurface,
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
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
            subText?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    HorizontalDivider()
}

@Composable
private fun RowItem(imageVector: ImageVector, text: String, subText: String?) {
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
            imageVector = imageVector,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        ) {
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
            subText?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    HorizontalDivider()
}