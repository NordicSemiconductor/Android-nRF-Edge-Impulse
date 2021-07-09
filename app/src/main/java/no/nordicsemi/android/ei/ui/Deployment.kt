package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Device

@Composable
fun Deployment(modifier: Modifier, connectedDevices: List<Device>) {
    var selectedDevice = connectedDevices.firstOrNull()
    var isDevicesMenuExpanded by remember { mutableStateOf(false) }
    var isEonCompilerEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                            onDeviceSelected = { device ->
                                isDevicesMenuExpanded = false
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Text(text = stringResource(R.string.label_enable_eon_compiler))
                Text(
                    text = stringResource(R.string.label_eon_compiler_summary),
                    style = MaterialTheme.typography.caption
                )
            }
            Switch(
                checked = false,
                modifier = Modifier
                    .weight(1.0f)
                    .padding(start = 8.dp)
                    .align(Alignment.CenterVertically),
                enabled = selectedDevice != null,
                onCheckedChange = {
                    isEonCompilerEnabled = it
                }
            )
        }

        Button(onClick = { /*TODO*/ }) {
            Text(text = stringResource(R.string.build))
        }
    }
}