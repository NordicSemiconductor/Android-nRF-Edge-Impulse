package no.nordicsemi.android.ei.ui

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.InferencingMessage.InferenceResults
import no.nordicsemi.android.ei.model.InferencingMessage.InferencingRequest
import no.nordicsemi.android.ei.ui.layouts.InfoDeviceDisconnectedLayout
import no.nordicsemi.android.ei.viewmodels.state.InferencingState
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InferencingScreen(
    connectedDevices: List<Device>,
    inferenceResults: List<InferenceResults>,
    inferencingTarget: Device?,
    onInferencingTargetSelected: (Device) -> Unit,
    inferencingState: InferencingState,
    sendInferencingRequest: (InferencingRequest) -> Unit
) {

    val isLargeScreen =
        LocalConfiguration.current.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 56.dp)
    ) {
        StartInferencing(
            isLargeScreen = isLargeScreen,
            isLandscape = isLandscape,
            connectedDevices = connectedDevices,
            inferencingTarget = inferencingTarget,
            onInferencingTargetSelected = onInferencingTargetSelected,
            inferencingState = inferencingState,
            sendInferencingRequest = sendInferencingRequest
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
        InferencingResults(
            isLargeScreen = isLargeScreen,
            isLandscape = isLandscape,
            inferenceResults = inferenceResults
        )
    }
}

@Composable
private fun StartInferencing(
    isLargeScreen: Boolean,
    isLandscape: Boolean,
    connectedDevices: List<Device>,
    inferencingTarget: Device?,
    onInferencingTargetSelected: (Device) -> Unit,
    inferencingState: InferencingState,
    sendInferencingRequest: (InferencingRequest) -> Unit
) {
    var isDevicesMenuExpanded by remember { mutableStateOf(false) }
    var width by rememberSaveable { mutableStateOf(0) }

    if (isLargeScreen || isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .wrapContentWidth(),
                text = stringResource(R.string.title_inferencing),
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.size(16.dp))
            OutlinedTextField(
                modifier = Modifier
                    .weight(0.5f)
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
            Spacer(modifier = Modifier.size(16.dp))
            Button(
                modifier = Modifier.defaultMinSize(minWidth = 100.dp),
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
                            InferencingState.Started -> R.string.action_stop
                            InferencingState.Stopped -> R.string.action_start
                        }
                    ).uppercase(
                        Locale.US
                    )
                )
            }
        }
    } else {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            text = stringResource(R.string.title_inferencing),
            style = MaterialTheme.typography.h6
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
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
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                modifier = Modifier
                    .width(width = 100.dp)
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
                            InferencingState.Started -> R.string.action_stop
                            InferencingState.Stopped -> R.string.action_start
                        }
                    ).uppercase(
                        Locale.US
                    )
                )
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InferencingResults(
    isLargeScreen: Boolean,
    isLandscape: Boolean,
    inferenceResults: List<InferenceResults>
) {
    val listState = rememberLazyListState()
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val scrollState = rememberScrollState()
    inferenceResults.takeIf { it.isNotEmpty() }?.let { results ->
        val cellCount = results.first().classification.size
        val cellWidth = calculateWith(cellCount, screenWidth, isLargeScreen, isLandscape)
        Log.d("AAAA", "Cell width: $cellWidth")
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(state = scrollState),
            state = listState
        ) {
            stickyHeader {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.background)
                ) {
                    results.firstOrNull()?.let { result ->
                        result.classification.forEach {
                            Text(
                                text = it.label,
                                modifier = Modifier
                                    .width(width = cellWidth)
                                    .padding(all = 16.dp),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = "anomaly",
                            modifier = Modifier
                                .width(width = cellWidth)
                                .padding(all = 16.dp),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Divider()
            }
            items(items = results) { results ->
                TableRow(inferenceResults = results, cellWidth = cellWidth)
            }
        }
    }
}

@Composable
private fun TableRow(inferenceResults: InferenceResults, cellWidth: Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface)
    ) {
        inferenceResults.classification.forEach { classification ->
            Text(
                text = BigDecimal(classification.value).setScale(
                    4,
                    RoundingMode.HALF_EVEN
                ).toString(),
                modifier = Modifier
                    .width(cellWidth)
                    .padding(16.dp),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = classification.value.color()
            )
        }
        Text(
            text = BigDecimal(inferenceResults.anomaly).setScale(
                4,
                RoundingMode.HALF_EVEN
            ).toString(),
            modifier = Modifier
                .width(cellWidth)
                .padding(16.dp),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = inferenceResults.anomaly.color()
        )
    }
    Divider()
}

private fun calculateWith(
    cellCount:Int,
    screenWidth: Int,
    isLargeScreen: Boolean,
    isLandscape: Boolean,
): Dp = if (isLandscape) {
    if (isLargeScreen) {
        if (cellCount == 5) {
            (screenWidth / cellCount).dp
        } else {
            MAX_CELL_WIDTH
        }
    } else {
        MIN_CELL_WIDTH
    }
} else {
    MIN_CELL_WIDTH
}

@Composable
private fun Double.color(): Color {
    return if (this < 0.6) {
        MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
    } else MaterialTheme.colors.onSurface
}

private val MIN_CELL_WIDTH = 100.dp
private val MAX_CELL_WIDTH = 140.dp