package no.nordicsemi.android.ei.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.Device.Sensor
import no.nordicsemi.android.ei.ui.theme.NordicGrass
import java.util.*


@Composable
fun RecordSampleLargeScreen(
    connectedDevices: List<Device>,
    focusRequester: FocusRequester,
    selectedDevice: Device?,
    onDeviceSelected: (Device) -> Unit,
    label: String,
    onLabelChanged: (String) -> Unit,
    selectedSensor: Sensor?,
    onSensorSelected: (Sensor) -> Unit,
    selectedFrequency: Number?,
    onFrequencySelected: (Number) -> Unit,
    onDismiss: () -> Unit
) {
    Surface {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.title_record_new_data),
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface
                )
            }
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
            ) {
                RecordSampleContent(
                    connectedDevices = connectedDevices,
                    focusRequester = focusRequester,
                    selectedDevice = selectedDevice,
                    onDeviceSelected = onDeviceSelected,
                    label = label,
                    onLabelChanged = onLabelChanged,
                    selectedSensor = selectedSensor,
                    onSensorSelected = onSensorSelected,
                    selectedFrequency = selectedFrequency,
                    onFrequencySelected = onFrequencySelected
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onDismiss() }) {
                        Text(
                            text = stringResource(R.string.action_dialog_cancel).toUpperCase(Locale.ROOT),
                            style = MaterialTheme.typography.button
                        )
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                    TextButton(
                        enabled = selectedSensor != null,
                        onClick = { /*TODO implement start sampling*/ }) {
                        Text(
                            text = stringResource(R.string.action_start_sampling).toUpperCase(Locale.ROOT),
                            style = MaterialTheme.typography.button
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecordSampleSmallScreen(
    isLandscape: Boolean,
    connectedDevices: List<Device>,
    focusRequester: FocusRequester,
    selectedDevice: Device?,
    onDeviceSelected: (Device) -> Unit,
    label: String,
    onLabelChanged: (String) -> Unit,
    selectedSensor: Sensor?,
    onSensorSelected: (Sensor) -> Unit,
    selectedFrequency: Number?,
    onFrequencySelected: (Number) -> Unit,
    onCloseClicked: () -> Unit
) {
    Scaffold(
        modifier = Modifier.wrapContentHeight(),
        topBar = {
            TopAppBar(title = {
                Text(
                    text = stringResource(R.string.title_record_new_data)
                )
            },
                navigationIcon = {
                    IconButton(onClick = { onCloseClicked() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(
                                id = R.string.content_decription_close_record_new_data
                            )
                        )
                    }
                })
        }
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .padding(16.dp)
                .verticalScroll(
                    state = rememberScrollState(),
                    enabled = isLandscape
                )
        ) {
            RecordSampleContent(
                connectedDevices = connectedDevices,
                focusRequester = focusRequester,
                selectedDevice = selectedDevice,
                onDeviceSelected = onDeviceSelected,
                label = label,
                onLabelChanged = onLabelChanged,
                selectedSensor = selectedSensor,
                onSensorSelected = onSensorSelected,
                selectedFrequency = selectedFrequency,
                onFrequencySelected = onFrequencySelected
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    enabled = selectedSensor != null,
                    onClick = { /*TODO implement start sampling*/ }) {
                    Text(
                        text = stringResource(R.string.action_start_sampling).toUpperCase(Locale.ROOT),
                        style = MaterialTheme.typography.button
                    )
                }
            }
        }
    }
}


@Composable
private fun RecordSampleContent(
    connectedDevices: List<Device>,
    focusRequester: FocusRequester,
    selectedDevice: Device?,
    onDeviceSelected: (Device) -> Unit,
    label: String,
    onLabelChanged: (String) -> Unit,
    selectedSensor: Sensor?,
    onSensorSelected: (Sensor) -> Unit,
    selectedFrequency: Number?,
    onFrequencySelected: (Number) -> Unit
) {

    val focusManager = LocalFocusManager.current
    var isDevicesMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isSensorsMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isFrequencyMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var sampleLength by remember { mutableStateOf(5000) }

    OutlinedTextField(
        value = selectedDevice?.name ?: stringResource(id = R.string.empty),
        onValueChange = { },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester = focusRequester),
        readOnly = true,
        label = {
            Text(
                text = stringResource(R.string.label_device)
            )
        },
        leadingIcon = {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .padding(4.dp),
                painter = painterResource(id = R.drawable.ic_devices),
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(onClick = {
                focusRequester.requestFocus()
                isDevicesMenuExpanded = true
            }) {
                Icon(
                    modifier = Modifier.rotate(if (isDevicesMenuExpanded) 180f else 0f),
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
                if (isDevicesMenuExpanded)
                    ShowDevicesDropdown(
                        connectedDevices = connectedDevices,
                        onDeviceSelected = { device ->
                            onDeviceSelected(device)
                            isDevicesMenuExpanded = false
                        },
                        onDismiss = {
                            isDevicesMenuExpanded = false
                            focusManager.clearFocus()
                        })
            }
        },
        singleLine = true
    )
    OutlinedTextField(
        value = label,
        onValueChange = { onLabelChanged(it) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        label = {
            Text(
                text = stringResource(R.string.label_label)
            )
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
    OutlinedTextField(
        value = selectedSensor?.name ?: stringResource(id = R.string.empty),
        onValueChange = { },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .focusRequester(focusRequester = focusRequester),
        enabled = selectedDevice != null,
        readOnly = true,
        label = {
            Text(
                text = stringResource(R.string.label_sensor)
            )
        },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Default.Sensors,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(enabled = selectedDevice != null, onClick = {
                focusRequester.requestFocus()
                isSensorsMenuExpanded = true
            }) {
                Icon(
                    modifier = Modifier.rotate(if (isSensorsMenuExpanded) 180f else 0f),
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
                if (isSensorsMenuExpanded)
                    selectedDevice?.let { device ->
                        ShowSensorsDropdown(
                            sensors = device.sensors,
                            onSensorSelected = { sensor ->
                                onSensorSelected(sensor)
                                isSensorsMenuExpanded = false
                            },
                            onDismiss = {
                                isSensorsMenuExpanded = false
                                focusManager.clearFocus()
                            })
                    }

            }
        },
        singleLine = true
    )
    OutlinedTextField(
        value = "$sampleLength ms",
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        enabled = selectedDevice != null,
        readOnly = true,
        label = {
            Text(
                text = stringResource(R.string.label_sample_length)
            )
        },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Outlined.Timer,
                contentDescription = null
            )
        },
        trailingIcon = {
            Row {
                IconButton(
                    onClick = {
                        if (sampleLength.plus(SAMPLE_DELTA) <= MAX_SAMPLE_LENGTH)
                            sampleLength = sampleLength.plus(SAMPLE_DELTA)
                    },
                    enabled = selectedSensor != null
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
                IconButton(
                    onClick = {
                        if (sampleLength.minus(SAMPLE_DELTA) > MIN_SAMPLE_LENGTH)
                            sampleLength = sampleLength.minus(SAMPLE_DELTA)
                    },
                    enabled = selectedSensor != null
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.Remove,
                        contentDescription = null
                    )
                }
            }
        },
        singleLine = true
    )
    OutlinedTextField(
        value = selectedFrequency?.toString() ?: stringResource(id = R.string.empty),
        onValueChange = { },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .focusRequester(focusRequester = focusRequester),
        enabled = selectedSensor?.frequencies?.isNotEmpty() ?: false,
        readOnly = true,
        label = {
            Text(
                text = stringResource(R.string.label_frequency)
            )
        },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_waveform),
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(
                enabled = selectedSensor?.frequencies?.isNotEmpty() ?: false,
                onClick = {
                    focusRequester.requestFocus()
                    isFrequencyMenuExpanded = true
                }) {
                Icon(
                    modifier = Modifier.rotate(if (isFrequencyMenuExpanded) 180f else 0f),
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
                if (isFrequencyMenuExpanded)
                    selectedSensor?.let { sensor ->
                        ShowFrequenciesDropdown(
                            frequencies = sensor.frequencies,
                            onFrequencySelected = { frequency ->
                                onFrequencySelected(frequency)
                                isFrequencyMenuExpanded = false
                            }
                        ) {
                            isFrequencyMenuExpanded = false
                            focusManager.clearFocus()
                        }
                    }

            }
        },
        singleLine = true
    )
}

@Composable
private fun ShowDevicesDropdown(
    connectedDevices: List<Device>,
    onDeviceSelected: (Device) -> Unit,
    onDismiss: () -> Unit,
) {
    DropdownMenu(
        modifier = Modifier
            .fillMaxWidth(),
        expanded = true,
        onDismissRequest = {
            Log.i("AA", "Dismissed?")
            onDismiss()
        }) {
        connectedDevices.forEach { device ->
            DropdownMenuItem(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { onDeviceSelected(device) }) {
                Text(
                    modifier = Modifier.weight(1.0f),
                    text = device.name
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        //TODO Add green to display connected devices
                        .background(color = NordicGrass, shape = CircleShape)
                )
            }
        }
    }
}

@Composable
private fun ShowSensorsDropdown(
    sensors: List<Sensor>,
    onSensorSelected: (Sensor) -> Unit,
    onDismiss: () -> Unit,
) {
    DropdownMenu(
        modifier = Modifier
            .fillMaxWidth(),
        expanded = true,
        onDismissRequest = {
            Log.i("AA", "Dismissed?")
            onDismiss()
        }) {
        sensors.forEach { sensor ->
            DropdownMenuItem(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { onSensorSelected(sensor) }) {
                Text(
                    modifier = Modifier.weight(1.0f),
                    text = sensor.name
                )
            }
        }
    }
}

@Composable
private fun ShowFrequenciesDropdown(
    frequencies: List<Number>,
    onFrequencySelected: (Number) -> Unit,
    onDismiss: () -> Unit,
) {
    DropdownMenu(
        modifier = Modifier
            .fillMaxWidth(),
        expanded = true,
        onDismissRequest = {
            Log.i("AA", "Dismissed?")
            onDismiss()
        }) {

        frequencies.forEach { frequency ->
            DropdownMenuItem(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                onClick = { onFrequencySelected(frequency) }) {
                Text(
                    modifier = Modifier.weight(1.0f),
                    text = frequency.toString()
                )
            }
        }
    }
}

private const val MIN_SAMPLE_LENGTH = 0
private const val SAMPLE_DELTA = 10
private const val MAX_SAMPLE_LENGTH = 10000