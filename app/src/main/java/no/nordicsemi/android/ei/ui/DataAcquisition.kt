package no.nordicsemi.android.ei.ui

import android.util.Log
import androidx.annotation.IntRange
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.flow.collect
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.Device.Sensor
import no.nordicsemi.android.ei.model.Sample
import no.nordicsemi.android.ei.showSnackbar
import no.nordicsemi.android.ei.ui.theme.NordicGrass
import no.nordicsemi.android.ei.viewmodels.DataAcquisitionViewModel
import no.nordicsemi.android.ei.viewmodels.event.Error
import java.net.UnknownHostException
import java.util.*

@OptIn(ExperimentalPagerApi::class)
@ExperimentalMaterialApi
@Composable
fun DataAcquisition(
    modifier: Modifier,
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    pagerState : PagerState,
    viewModel: DataAcquisitionViewModel,
    connectedDevices: List<Device>,
    displayCreateSampleFab: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val trainingListState = rememberLazyListState()
    val testingListState = rememberLazyListState()
    val anomalyListState = rememberLazyListState()

    displayCreateSampleFab(bottomSheetScaffoldState.bottomSheetState.isCollapsed)
    LocalLifecycleOwner.current.lifecycleScope.launchWhenStarted {
        viewModel.eventFlow.runCatching {
            this.collect {
                when (it) {
                    is Error -> {
                        showSnackbar(
                            coroutineScope = coroutineScope,
                            snackbarHostState = snackbarHostState,
                            message = when (it.throwable) {
                                is UnknownHostException -> context.getString(R.string.error_no_internet)
                                else -> it.throwable.localizedMessage
                                    ?: context.getString(R.string.error_refreshing_failed)
                            }
                        )
                    }
                }
            }
        }
    }
    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            CreateSample(
                viewModel = viewModel,
                connectedDevices = connectedDevices
            )
        },
        sheetShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        sheetElevation = 4.dp,
        sheetPeekHeight = 0.dp
    ) {

        //TODO display empty data message
        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> viewModel.trainingSamples
                1 -> viewModel.testingSamples
                else -> viewModel.anomalySamples
            }.takeIf {
                it.isNotEmpty()
            }?.let { notEmptyList ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    state = when (page) {
                        0 -> trainingListState
                        1 -> testingListState
                        else -> anomalyListState
                    }
                ) {
                    items(items = notEmptyList, key = {
                        it.id
                    }) { sample ->
                        CollectedDataRow(sample = sample, page)
                        Divider()
                    }
                }
            } ?: run {
                Column(
                    modifier = modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(64.dp)
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(text = stringResource(R.string.label_loading_collected_data))
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RecordDataFloatingActionButton(onClick: () -> Unit) {
    // Toggle the visibility of the content with animation.
    FloatingActionButton(onClick = onClick) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Text(
                text = stringResource(R.string.action_record_new_data),
                modifier = Modifier
                    .padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun CollectedDataRow(sample: Sample, @IntRange(from = 0, to = 2) page: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (page) {
                0 -> Icons.Outlined.ModelTraining
                1 -> Icons.Outlined.Science
                else -> Icons.Outlined.Psychology
            },
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colors.primary,
                    shape = CircleShape
                )
                .padding(8.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = sample.filename,
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.body1
            )
            Text(
                text = sample.added,
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.caption
            )
        }
    }
}

@Composable
fun CreateSample(
    viewModel: DataAcquisitionViewModel,
    connectedDevices: List<Device>
) {
    val focusRequester = viewModel.focusRequester
    val focusManager = LocalFocusManager.current
    val selectedDevice = viewModel.selectedDevice
    val label = viewModel.label
    val selectedSensor = viewModel.selectedSensor
    val selectedFrequency = viewModel.selectedFrequency
    var isDevicesMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isSensorsMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isFrequencyMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var sampleLength by remember { mutableStateOf(5000) }
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
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
                                viewModel.onDeviceSelected(device)
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
            onValueChange = { viewModel.onLabelChanged(it) },
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
                                    viewModel.onFrequencySelected(frequency)
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
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(
                enabled = selectedSensor != null,
                onClick = { /*TODO*/ }) {
                Text(
                    text = stringResource(R.string.action_start_sampling).toUpperCase(Locale.ROOT),
                    style = MaterialTheme.typography.button
                )
            }
        }
    }
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