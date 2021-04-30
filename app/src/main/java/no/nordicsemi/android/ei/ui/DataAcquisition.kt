package no.nordicsemi.android.ei.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.Sensor
import no.nordicsemi.android.ei.ui.theme.NordicGrass
import no.nordicsemi.android.ei.viewmodels.DataAcquisitionViewModel

@Composable
fun DataAcquisition(
    modifier: Modifier,
    viewModel: DataAcquisitionViewModel,
    connectedDevices: List<Device>
) {

    val focusRequester = viewModel.focusRequester
    val focusManager = LocalFocusManager.current
    val selectedDevice = viewModel.selectedDevice
    val label = viewModel.label
    val selectedSensor = viewModel.selectedSensor
    var isDevicesMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isSensorsMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var sampleLength by remember { mutableStateOf(10) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn {
            item {
                Text(
                    text = stringResource(R.string.title_collected_data),
                    style = MaterialTheme.typography.h6
                )
            }
        }

        Column {
            Text(
                text = stringResource(R.string.title_record_new_data),
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.height(16.dp))
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
                        modifier = Modifier.size(24.dp),
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
                                configuredDevices = connectedDevices,
                                onDeviceSelected = { device ->
                                    viewModel.onDeviceSelected(device)
                                    device.sensors.takeIf { sensors -> sensors.isNotEmpty() }
                                        ?.let { sensors -> viewModel.onSensorSelected(sensor = sensors[0]) }
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
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                readOnly = true,
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
                    IconButton(onClick = {
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
                                        viewModel.onSensorSelected(sensor)
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
                value = sampleLength.toString(),
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .focusRequester(focusRequester = focusRequester),
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
                                focusRequester.requestFocus()
                                sampleLength = sampleLength.inc()
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
                                focusRequester.requestFocus()
                                sampleLength = sampleLength.dec()
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
        }
    }
}

@Composable
private fun ShowDevicesDropdown(
    configuredDevices: List<Device>,
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
        configuredDevices.forEach { device ->
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