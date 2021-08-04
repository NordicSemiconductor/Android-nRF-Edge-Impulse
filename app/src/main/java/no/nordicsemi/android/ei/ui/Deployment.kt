package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.comms.BuildState
import no.nordicsemi.android.ei.model.BuildLog
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.showSnackbar
import no.nordicsemi.android.ei.util.Engine
import no.nordicsemi.android.ei.util.ModelType

@Composable
fun Deployment(
    snackbarHostState: SnackbarHostState,
    connectedDevices: List<Device>,
    logs: SnapshotStateList<BuildLog>,
    buildState: BuildState,
    onBuildFirmware: (Engine, ModelType) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var selectedDevice by remember {
        mutableStateOf(connectedDevices.firstOrNull())
    }

    if (buildState is BuildState.Error) {
        showSnackbar(
            snackbarHostState = snackbarHostState,
            coroutineScope = coroutineScope,
            message = buildState.reason ?: context.getString(R.string.error_unknown)
        )
    }

    LazyColumn(contentPadding = PaddingValues(bottom = 56.dp)) {
        item {
            BuildFirmware(
                connectedDevices = connectedDevices,
                selectedDevice = selectedDevice,
                buildState = buildState,
                onDeviceSelected = {
                    selectedDevice = it
                },
                onBuildFirmware = onBuildFirmware
            )
        }
        logs.takeIf { it.isNotEmpty() }?.let { notEmptyLogs ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .weight(1.0f),
                        text = stringResource(R.string.label_logs),
                        style = MaterialTheme.typography.h6
                    )
                    if (buildState is BuildState.Started) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
            }
            items(items = notEmptyLogs) { log ->
                LogRow(buildLog = log)
                Divider()
            }
        }
    }
}

@Composable
private fun BuildFirmware(
    connectedDevices: List<Device>,
    selectedDevice: Device?,
    buildState: BuildState,
    onDeviceSelected: (Device) -> Unit,
    onBuildFirmware: (Engine, ModelType) -> Unit
) {

    Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        text = stringResource(R.string.label_deploy_impulse),
        style = MaterialTheme.typography.h6
    )
    Surface {
        Column(
            modifier = Modifier
                .padding(all = 16.dp)
        ) {
            SelectDevice(
                connectedDevices = connectedDevices,
                selectedDevice = selectedDevice,
                buildState = buildState,
                onDeviceSelected = onDeviceSelected
            )
            SelectOptimizations(
                selectedDevice = selectedDevice,
                buildState = buildState,
                onBuildFirmware = onBuildFirmware
            )
        }
    }
}

@Composable
private fun SelectDevice(
    connectedDevices: List<Device>,
    selectedDevice: Device?,
    buildState: BuildState,
    onDeviceSelected: (Device) -> Unit
) {
    var isDevicesMenuExpanded by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalContentAlpha provides if (selectedDevice != null && buildState != BuildState.Started) ContentAlpha.high else ContentAlpha.disabled) {
        Text(
            text = stringResource(R.string.label_select_device),
            style = TextStyle(fontSize = 18.sp),
        )
    }
    Spacer(modifier = Modifier.size(16.dp))
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        value = selectedDevice?.name ?: stringResource(id = R.string.empty),
        onValueChange = { },
        enabled = connectedDevices.isNotEmpty() && buildState != BuildState.Started,
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
                enabled = connectedDevices.isNotEmpty(),
                onClick = {
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
                        onDeviceSelected = {
                            isDevicesMenuExpanded = false
                            onDeviceSelected(it)
                        },
                        onDismiss = {
                            isDevicesMenuExpanded = false
                        }
                    )
                }
            }
        },
        singleLine = true
    )
}

@Composable
private fun SelectOptimizations(
    selectedDevice: Device?,
    buildState: BuildState,
    onBuildFirmware: (Engine, ModelType) -> Unit
) {
    var isEonCompilerEnabled by remember { mutableStateOf(false) }
    var selectedNNClassifier by remember { mutableStateOf(ModelType.INT_8) }

    Column {
        CompositionLocalProvider(LocalContentAlpha provides if (selectedDevice != null && buildState != BuildState.Started) ContentAlpha.high else ContentAlpha.disabled) {
            Text(
                text = stringResource(R.string.title_select_optional_optimizations),
                style = TextStyle(fontSize = 18.sp),
                textAlign = TextAlign.Start
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column {
                CompositionLocalProvider(LocalContentAlpha provides if (selectedDevice != null && buildState != BuildState.Started) ContentAlpha.high else ContentAlpha.disabled) {
                    Text(
                        text = stringResource(R.string.label_enable_eon_compiler)
                    )
                    Text(
                        text = stringResource(R.string.label_eon_compiler_summary),
                        style = MaterialTheme.typography.caption
                    )
                }
            }
            Switch(
                checked = isEonCompilerEnabled,
                modifier = Modifier
                    .weight(1.0f)
                    .padding(start = 8.dp)
                    .align(Alignment.CenterVertically),
                enabled = selectedDevice != null,
                onCheckedChange = { isEonCompilerEnabled = it }
            )
        }
        CompositionLocalProvider(LocalContentAlpha provides if (selectedDevice != null && buildState != BuildState.Started) ContentAlpha.high else ContentAlpha.disabled) {
            Text(
                text = stringResource(R.string.title_available_optimizations),
                style = TextStyle(fontSize = 18.sp)
            )
        }
        Row(
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedNNClassifier == ModelType.INT_8,
                onClick = { selectedNNClassifier = ModelType.INT_8 },
                enabled = selectedDevice != null && buildState != BuildState.Started
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = { selectedNNClassifier = ModelType.INT_8 })
                    .padding(start = 8.dp)
            ) {
                CompositionLocalProvider(LocalContentAlpha provides if (selectedDevice != null && buildState != BuildState.Started) ContentAlpha.high else ContentAlpha.disabled) {
                    Text(
                        text = AnnotatedString(text = stringResource(R.string.label_neural_classifier_quantized_int8))
                    )
                    Text(
                        text = AnnotatedString(text = stringResource(R.string.label_optimization_performance_summary)),
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedNNClassifier == ModelType.FLOAT_32,
                onClick = { selectedNNClassifier = ModelType.FLOAT_32 },
                enabled = selectedDevice != null && buildState != BuildState.Started
            )
            CompositionLocalProvider(LocalContentAlpha provides if (selectedDevice != null && buildState != BuildState.Started) ContentAlpha.high else ContentAlpha.disabled) {
                Text(
                    text = AnnotatedString(text = stringResource(R.string.label_neural_classifier_unoptimized_float32)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = selectedDevice != null,
                            onClick = { selectedNNClassifier = ModelType.FLOAT_32 })
                        .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.size(16.dp))
        Button(
            modifier = Modifier
                .wrapContentWidth()
                .align(CenterHorizontally),
            onClick = {
                onBuildFirmware(
                    if (isEonCompilerEnabled) Engine.TFLITE_EON else Engine.TFLITE,
                    selectedNNClassifier
                )
            },
            enabled = selectedDevice != null && buildState != BuildState.Started
        ) {
            Text(text = stringResource(R.string.build))
        }
    }
}

@Composable
private fun LogRow(buildLog: BuildLog) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface)
            .padding(16.dp),
    ) {
        when (buildLog) {
            is BuildLog.Data -> {
                Text(text = buildLog.data.replace("\n", ""))
            }
            is BuildLog.Finished -> {
                Text(text = buildLog.success.takeIf { it }?.let { "Success" } ?: "Failed")
            }
        }
    }
}
