package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.InferencingMessage.InferencingRequest
import no.nordicsemi.android.ei.model.InferencingMessage.InferencingResults
import no.nordicsemi.android.ei.ui.layouts.InfoDeviceDisconnectedLayout
import no.nordicsemi.android.ei.viewmodels.state.InferencingState
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InferencingScreen(
    connectedDevices: List<Device>,
    inferenceResults: List<InferencingResults>,
    inferencingTarget: Device?,
    onInferencingTargetSelected: (Device) -> Unit,
    inferencingState: InferencingState,
    sendInferencingRequest: (InferencingRequest) -> Unit
) {

    val scrollState = rememberScrollState()
    var isDevicesMenuExpanded by remember { mutableStateOf(false) }
    var width by rememberSaveable { mutableStateOf(0) }

    Column(modifier = Modifier.padding(bottom = 56.dp)) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            text = stringResource(R.string.title_inferencing),
            style = MaterialTheme.typography.h6
        )
        connectedDevices.takeIf { it.isEmpty() }?.let {
            InfoDeviceDisconnectedLayout(
                text = stringResource(R.string.connect_device_for_inferencing),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } ?: run {
            if (inferencingTarget == null) {
                onInferencingTargetSelected(connectedDevices.first())
            }
        }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                .onSizeChanged { width = it.width },
            value = inferencingTarget?.name ?: stringResource(id = R.string.empty),
            enabled = connectedDevices.isNotEmpty(),
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
                    enabled = connectedDevices.isNotEmpty() && inferencingState is InferencingState.Stopped,
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
                            isDevicesMenuExpanded = false
                            onInferencingTargetSelected(device)
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
            Button(
                modifier = Modifier
                    .width(width = 200.dp)
                    .padding(vertical = 16.dp),
                enabled = connectedDevices.isNotEmpty(),
                onClick = {
                    sendInferencingRequest(
                        when (inferencingState) {
                            InferencingState.Started -> InferencingRequest.Stop()
                            InferencingState.Stopped -> InferencingRequest.Start()
                        }
                    )
                }) {
                Text(
                    text = stringResource(
                        id = when (inferencingState) {
                            InferencingState.Started -> R.string.action_stop_inferencing
                            InferencingState.Stopped -> R.string.action_start_inferencing
                        }
                    ).uppercase(
                        Locale.US
                    )
                )
            }
        }
        inferenceResults.let { inferenceResult ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(state = scrollState)
            ) {
                if (inferenceResult.isNotEmpty())
                    stickyHeader {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colors.background)
                        ) {
                            inferenceResult.first().let { result ->
                                result.classification.forEach {
                                    Text(
                                        text = it.label,
                                        modifier = Modifier
                                            .width(100.dp)
                                            .padding(16.dp),
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Text(
                                    text = "anomaly",
                                    modifier = Modifier
                                        .width(100.dp)
                                        .padding(16.dp),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Divider()
                    }

                items(items = inferenceResult) { results ->
                    TableRow(inferencingResults = results)
                }
            }
        }
    }
}

@Composable
private fun TableRow(inferencingResults: InferencingResults) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface)
    ) {
        inferencingResults.classification.forEach { classification ->
            Text(
                text = BigDecimal(classification.value).setScale(
                    4,
                    RoundingMode.HALF_EVEN
                ).toString(),
                modifier = Modifier
                    .width(100.dp)
                    .padding(16.dp),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = classification.value.color()
            )
        }
        Text(
            text = BigDecimal(inferencingResults.anomaly).setScale(
                4,
                RoundingMode.HALF_EVEN
            ).toString(),
            modifier = Modifier
                .width(100.dp)
                .padding(16.dp),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = inferencingResults.anomaly.color()
        )
    }
    Divider()
}

@Composable
private fun Double.color(): Color {
    return if (this < 0.6) {
        MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
    } else MaterialTheme.colors.onSurface
}