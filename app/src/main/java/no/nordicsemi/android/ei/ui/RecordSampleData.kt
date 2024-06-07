/*
 * Copyright (c) 2022, Nordic Semiconductor
 *
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Category
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.Message
import no.nordicsemi.android.ei.model.Message.Sample.Finished
import no.nordicsemi.android.ei.model.Message.Sample.Unknown
import no.nordicsemi.android.ei.model.Sensor
import no.nordicsemi.android.ei.ui.layouts.BottomSheetAppBar
import no.nordicsemi.android.ei.ui.layouts.DeviceDisconnected
import no.nordicsemi.android.ei.viewmodels.ProjectViewModel
import java.util.Locale

@Composable
fun RecordSampleLargeScreen(
    viewModel: ProjectViewModel,
    connectedDevices: List<Device>,
    onSamplingMessageDismissed: (Boolean) -> Unit
) {
    Column {
        SamplingMessage(
            isSamplingMessageVisible = viewModel.samplingState !is Unknown,
            onSamplingMessageDismissed = onSamplingMessageDismissed,
            samplingState = viewModel.samplingState,
            isSamplingStartedFromDevice = viewModel.isSamplingStartedFromDevice
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(state = rememberScrollState())
                .weight(weight = 1f, fill = false)
        ) {
            RecordSampleContent(
                connectedDevices = connectedDevices,
                samplingState = viewModel.samplingState,
                category = viewModel.category,
                onCategorySelected = { viewModel.onCategoryChanged(it) },
                dataAcquisitionTarget = viewModel.dataAcquisitionTarget,
                onDataAcquisitionTargetSelected = {
                    viewModel.onDataAcquisitionTargetSelected(
                        device = it
                    )
                },
                label = viewModel.label,
                onLabelChanged = { viewModel.onLabelChanged(label = it) },
                selectedSensor = viewModel.sensor,
                onSensorSelected = { viewModel.onSensorSelected(sensor = it) },
                sampleLength = viewModel.sampleLength,
                onSampleLengthChanged = { viewModel.onSampleLengthChanged(it) },
                selectedFrequency = viewModel.frequency,
                onFrequencySelected = { viewModel.onFrequencySelected(frequency = it) },
            )
        }
    }
}

@Composable
fun RecordSampleSmallScreen(
    viewModel: ProjectViewModel,
    connectedDevices: List<Device>,
    onSamplingMessageDismissed: (Boolean) -> Unit,
    buttonContent: @Composable () -> Unit,
    onCloseClicked: () -> Unit
) {
    BottomSheetAppBar(
        imageVector = Icons.Default.Close,
        title = stringResource(R.string.title_record_new_data),
        onBackPressed = onCloseClicked
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        SamplingMessage(
            isSamplingMessageVisible = viewModel.samplingState !is Unknown,
            onSamplingMessageDismissed = onSamplingMessageDismissed,
            samplingState = viewModel.samplingState,
            isSamplingStartedFromDevice = viewModel.isSamplingStartedFromDevice
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(state = rememberScrollState()),
        ) {
            RecordSampleContent(
                connectedDevices = connectedDevices,
                samplingState = viewModel.samplingState,
                category = viewModel.category,
                onCategorySelected = { viewModel.onCategoryChanged(it) },
                dataAcquisitionTarget = viewModel.dataAcquisitionTarget,
                onDataAcquisitionTargetSelected = {
                    viewModel.onDataAcquisitionTargetSelected(
                        device = it
                    )
                },
                label = viewModel.label,
                onLabelChanged = { viewModel.onLabelChanged(label = it) },
                selectedSensor = viewModel.sensor,
                onSensorSelected = { viewModel.onSensorSelected(sensor = it) },
                sampleLength = viewModel.sampleLength,
                onSampleLengthChanged = { viewModel.onSampleLengthChanged(it) },
                selectedFrequency = viewModel.frequency,
                onFrequencySelected = { viewModel.onFrequencySelected(frequency = it) },
            )
            buttonContent()
        }
    }
}

@Composable
private fun RecordSampleContent(
    samplingState: Message.Sample,
    connectedDevices: List<Device>,
    category: Category,
    onCategorySelected: (Category) -> Unit,
    dataAcquisitionTarget: Device?,
    onDataAcquisitionTargetSelected: (Device) -> Unit,
    label: String,
    onLabelChanged: (String) -> Unit,
    selectedSensor: Sensor?,
    onSensorSelected: (Sensor) -> Unit,
    sampleLength: Int,
    onSampleLengthChanged: (Int) -> Unit,
    selectedFrequency: Number?,
    onFrequencySelected: (Number) -> Unit
) {
    var isLabelError by rememberSaveable {
        mutableStateOf(false)
    }
    connectedDevices.takeIf { it.isNotEmpty() }?.apply {
        if (dataAcquisitionTarget == null) {
            onDataAcquisitionTargetSelected(connectedDevices[0])
        }
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DeviceSelection(
            connectedDevices = connectedDevices,
            samplingState = samplingState,
            dataAcquisitionTarget = dataAcquisitionTarget,
            onDataAcquisitionTargetSelected = onDataAcquisitionTargetSelected
        )
        CategorySelection(
            connectedDevices = connectedDevices,
            category = category,
            samplingState = samplingState,
            onCategorySelected = onCategorySelected
        )
        LabelInput(
            connectedDevices = connectedDevices,
            samplingState = samplingState,
            label = label,
            onLabelChanged = {
                isLabelError = it.isEmpty()
                onLabelChanged(it)
            },
            isLabelError = isLabelError
        )
        SensorSelection(
            connectedDevices = connectedDevices,
            dataAcquisitionTarget = dataAcquisitionTarget,
            samplingState = samplingState,
            selectedSensor = selectedSensor,
            onSensorSelected = onSensorSelected
        )
        SampleLengthInput(
            connectedDevices = connectedDevices,
            samplingState = samplingState,
            selectedSensor = selectedSensor,
            sampleLength = sampleLength,
            onSampleLengthChanged = onSampleLengthChanged
        )
        FrequencySelection(
            connectedDevices = connectedDevices,
            samplingState = samplingState,
            selectedSensor = selectedSensor,
            selectedFrequency = selectedFrequency,
            onFrequencySelected = onFrequencySelected
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceSelection(
    connectedDevices: List<Device>,
    samplingState: Message.Sample,
    dataAcquisitionTarget: Device?,
    onDataAcquisitionTargetSelected: (Device) -> Unit
) {
    var width by rememberSaveable { mutableIntStateOf(0) }
    var isDevicesMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val isEnabled = shouldEnable(
        connectedDevices = connectedDevices,
        samplingState = samplingState
    )
    ExposedDropdownMenuBox(
        expanded = isDevicesMenuExpanded,
        onExpandedChange = { isDevicesMenuExpanded = isEnabled }
    ) {
        OutlinedTextField(
            value = dataAcquisitionTarget?.name ?: stringResource(id = R.string.empty),
            onValueChange = { },
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .onSizeChanged { width = it.width },
            readOnly = true,
            label = {
                DeviceDisconnected(connectedDevices = connectedDevices)
            },
            leadingIcon = {
                Icon(
                    modifier = Modifier
                        .size(24.dp),
                    imageVector = Icons.Rounded.DeveloperBoard,
                    contentDescription = null
                )
            },
            trailingIcon = {
                Icon(
                    modifier = Modifier.rotate(if (isDevicesMenuExpanded) 180f else 0f),
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            },
            singleLine = true
        )
        ShowDevicesDropdown(
            modifier = Modifier.exposedDropdownSize(),
            expanded = isDevicesMenuExpanded,
            connectedDevices = connectedDevices,
            onDeviceSelected = { device ->
                onDataAcquisitionTargetSelected(device)
                isDevicesMenuExpanded = false
            },
            onDismiss = {
                isDevicesMenuExpanded = false
            }
        )
    }
}

@Composable
private fun CategorySelection(
    connectedDevices: List<Device>,
    samplingState: Message.Sample,
    category: Category,
    onCategorySelected: (Category) -> Unit
) {
    var width by rememberSaveable { mutableIntStateOf(0) }
    var isCategoryExpanded by rememberSaveable { mutableStateOf(false) }
    val isEnabled = shouldEnable(
        connectedDevices = connectedDevices,
        samplingState = samplingState
    )
    ExposedDropdownMenuBox(
        expanded = isCategoryExpanded,
        onExpandedChange = { isCategoryExpanded = isEnabled }
    ) {
        OutlinedTextField(
            value = category.type.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.US
                ) else it.toString()
            },
            onValueChange = { },
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .onSizeChanged { width = it.width },
            readOnly = true,
            label = {
                Text(text = stringResource(R.string.label_category))
            },
            leadingIcon = {
                Icon(
                    modifier = Modifier
                        .size(24.dp),
                    imageVector = Icons.Default.Category,
                    contentDescription = null
                )
            },
            trailingIcon = {
                Icon(
                    modifier = Modifier.rotate(if (isCategoryExpanded) 180f else 0f),
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            },
            singleLine = true
        )
        ShowDropdown(
            modifier = Modifier.exposedDropdownSize(),
            expanded = isCategoryExpanded,
            onDismiss = {
                isCategoryExpanded = false
            }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = {
                        Text(
                            modifier = Modifier.weight(1.0f),
                            text = category.type.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.US
                                ) else it.toString()
                            }
                        )
                    },
                    onClick = {
                        onCategorySelected(category)
                        isCategoryExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LabelInput(
    connectedDevices: List<Device>,
    samplingState: Message.Sample,
    label: String,
    onLabelChanged: (String) -> Unit,
    isLabelError: Boolean = false
) {
    OutlinedTextField(
        value = label,
        onValueChange = { onLabelChanged(it) },
        enabled = shouldEnable(
            connectedDevices = connectedDevices,
            samplingState = samplingState
        ),
        modifier = Modifier
            .fillMaxWidth(),
        label = {
            Text(text = stringResource(R.string.label_label))
        },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.AutoMirrored.Outlined.Label,
                contentDescription = null
            )
        },
        isError = isLabelError,
        singleLine = true
    )
    if (isLabelError) {
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = stringResource(R.string.label_empty_label_error),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SensorSelection(
    connectedDevices: List<Device>,
    dataAcquisitionTarget: Device?,
    samplingState: Message.Sample,
    selectedSensor: Sensor?,
    onSensorSelected: (Sensor) -> Unit
) {
    var width by rememberSaveable { mutableIntStateOf(0) }
    var isSensorsMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val isEnabled = shouldEnable(
        connectedDevices = connectedDevices,
        samplingState = samplingState
    )
    ExposedDropdownMenuBox(
        expanded = isSensorsMenuExpanded,
        onExpandedChange = { isSensorsMenuExpanded = isEnabled }
    ) {
        OutlinedTextField(
            value = selectedSensor?.name ?: stringResource(id = R.string.empty),
            onValueChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .onSizeChanged { width = it.width },
            enabled = isEnabled,
            readOnly = true,
            label = {
                Text(text = stringResource(R.string.label_sensor))
            },
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null
                )
            },
            trailingIcon = {
                Icon(
                    modifier = Modifier.rotate(if (isSensorsMenuExpanded) 180f else 0f),
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            },
            singleLine = true
        )
        dataAcquisitionTarget?.let { device ->
            ShowDropdown(
                modifier = Modifier.exposedDropdownSize(),
                expanded = isSensorsMenuExpanded,
                onDismiss = {
                    isSensorsMenuExpanded = false
                }) {
                device.sensors.forEach { sensor ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                modifier = Modifier.weight(1.0f),
                                text = sensor.name
                            )
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {
                            onSensorSelected(sensor)
                            isSensorsMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SampleLengthInput(
    connectedDevices: List<Device>,
    samplingState: Message.Sample,
    selectedSensor: Sensor?,
    sampleLength: Int,
    onSampleLengthChanged: (Int) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isEnabled = shouldEnable(
        connectedDevices = connectedDevices,
        samplingState = samplingState
    )
    OutlinedTextField(
        value = sampleLength.toString(),
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth(),
        enabled = isEnabled,
        readOnly = true,
        label = {
            Text(text = stringResource(R.string.label_sample_length))
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
                val incrementalInteractionSource = remember { MutableInteractionSource() }
                val isPlusPressed by incrementalInteractionSource.collectIsPressedAsState()
                val decrementalInteractionSource = remember { MutableInteractionSource() }
                val isMinusPressed by decrementalInteractionSource.collectIsPressedAsState()
                IconButton(
                    onClick = {
                        selectedSensor?.let { sensor ->
                            if (sampleLength + SAMPLE_LENGTH_DELTA <= sensor.maxSampleLengths * 1000) {
                                onSampleLengthChanged(sampleLength + SAMPLE_LENGTH_DELTA)
                            } else if (sampleLength < sensor.maxSampleLengths * 1000) {
                                onSampleLengthChanged(sampleLength + MIN_SAMPLE_LENGTH_S)
                            }
                        }
                    },
                    enabled = shouldEnable(
                        connectedDevices = connectedDevices,
                        samplingState = samplingState
                    ),
                    interactionSource = incrementalInteractionSource
                ) {
                    if (isPlusPressed) {
                        selectedSensor?.let { sensor ->
                            if (sampleLength + SAMPLE_LENGTH_DELTA <= sensor.maxSampleLengths * 1000) {
                                onSampleLengthChanged(sampleLength + SAMPLE_LENGTH_DELTA)
                            } else if (sampleLength < sensor.maxSampleLengths * 1000) {
                                onSampleLengthChanged(sampleLength + MIN_SAMPLE_LENGTH_S)
                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
                IconButton(
                    onClick = {
                        if (sampleLength - SAMPLE_LENGTH_DELTA >= MIN_SAMPLE_LENGTH_S) {
                            onSampleLengthChanged(sampleLength - SAMPLE_LENGTH_DELTA)
                        } else if (sampleLength > MIN_SAMPLE_LENGTH_S) {
                            onSampleLengthChanged(sampleLength - MIN_SAMPLE_LENGTH_S)
                        }
                    },
                    enabled = shouldEnable(
                        connectedDevices = connectedDevices,
                        samplingState = samplingState
                    ),
                    interactionSource = decrementalInteractionSource
                ) {
                    if (isMinusPressed) {
                        if (sampleLength - SAMPLE_LENGTH_DELTA >= MIN_SAMPLE_LENGTH_S) {
                            onSampleLengthChanged(sampleLength - SAMPLE_LENGTH_DELTA)
                        } else if (sampleLength > MIN_SAMPLE_LENGTH_S) {
                            onSampleLengthChanged(sampleLength - MIN_SAMPLE_LENGTH_S)
                        }
                    }
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.Remove,
                        contentDescription = null
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(onNext = {
            keyboardController?.hide()
            focusManager.moveFocus(FocusDirection.Down)
        }),
        singleLine = true
    )
}

@Composable
private fun FrequencySelection(
    connectedDevices: List<Device>,
    samplingState: Message.Sample,
    selectedSensor: Sensor?,
    selectedFrequency: Number?,
    onFrequencySelected: (Number) -> Unit
) {
    var width by rememberSaveable { mutableIntStateOf(0) }
    var isFrequencyMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val isEnabled = shouldEnable(
        connectedDevices = connectedDevices,
        samplingState = samplingState
    )
    ExposedDropdownMenuBox(
        expanded = isFrequencyMenuExpanded,
        onExpandedChange = { isFrequencyMenuExpanded = isEnabled }
    ) {
        OutlinedTextField(
            value = selectedFrequency?.toString() ?: stringResource(id = R.string.empty),
            onValueChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .onSizeChanged { width = it.width },
            enabled = isEnabled,
            readOnly = true,
            label = { Text(text = stringResource(R.string.label_frequency)) },
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Outlined.GraphicEq,
                    contentDescription = null
                )
            },
            trailingIcon = {
                Icon(
                    modifier = Modifier.rotate(if (isFrequencyMenuExpanded) 180f else 0f),
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            },
            singleLine = true
        )
        selectedSensor?.let { sensor ->
            ShowDropdown(
                modifier = Modifier.exposedDropdownSize(),
                expanded = isFrequencyMenuExpanded,
                onDismiss = {
                    isFrequencyMenuExpanded = false
                }
            ) {
                sensor.frequencies.forEach { frequency ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                modifier = Modifier.weight(1.0f),
                                text = frequency.toString()
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        onClick = {
                            onFrequencySelected(frequency)
                            isFrequencyMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ShowDevicesDropdown(
    modifier: Modifier,
    connectedDevices: List<Device>,
    expanded: Boolean,
    onDeviceSelected: (Device) -> Unit,
    onDismiss: () -> Unit,
) {
    ShowDropdown(
        modifier = modifier,
        expanded = expanded,
        onDismiss = onDismiss,
        content = {
            connectedDevices.forEach { device ->
                DropdownMenuItem(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .padding(start = 8.dp)
                                    .background(color = Color.Green, shape = CircleShape)
                            )
                            Text(
                                modifier = Modifier
                                    .weight(1.0f)
                                    .padding(start = 16.dp),
                                text = device.name
                            )
                        }
                    },
                    onClick = { onDeviceSelected(device) }
                )
            }
        }
    )
}

@Composable
fun ShowDropdown(
    modifier: Modifier,
    expanded: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        content()
    }
}

@Composable
private fun shouldEnable(connectedDevices: List<Device>, samplingState: Message.Sample): Boolean =
    connectedDevices.isNotEmpty() && (samplingState is Finished || samplingState is Unknown)

private const val MIN_SAMPLE_LENGTH_S = 1
private const val SAMPLE_LENGTH_DELTA = 100
val categories = listOf(Category.TRAINING, Category.TESTING)