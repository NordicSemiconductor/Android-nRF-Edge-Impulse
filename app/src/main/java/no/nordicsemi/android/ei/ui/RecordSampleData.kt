package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import java.util.*


@Composable
fun RecordSampleLargeScreen(
    content: @Composable () -> Unit
) {
    content()
}

@Composable
fun RecordSampleSmallScreen(
    content: @Composable () -> Unit,
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
            content()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RecordSampleContent(
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
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isCategoryExpanded by rememberSaveable { mutableStateOf(false) }
    var isDevicesMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isSensorsMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isFrequencyMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val categories = listOf(Category.TRAINING, Category.TESTING)
    var width by rememberSaveable { mutableStateOf(0) }

    connectedDevices.takeIf { it.isNotEmpty() }?.apply {
        Spacer(modifier = Modifier.height(height = 16.dp))
        if (dataAcquisitionTarget == null) {
            onDataAcquisitionTargetSelected(connectedDevices[0])
        }
    }
    //Spacer(modifier = Modifier.size(size = 16.dp))
    OutlinedTextField(
        value = category.type.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.US
            ) else it.toString()
        },
        onValueChange = { },
        enabled = (samplingState is Finished || samplingState is Unknown),
        modifier = Modifier
            .fillMaxWidth()
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
        },
        singleLine = true
    )
    OutlinedTextField(
        value = dataAcquisitionTarget?.name ?: stringResource(id = R.string.empty),
        onValueChange = { },
        enabled = connectedDevices.isNotEmpty() && (samplingState is Finished || samplingState is Unknown),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
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
                enabled = connectedDevices.isNotEmpty() && (samplingState is Finished || samplingState is Unknown),
                onClick = {
                    isDevicesMenuExpanded = true
                }
            ) {
                Icon(
                    modifier = Modifier.rotate(if (isDevicesMenuExpanded) 180f else 0f),
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
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
        },
        singleLine = true
    )
    OutlinedTextField(
        value = label,
        onValueChange = { onLabelChanged(it) },
        enabled = connectedDevices.isNotEmpty() && (samplingState is Finished || samplingState is Unknown),
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
        isError = label.isEmpty(),
        singleLine = true
    )
    OutlinedTextField(
        value = selectedSensor?.name ?: stringResource(id = R.string.empty),
        onValueChange = { },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        enabled = connectedDevices.isNotEmpty() && (samplingState is Finished || samplingState is Unknown),
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
                enabled = connectedDevices.isNotEmpty() && (samplingState is Finished || samplingState is Unknown),
                onClick = {
                    isSensorsMenuExpanded = true
                }
            ) {
                Icon(
                    modifier = Modifier.rotate(if (isSensorsMenuExpanded) 180f else 0f),
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
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
        },
        singleLine = true
    )
    OutlinedTextField(
        value = sampleLength.toString(),
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            disabledTextColor = LocalContentColor.current.copy(LocalContentAlpha.current),
            disabledBorderColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled),
            disabledLabelColor = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium)
        ),
        enabled = false,
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
                    enabled = connectedDevices.isNotEmpty() && (samplingState is Finished || samplingState is Unknown),
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
                    enabled = connectedDevices.isNotEmpty() && (samplingState is Finished || samplingState is Unknown),
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
    OutlinedTextField(
        value = selectedFrequency?.toString() ?: stringResource(id = R.string.empty),
        onValueChange = { },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        enabled = connectedDevices.isNotEmpty() && (samplingState is Finished || samplingState is Unknown),
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
                enabled = connectedDevices.isNotEmpty() && (samplingState is Finished || samplingState is Unknown),
                onClick = {
                    isFrequencyMenuExpanded = true
                }
            ) {
                Icon(
                    modifier = Modifier.rotate(if (isFrequencyMenuExpanded) 180f else 0f),
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
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
        },
        singleLine = true
    )
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

private const val MIN_SAMPLE_LENGTH_S = 1
private const val SAMPLE_LENGTH_DELTA = 100