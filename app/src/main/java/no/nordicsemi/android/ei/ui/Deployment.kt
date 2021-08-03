package no.nordicsemi.android.ei.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import no.nordicsemi.android.ei.model.Device

@Composable
fun Deployment(connectedDevices: List<Device>) {
    val context = LocalContext.current
    var selectedDevice by remember {
        mutableStateOf(connectedDevices.firstOrNull())
    }

    LazyColumn {
        item {
            BuildFirmware(
                context = context,
                connectedDevices = connectedDevices,
                selectedDevice = selectedDevice,
                onDeviceSelected = {
                    selectedDevice = it
                }
            )
        }

        item {
            BuildLogs(selectedDevice = selectedDevice)
        }
    }
}

@Composable
private fun BuildFirmware(
    context: Context,
    connectedDevices: List<Device>,
    selectedDevice: Device?,
    onDeviceSelected: (Device) -> Unit
) {

    CompositionLocalProvider(LocalContentAlpha provides if (selectedDevice == null) ContentAlpha.disabled else ContentAlpha.high) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            text = stringResource(R.string.label_deploy_impulse),
            style = MaterialTheme.typography.h6
        )
    }
    Surface {
        Column(modifier = Modifier.padding(all = 16.dp)) {
            SelectDevice(
                connectedDevices = connectedDevices,
                selectedDevice = selectedDevice,
                onDeviceSelected = onDeviceSelected
            )
            SelectOptimizations(context = context, selectedDevice = selectedDevice)
        }
    }
}

@Composable
private fun SelectDevice(
    connectedDevices: List<Device>,
    selectedDevice: Device?,
    onDeviceSelected: (Device) -> Unit
) {
    var isDevicesMenuExpanded by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalContentAlpha provides if (selectedDevice == null) ContentAlpha.disabled else ContentAlpha.high) {
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
        enabled = connectedDevices.isNotEmpty(),
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
    context: Context,
    selectedDevice: Device?
) {
    var isEonCompilerEnabled by remember { mutableStateOf(false) }
    var selectedNNClassifier by remember { mutableStateOf("Quantized (int8)") }

    Column {
        CompositionLocalProvider(LocalContentAlpha provides if (selectedDevice == null) ContentAlpha.disabled else ContentAlpha.high) {
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
                CompositionLocalProvider(LocalContentAlpha provides if (selectedDevice == null) ContentAlpha.disabled else ContentAlpha.high) {
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
        Text(
            modifier = Modifier
                .alpha(if (selectedDevice == null) ContentAlpha.disabled else ContentAlpha.high),
            text = stringResource(R.string.title_available_optimizations),
            style = TextStyle(fontSize = 18.sp)
        )
        Row(
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedNNClassifier == stringResource(id = R.string.label_neural_classifier_quantized_int8),
                onClick = {
                    selectedNNClassifier =
                        context.getString(R.string.label_neural_classifier_quantized_int8)
                },
                enabled = selectedDevice != null
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = {
                            selectedNNClassifier =
                                context.getString(R.string.label_neural_classifier_quantized_int8)
                        })
                    .padding(start = 8.dp)
            ) {
                CompositionLocalProvider(LocalContentAlpha provides if (selectedDevice == null) ContentAlpha.disabled else ContentAlpha.high) {
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
                selected = selectedNNClassifier == stringResource(id = R.string.label_neural_classifier_unoptimized_float32),
                onClick = {
                    selectedNNClassifier =
                        context.getString(R.string.label_neural_classifier_unoptimized_float32)
                },
                enabled = selectedDevice != null
            )
            CompositionLocalProvider(LocalContentAlpha provides if (selectedDevice == null) ContentAlpha.disabled else ContentAlpha.high) {
                Text(
                    text = AnnotatedString(text = stringResource(R.string.label_neural_classifier_unoptimized_float32)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = selectedDevice != null,
                            onClick = {
                                selectedNNClassifier =
                                    context.getString(R.string.label_neural_classifier_unoptimized_float32)
                            })
                        .padding(start = 8.dp, top = 8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.size(16.dp))
        Button(
            modifier = Modifier
                .wrapContentWidth()
                .align(CenterHorizontally),
            onClick = { /*TODO*/ },
            enabled = selectedDevice != null
        ) {
            Text(text = stringResource(R.string.build))
        }
    }
}

@Composable
private fun BuildLogs(
    selectedDevice: Device?
) {
    CompositionLocalProvider(LocalContentAlpha provides if (selectedDevice == null) ContentAlpha.disabled else ContentAlpha.high) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            text = stringResource(R.string.label_logs),
            style = MaterialTheme.typography.h6
        )
    }
}