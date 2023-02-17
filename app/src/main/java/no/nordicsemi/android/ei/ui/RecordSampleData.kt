/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
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
import no.nordicsemi.android.ei.ui.layouts.DeviceDisconnected
import no.nordicsemi.android.ei.viewmodels.ProjectViewModel
import java.util.*

@Composable
fun RecordSampleLargeScreen(
    viewModel:ProjectViewModel,
    connectedDevices: List<Device>,
    onSamplingMessageDismissed: (Boolean) -> Unit,
    buttonContent: @Composable () -> Unit
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
                onCategorySelected = { viewModel.onCategoryChanged(it)},
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
        buttonContent()
    }
}


@Composable
fun RecordSampleSmallScreen(
    viewModel:ProjectViewModel,
    connectedDevices: List<Device>,
    onSamplingMessageDismissed: (Boolean) -> Unit,
    buttonContent: @Composable () -> Unit,
    onCloseClicked: () -> Unit
) {
    val scaffoldState = rememberScaffoldState(snackbarHostState = SnackbarHostState())
    Scaffold(
        modifier = Modifier.wrapContentHeight(),
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.title_record_new_data))
                },
                navigationIcon = {
                    IconButton(onClick = onCloseClicked) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                })
        }
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
            ) {
                RecordSampleContent(
                    connectedDevices = connectedDevices,
                    samplingState = viewModel.samplingState,
                    category = viewModel.category,
                    onCategorySelected = { viewModel.onCategoryChanged(it)},
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
}

@OptIn(ExperimentalComposeUiApi::class)
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
    Spacer(modifier = Modifier.size(size = 16.dp))
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

@Composable
private fun DeviceSelection(
    connectedDevices: List<Device>,
    samplingState: Message.Sample,
    dataAcquisitionTarget: Device?,
    onDataAcquisitionTargetSelected: (Device) -> Unit
) {
    var width by rememberSaveable { mutableStateOf(0) }
    var isDevicesMenuExpanded by rememberSaveable { mutableStateOf(false) }
    Column {
        OutlinedTextField(
            value = dataAcquisitionTarget?.name ?: stringResource(id = R.string.empty),
            onValueChange = { },
            enabled = shouldEnable(
                connectedDevices = connectedDevices,
                samplingState = samplingState
            ),
            modifier = Modifier
                .fillMaxWidth()
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
                IconButton(
                    enabled = shouldEnable(
                        connectedDevices = connectedDevices,
                        samplingState = samplingState
                    ),
                    onClick = {
                        isDevicesMenuExpanded = true
                    }
                ) {
                    Icon(
                        modifier = Modifier.rotate(if (isDevicesMenuExpanded) 180f else 0f),
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
            },
            singleLine = true
        )
        ShowDevicesDropdown(
            modifier = Modifier.width(with(LocalDensity.current) { width.toDp() }),
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
    var width by rememberSaveable { mutableStateOf(0) }
    var isCategoryExpanded by rememberSaveable { mutableStateOf(false) }
    Column {
        OutlinedTextField(
            value = category.type.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.US
                ) else it.toString()
            },
            onValueChange = { },
            enabled = shouldEnable(
                connectedDevices = connectedDevices,
                samplingState = samplingState
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { width = it.width }
                .padding(top = 16.dp),
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
                IconButton(
                    enabled = (samplingState is Finished || samplingState is Unknown),
                    onClick = {
                        isCategoryExpanded = true
                    }
                ) {
                    Icon(
                        modifier = Modifier.rotate(if (isCategoryExpanded) 180f else 0f),
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
            },
            singleLine = true
        )
        ShowDropdown(
            modifier = Modifier.width(with(LocalDensity.current) { width.toDp() }),
            expanded = isCategoryExpanded,
            onDismiss = {
                isCategoryExpanded = false
            }) {
            categories.forEach { category ->
                DropdownMenuItem(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        onCategorySelected(category)
                        isCategoryExpanded = false
                    }
                ) {
                    Text(
                        modifier = Modifier.weight(1.0f),
                        text = category.type.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.US
                            ) else it.toString()
                        }
                    )
                }
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
            .fillMaxWidth()
            .padding(top = 16.dp),
        label = {
            Text(text = stringResource(R.string.label_label))
        },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Outlined.Label,
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
            color = MaterialTheme.colors.error,
            style = MaterialTheme.typography.caption
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
    var width by rememberSaveable { mutableStateOf(0) }
    var isSensorsMenuExpanded by rememberSaveable { mutableStateOf(false) }
    Column {
        OutlinedTextField(
            value = selectedSensor?.name ?: stringResource(id = R.string.empty),
            onValueChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { width = it.width }
                .padding(top = 16.dp),
            enabled = shouldEnable(
                connectedDevices = connectedDevices,
                samplingState = samplingState
            ),
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
                IconButton(
                    enabled = shouldEnable(
                        connectedDevices = connectedDevices,
                        samplingState = samplingState
                    ),
                    onClick = {
                        isSensorsMenuExpanded = true
                    }
                ) {
                    Icon(
                        modifier = Modifier.rotate(if (isSensorsMenuExpanded) 180f else 0f),
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )

                }
            },
            singleLine = true
        )
    }
    dataAcquisitionTarget?.let { device ->
        ShowDropdown(
            modifier = Modifier.width(with(LocalDensity.current) { width.toDp() }),
            expanded = isSensorsMenuExpanded,
            onDismiss = {
                isSensorsMenuExpanded = false
            }) {
            device.sensors.forEach { sensor ->
                DropdownMenuItem(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        onSensorSelected(sensor)
                        isSensorsMenuExpanded = false
                    }
                ) {
                    Text(
                        modifier = Modifier.weight(1.0f),
                        text = sensor.name
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
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
    Column {

        OutlinedTextField(
            value = sampleLength.toString(),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = shouldEnable(
                connectedDevices = connectedDevices,
                samplingState = samplingState
            ),
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
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FrequencySelection(
    connectedDevices: List<Device>,
    samplingState: Message.Sample,
    selectedSensor: Sensor?,
    selectedFrequency: Number?,
    onFrequencySelected: (Number) -> Unit
) {
    var width by rememberSaveable { mutableStateOf(0) }
    var isFrequencyMenuExpanded by rememberSaveable { mutableStateOf(false) }
    Column {
        OutlinedTextField(
            value = selectedFrequency?.toString() ?: stringResource(id = R.string.empty),
            onValueChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { width = it.width }
                .padding(top = 16.dp),
            enabled = shouldEnable(
                connectedDevices = connectedDevices,
                samplingState = samplingState
            ),
            readOnly = true,
            label = { Text(text = stringResource(R.string.label_frequency)) },
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = R.drawable.ic_waveform),
                    contentDescription = null
                )
            },
            trailingIcon = {
                IconButton(
                    enabled = shouldEnable(
                        connectedDevices = connectedDevices,
                        samplingState = samplingState
                    ),
                    onClick = {
                        isFrequencyMenuExpanded = true
                    }
                ) {
                    Icon(
                        modifier = Modifier.rotate(if (isFrequencyMenuExpanded) 180f else 0f),
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
            },
            singleLine = true
        )
        selectedSensor?.let { sensor ->
            ShowDropdown(
                modifier = Modifier.width(with(LocalDensity.current) { width.toDp() }),
                expanded = isFrequencyMenuExpanded,
                onDismiss = {
                    isFrequencyMenuExpanded = false
                }) {
                sensor.frequencies.forEach { frequency ->
                    DropdownMenuItem(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        onClick = {
                            onFrequencySelected(frequency)
                            isFrequencyMenuExpanded = false
                        }
                    ) {
                        Text(
                            modifier = Modifier.weight(1.0f),
                            text = frequency.toString()
                        )
                    }
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
                    onClick = { onDeviceSelected(device) }
                ) {
                    Text(
                        modifier = Modifier.weight(1.0f),
                        text = device.name
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(color = Color.Green, shape = CircleShape)
                    )
                }
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