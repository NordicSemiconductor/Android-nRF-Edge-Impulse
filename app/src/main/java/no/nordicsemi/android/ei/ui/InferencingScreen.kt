package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Device
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InferencingScreen(
    modifier: Modifier = Modifier,
    connectedDevices: List<Device>,
    results: List<Int>
) {
    var selectedDevice by remember {
        mutableStateOf(connectedDevices.firstOrNull())
    }
    var isDevicesMenuExpanded by remember { mutableStateOf(false) }
    var width by rememberSaveable { mutableStateOf(0) }
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = 144.dp)
    ) {
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                text = stringResource(R.string.title_inferencing),
                style = MaterialTheme.typography.h6
            )
        }
        item {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
                    .onSizeChanged { width = it.width },
                value = selectedDevice?.name ?: stringResource(id = R.string.empty),
                onValueChange = { },
                readOnly = true,
                label = {
                    Text(text = stringResource(R.string.label_device))
                },
                leadingIcon = {
                    Icon(
                        modifier = Modifier
                            .size(24.dp),
                        imageVector = Icons.Rounded.DeveloperBoard,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onSurface
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
                                selectedDevice = it
                            },
                            onDismiss = {
                                isDevicesMenuExpanded = false
                            }
                        )
                    }
                },
                singleLine = true
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(modifier = Modifier.padding(vertical = 16.dp), onClick = { /*TODO*/ }) {
                    Text(
                        text = stringResource(id = R.string.label_start_inferencing).uppercase(
                            Locale.US
                        )
                    )
                }
            }
        }

        results.takeIf { it.isNotEmpty() }?.let {
            stickyHeader {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.background)
                        .padding(16.dp)
                ) {
                    // TODO fix the columns
                    Text(
                        text = stringResource(id = R.string.label_col_sample_name),
                        modifier = Modifier.weight(0.5f),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(id = R.string.label_col_label),
                        modifier = Modifier.weight(0.5f),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(id = R.string.label_col_length),
                        modifier = Modifier.width(60.dp),
                        fontWeight = FontWeight.Bold,
                    )
                }
                Divider()
            }
        }
    }
}