package no.nordicsemi.android.ei.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collect
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.comms.BuildState
import no.nordicsemi.android.ei.comms.DeploymentManager
import no.nordicsemi.android.ei.model.BuildLog
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.showSnackbar
import no.nordicsemi.android.ei.util.Engine
import no.nordicsemi.android.ei.util.ModelType
import java.util.*

@Composable
fun Deployment(
    snackbarHostState: SnackbarHostState,
    deploymentManager: DeploymentManager,
    projectName: String,
    connectedDevices: List<Device>,
    onBuildFirmware: (Engine, ModelType) -> Unit,
    onFirmwareDownload: (ModelType, Uri) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var modelType by rememberSaveable { mutableStateOf(ModelType.INT_8) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                onFirmwareDownload(modelType, uri)
            }
        }
    }
    val logs by remember(deploymentManager.jobId) {
        derivedStateOf {
            deploymentManager.logs
        }
    }
    var buildState by remember {
        mutableStateOf<BuildState>(BuildState.Unknown)
    }
    var selectedDevice by remember {
        mutableStateOf(connectedDevices.firstOrNull())
    }
    LaunchedEffect(deploymentManager.jobId) {
        deploymentManager.buildStateAsFlow().collect { state ->
            buildState = state
            when (state) {
                is BuildState.Error -> {
                    // TODO confirm error message to be displayed
                    showSnackbar(
                        snackbarHostState = snackbarHostState,
                        coroutineScope = coroutineScope,
                        message = context.getString(R.string.error_building_firmware) + ((buildState as BuildState.Error).reason
                                ?: context.getString(R.string.error_unknown))
                    )
                }
                is BuildState.Finished -> {
                    launcher.launch(
                        Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
                            .setType("application/zip").putExtra(
                                Intent.EXTRA_TITLE,
                                "$projectName-thingy-53".replace(" ", "-").lowercase(Locale.US)
                            )
                    )
                }
                else -> {
                }
            }
        }
    }

    LazyColumn(contentPadding = PaddingValues(bottom = 56.dp)) {
        item {
            BuildFirmware(
                connectedDevices = connectedDevices,
                selectedDevice = selectedDevice,
                onDeviceSelected = {
                    selectedDevice = it
                },
                modelType = modelType,
                onModelTypeSelected = { modelType = it },
                buildState = buildState,
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
    onDeviceSelected: (Device) -> Unit,
    modelType: ModelType,
    onModelTypeSelected: (ModelType) -> Unit,
    buildState: BuildState,
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
                modelType = modelType,
                onModelTypeSelected = onModelTypeSelected,
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
    var width by rememberSaveable { mutableStateOf(0) }

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
            .onSizeChanged { width = it.width }
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
                ShowDevicesDropdown(
                    modifier = Modifier.width(with(LocalDensity.current) { width.toDp() }),
                    expanded = isDevicesMenuExpanded,
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
        },
        singleLine = true
    )
}

@Composable
private fun SelectOptimizations(
    selectedDevice: Device?,
    modelType: ModelType,
    onModelTypeSelected: (ModelType) -> Unit,
    buildState: BuildState,
    onBuildFirmware: (Engine, ModelType) -> Unit
) {
    var engine by rememberSaveable { mutableStateOf(Engine.TFLITE_EON) }
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
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.0f)) {
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
                checked = engine == Engine.TFLITE_EON,
                modifier = Modifier
                    .padding(start = 8.dp),
                enabled = selectedDevice != null,
                onCheckedChange = { checked ->
                    engine = when (checked) {
                        true -> Engine.TFLITE_EON
                        false -> Engine.TFLITE
                    }
                }
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
                selected = modelType == ModelType.INT_8,
                onClick = { onModelTypeSelected(modelType) },
                enabled = selectedDevice != null && buildState != BuildState.Started
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = { onModelTypeSelected(modelType) })
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
                selected = modelType == ModelType.FLOAT_32,
                onClick = { onModelTypeSelected(modelType) },
                enabled = selectedDevice != null && buildState != BuildState.Started
            )
            CompositionLocalProvider(LocalContentAlpha provides if (selectedDevice != null && buildState != BuildState.Started) ContentAlpha.high else ContentAlpha.disabled) {
                Text(
                    text = AnnotatedString(text = stringResource(R.string.label_neural_classifier_unoptimized_float32)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = selectedDevice != null,
                            onClick = { onModelTypeSelected(modelType) })
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
                    engine,
                    modelType
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
