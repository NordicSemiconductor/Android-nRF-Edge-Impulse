package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
                content()
            }
        }
    }
}

@Composable
fun RecordSampleSmallScreen(
    isLandscape: Boolean,
    content: @Composable () -> Unit,
    onCloseClicked: () -> Unit
) {
    Scaffold(
        modifier = Modifier.wrapContentHeight(),
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
                .wrapContentHeight()
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
        if (selectedDevice == null) {
            onDeviceSelected(connectedDevices[0])
        }
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
                if (isCategoryExpanded) {
                    ShowCategoryDropdown(
                        onCategorySelected = {
                            onCategorySelected(it)
                            isCategoryExpanded = false
                        },
                        onDismiss = {
                            isCategoryExpanded = false
                            focusManager.clearFocus()
                        }
                    )
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
                if (isDevicesMenuExpanded) {
                    ShowDevicesDropdown(
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
                if (isSensorsMenuExpanded) {
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
                            }
                        )
                    }
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
        enabled = connectedDevices.isNotEmpty() && (samplingState is Finished || samplingState is Unknown),
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
                IconButton(
                    onClick = {
                        selectedSensor?.let { sensor ->
                            if (sampleLength + SAMPLE_LENGTH_DELTA <= sensor.maxSampleLengths)
                                onSampleLengthChanged(sampleLength + SAMPLE_LENGTH_DELTA)
                        }
                    },
                    enabled = connectedDevices.isNotEmpty() && (samplingState is Finished || samplingState is Unknown)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
                IconButton(
                    onClick = {
                        if (sampleLength - SAMPLE_LENGTH_DELTA > MIN_SAMPLE_LENGTH)
                            onSampleLengthChanged(sampleLength - SAMPLE_LENGTH_DELTA)
                    },
                    enabled = connectedDevices.isNotEmpty() && (samplingState is Finished || samplingState is Unknown)
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
                if (isFrequencyMenuExpanded) {
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

            }
        },
        singleLine = true
    )
    if (connectedDevices.isNotEmpty() && samplingState !is Unknown) {
        Surface(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .clip(shape = RoundedCornerShape(8.dp))
        ) {
            Row(
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1.0f), text = when (samplingState) {
                        is Request -> {
                            "Sending sample request to device..."
                        }
                        is Response -> {
                            "Response to sample request received..."
                        }
                        is ProgressEvent.Started -> {
                            "Sampling started..."
                        }
                        is ProgressEvent.Processing -> {
                            "Sample processing..."
                        }
                        is ProgressEvent.Uploading -> {
                            "Uploading sample..."
                        }
                        is Finished -> {
                            "Sampling finished."
                        }
                        else -> {
                            "Unknown."
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
fun ShowCategoryDropdown(
    onCategorySelected: (Category) -> Unit,
    onDismiss: () -> Unit,
) {
    val categories = listOf(Category.TRAINING, Category.TESTING)
    DropdownMenu(
        modifier = Modifier
            .fillMaxWidth(),
        expanded = true,
        onDismissRequest = onDismiss
    ) {
        categories.forEach { category ->
            DropdownMenuItem(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { onCategorySelected(category) }
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

@Composable
fun ShowDevicesDropdown(
    connectedDevices: List<Device>,
    onDeviceSelected: (Device) -> Unit,
    onDismiss: () -> Unit,
) {
    DropdownMenu(
        modifier = Modifier
            .fillMaxWidth(),
        expanded = true,
        onDismissRequest = onDismiss
    ) {
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
        onDismissRequest = onDismiss
    ) {
        sensors.forEach { sensor ->
            DropdownMenuItem(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { onSensorSelected(sensor) }
            ) {
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
        onDismissRequest = onDismiss
    ) {
        frequencies.forEach { frequency ->
            DropdownMenuItem(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                onClick = { onFrequencySelected(frequency) }
            ) {
                Text(
                    modifier = Modifier.weight(1.0f),
                    text = frequency.toString()
                )
            }
        }
    }
}

private const val MIN_SAMPLE_LENGTH = 0
private const val SAMPLE_LENGTH_DELTA = 10