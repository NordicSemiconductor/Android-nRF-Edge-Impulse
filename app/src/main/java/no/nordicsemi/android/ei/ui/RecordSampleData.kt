package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Label
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Category
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.Message
import no.nordicsemi.android.ei.model.Message.Sample.*
import no.nordicsemi.android.ei.model.Sensor
import no.nordicsemi.android.ei.ui.theme.NordicGrass
import no.nordicsemi.android.ei.ui.theme.NordicRed
import java.util.*


@Composable
fun RecordSampleLargeScreen(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .verticalScroll(state = rememberScrollState())
            .padding(end = 16.dp)
    ) {
        content()
    }
}

@Composable
fun RecordSampleSmallScreen(
    isLandscape: Boolean,
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
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
                .verticalScroll(
                    state = rememberScrollState(),
                    enabled = isLandscape
                )
        ) {
            content()
        }
    }
}

@Composable
fun RecordSampleContent(
    samplingState: Message.Sample?,
    connectedDevices: List<Device>,
    focusRequester: FocusRequester,
    category: Category,
    onCategorySelected: (Category) -> Unit,
    selectedDevice: Device?,
    onDeviceSelected: (Device) -> Unit,
    label: String,
    onLabelChanged: (String) -> Unit,
    selectedSensor: Sensor?,
    onSensorSelected: (Sensor) -> Unit,
    sampleLength: Int,
    onSampleLengthChanged: (Int) -> Unit,
    selectedFrequency: Number?,
    onFrequencySelected: (Number) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var isCategoryExpanded by rememberSaveable { mutableStateOf(false) }
    var isDevicesMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isSensorsMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isFrequencyMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val categories = listOf(Category.TRAINING, Category.TESTING)
    val maxSampleLengthMs = selectedSensor?.let { it.maxSampleLengths.toFloat() * 1000 }
        ?: MAX_SAMPLE_LENGTH_MS
    var sliderPosition by rememberSaveable { mutableStateOf(1f * 1000)}
    var width by rememberSaveable { mutableStateOf(0) }
    //TODO clear data when if the device gets disconnected?
    connectedDevices.takeIf { it.isEmpty() }?.apply {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
                Icon(
                    modifier = Modifier
                        .size(36.dp),
                    tint = NordicRed,
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = context.getString(R.string.connect_device_for_data_acquisition)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = context.getString(R.string.connect_device_for_data_acquisition))
            }
        }
        Spacer(modifier = Modifier.height(height = 16.dp))
    } ?: run {
        onDeviceSelected(connectedDevices[0])
    }
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
            .onSizeChanged { width = it.width }
            .focusRequester(focusRequester = focusRequester),
        readOnly = true,
        label = {
            Text(text = stringResource(R.string.label_category))
        },
        leadingIcon = {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .padding(4.dp),
                imageVector = Icons.Default.Category,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(
                enabled = (samplingState is Finished || samplingState is Unknown),
                onClick = {
                    focusRequester.requestFocus()
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
                        focusManager.clearFocus()
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
        value = selectedDevice?.name ?: stringResource(id = R.string.empty),
        onValueChange = { },
        enabled = connectedDevices.isNotEmpty() && (samplingState is Finished || samplingState is Unknown),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .focusRequester(focusRequester = focusRequester),
        readOnly = true,
        label = {
            Text(text = stringResource(R.string.label_device))
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
            IconButton(
                enabled = connectedDevices.isNotEmpty() && (samplingState is Finished || samplingState is Unknown),
                onClick = {
                    focusRequester.requestFocus()
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
                        onDeviceSelected(device)
                        isDevicesMenuExpanded = false
                    },
                    onDismiss = {
                        isDevicesMenuExpanded = false
                        focusManager.clearFocus()
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
        singleLine = true
    )
    OutlinedTextField(
        value = selectedSensor?.name ?: stringResource(id = R.string.empty),
        onValueChange = { },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .focusRequester(focusRequester = focusRequester),
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
                    focusRequester.requestFocus()
                    isSensorsMenuExpanded = true
                }
            ) {
                Icon(
                    modifier = Modifier.rotate(if (isSensorsMenuExpanded) 180f else 0f),
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
                selectedDevice?.let { device ->
                    ShowDropdown(
                        modifier = Modifier.width(with(LocalDensity.current) { width.toDp() }),
                        expanded = isSensorsMenuExpanded,
                        onDismiss = {
                            isSensorsMenuExpanded = false
                            focusManager.clearFocus()
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
    Spacer(modifier = Modifier.height(16.dp))
    Row {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.label_sample_length),
            textAlign = TextAlign.Start
        )
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.label_ms, sliderPosition.toInt()),
            textAlign = TextAlign.End
        )
    }
    Slider(
        modifier = Modifier
            .fillMaxWidth(),
        value = sliderPosition,
        onValueChange = { sliderPosition = it },
        onValueChangeFinished = {onSampleLengthChanged(sliderPosition.toInt())},
        enabled = connectedDevices.isNotEmpty() && (samplingState is Finished || samplingState is Unknown),
        steps = 10000,
        valueRange = MIN_SAMPLE_LENGTH..maxSampleLengthMs
    )

    OutlinedTextField(
        value = selectedFrequency?.toString() ?: stringResource(id = R.string.empty),
        onValueChange = { },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .focusRequester(focusRequester = focusRequester),
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
                    focusRequester.requestFocus()
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
                            focusManager.clearFocus()
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
    if (connectedDevices.isNotEmpty() && (samplingState !is Unknown)) {
        Surface(
            modifier = Modifier
                .padding(top = 16.dp)
                .clip(shape = RoundedCornerShape(8.dp)),
            elevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1.0f), text = when (samplingState) {
                        is Request -> {
                            stringResource(R.string.label_sending_sample_request)
                        }
                        is Response -> {
                            stringResource(R.string.label_sampling_response_received)
                        }
                        is ProgressEvent.Started -> {
                            stringResource(R.string.label_sampling_started)
                        }
                        is ProgressEvent.Processing -> {
                            stringResource(R.string.label_sampling_processing)
                        }
                        is ProgressEvent.Uploading -> {
                            stringResource(R.string.label_uploading_started)
                        }
                        is Finished -> stringResource(R.string.label_sampling_finished).plus(
                            if (samplingState.error != null)
                                " : ${samplingState.error}"
                            else "."
                        )
                        else -> {
                            stringResource(R.string.unknown)
                        }
                    }
                )
                if (samplingState !is Finished && samplingState !is Unknown)
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
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
                            .background(color = NordicGrass, shape = CircleShape)
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

private const val MIN_SAMPLE_LENGTH = 1f * 1000
private const val SAMPLE_LENGTH_DELTA = 10
private const val MAX_SAMPLE_LENGTH_MS = 10f * 1000