package no.nordicsemi.android.ei.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.viewmodels.state.DeviceState
import java.util.*

@Composable
fun DeviceDetails(
    device: Device,
    deviceState: DeviceState?,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 56.dp)
    ) {
        item {
            DeviceName(device = device)
            Connectivity(
                device = device,
                deviceState = deviceState,
                onConnectClick = onConnectClick,
                onDisconnectClick = onDisconnectClick
            )
            SensorInformation(device = device)
            Capabilities(device = device)
            DeviceInformation(device = device)
        }
    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun DeviceName(device: Device) {

    var onEditClick by rememberSaveable {
        mutableStateOf(false)
    }

    var deviceName by rememberSaveable {
        mutableStateOf(device.name)
    }
    AnimatedVisibility(visible = onEditClick) {
        Row(
            modifier = Modifier.wrapContentHeight().padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
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
                singleLine = true
            )
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { onEditClick = !onEditClick }) {
                Icon(
                    modifier = Modifier
                        .size(24.dp),
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary
                )
            }
            IconButton(onClick = { onEditClick = !onEditClick }) {
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
    if (!onEditClick) {
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
                    style = MaterialTheme.typography.body2
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

@Composable
private fun DeviceInformation(device: Device) {
    SectionTitle(text = stringResource(R.string.label_details))
    Column {
        RowItem(
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
            imageVector = Icons.Outlined.History,
            text = stringResource(R.string.label_created_at),
            subText = device.created
        )
        RowItem(
            imageVector = Icons.Outlined.Visibility,
            text = stringResource(R.string.label_last_seen),
            subText = device.lastSeen
        )
    }
}

@Composable
private fun Connectivity(
    device: Device,
    deviceState: DeviceState?,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
) {
    SectionTitle(text = stringResource(R.string.label_connectivity), content = {
        when (deviceState) {
            DeviceState.IN_RANGE -> {
                TextButton(
                    onClick = {
                        onConnectClick()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_connect),
                        style = TextStyle(fontSize = 18.sp)
                    )
                }
            }
            DeviceState.CONNECTING,
            DeviceState.AUTHENTICATING,
            DeviceState.AUTHENTICATED -> {
                TextButton(
                    onClick = {
                        onDisconnectClick()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_disconnect).uppercase(Locale.US),
                        style = TextStyle(fontSize = 18.sp)
                    )
                }
            }
            else -> {
            }
        }
    })
    RowItem(
        imageVector = Icons.Outlined.Cloud,
        text = stringResource(R.string.label_connected_to_remote_management),
        subText = device.remoteMgmtConnected.toString()
    )
}

@Composable
private fun Capabilities(device: Device) {
    SectionTitle(text = stringResource(R.string.label_capabilities))
    RowItem(
        imageVector = Icons.Outlined.SettingsEthernet,
        text = stringResource(R.string.label_supports_snapshot_streaming),
        subText = device.remoteMgmtConnected.toString()
    )
}


@Composable
private fun SensorInformation(device: Device) {
    device.sensors.takeIf { sensors -> sensors.isNotEmpty() }?.onEach { sensor ->
        SectionTitle(text = sensor.name)
        RowItem(
            imageVector = Icons.Outlined.Timer,
            text = stringResource(R.string.label_max_sample_length),
            subText = stringResource(id = R.string.label_sample_duration, sensor.maxSampleLengths)
        )
        RowItem(
            imageVector = Icons.Outlined.GraphicEq,
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