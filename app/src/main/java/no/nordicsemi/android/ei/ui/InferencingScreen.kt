/*
 * Copyright (c) 2022, Nordic Semiconductor
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package no.nordicsemi.android.ei.ui

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
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
import no.nordicsemi.android.ei.ui.layouts.DeviceDisconnected
import no.nordicsemi.android.ei.util.round
import no.nordicsemi.android.ei.viewmodels.state.InferencingState

@Composable
fun InferencingScreen(
    modifier: Modifier,
    connectedDevices: List<Device>,
    inferenceResults: List<InferenceResults>,
    inferencingTarget: Device?,
    onInferencingTargetSelected: (Device) -> Unit,
    inferencingState: InferencingState,
    sendInferencingRequest: (InferencingRequest) -> Unit
) {

    val isLargeScreen =
        LocalConfiguration.current.screenLayout and
                Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Column(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.background)
            .padding(bottom = 100.dp),
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
        connectedDevices.takeIf { it.isNotEmpty() }?.apply {
            if (inferencingTarget == null) {
                onInferencingTargetSelected(connectedDevices.first())
            }
        }
        InferencingTable(
            isLargeScreen = isLargeScreen,
            isLandscape = isLandscape,
            inferenceResults = inferenceResults
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.size(16.dp))
            ExposedDropdownMenuBox(
                expanded = isDevicesMenuExpanded,
                onExpandedChange = { isDevicesMenuExpanded = it }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .weight(0.5f)
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryNotEditable,
                            enabled = connectedDevices.isNotEmpty()
                        ),
                    value = inferencingTarget?.name ?: stringResource(id = R.string.empty),
                    enabled = connectedDevices.isNotEmpty(),
                    onValueChange = { },
                    readOnly = true,
                    label = { DeviceDisconnected(connectedDevices = connectedDevices) },
                    leadingIcon = {
                        Icon(
                            modifier = Modifier
                                .size(24.dp),
                            imageVector = Icons.Rounded.DeveloperBoard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.rotate(if (isDevicesMenuExpanded) 180f else 0f),
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    },
                    singleLine = true
                )
                ShowDevicesDropdown(
                    modifier = Modifier.exposedDropdownSize(),
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
            style = MaterialTheme.typography.titleLarge
        )
        ExposedDropdownMenuBox(
            modifier = Modifier.padding(horizontal = 16.dp),
            expanded = isDevicesMenuExpanded,
            onExpandedChange = { isDevicesMenuExpanded = it }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(
                        type = MenuAnchorType.PrimaryNotEditable,
                        enabled = connectedDevices.isNotEmpty()
                    ),
                value = inferencingTarget?.name ?: stringResource(id = R.string.empty),
                enabled = connectedDevices.isNotEmpty(),
                onValueChange = { },
                readOnly = true,
                label = { DeviceDisconnected(connectedDevices = connectedDevices) },
                leadingIcon = {
                    Icon(
                        modifier = Modifier
                            .size(24.dp),
                        imageVector = Icons.Rounded.DeveloperBoard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                trailingIcon = {
                    Icon(
                        modifier = Modifier.rotate(if (isDevicesMenuExpanded) 180f else 0f),
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                },
                singleLine = true
            )
            ShowDevicesDropdown(
                modifier = Modifier.exposedDropdownSize(),
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
                },
                colors = ButtonDefaults.buttonColors(
                    if (inferencingState is InferencingState.Started) Color.Red
                    else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(
                        id = when (inferencingState) {
                            InferencingState.Started -> R.string.action_stop
                            InferencingState.Stopped -> R.string.action_start
                        }
                    ),
                    color = Color.White
                )
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InferencingTable(
    isLargeScreen: Boolean,
    isLandscape: Boolean,
    inferenceResults: List<InferenceResults>
) {
    val listState = rememberLazyListState()
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val scrollState = rememberScrollState()
    LaunchedEffect(key1 = inferenceResults.size) {
        if (inferenceResults.isNotEmpty()) {
            listState.scrollToItem(inferenceResults.size - 1)
        }
    }
    inferenceResults.takeIf { it.isNotEmpty() }?.let { results ->
        val cellCount = results.first().classification.size + 1
        val cellWidth = calculateWith(cellCount, screenWidth, isLargeScreen, isLandscape)
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
                        .background(color = MaterialTheme.colorScheme.background)
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
                            text = stringResource(R.string.label_anomaly),
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
                HorizontalDivider()
            }
            items(items = results) { results ->
                InferencingResult(inferenceResults = results, cellWidth = cellWidth)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun InferencingResult(inferenceResults: InferenceResults, cellWidth: Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
    ) {
        inferenceResults.classification.forEach { classification ->
            Text(
                text = classification.value.round().toString(),
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

        inferenceResults.anomaly.let { anomaly ->
            when(anomaly){
                null -> Text(
                    text = "N/A",
                    modifier = Modifier
                        .width(cellWidth)
                        .padding(16.dp),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                else -> Text(
                    text = anomaly.round().toString(),
                    modifier = Modifier
                        .width(cellWidth)
                        .padding(16.dp),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = anomaly.color()
                )
            }
        }
    }
}

@Composable
private fun Double.color(): Color {
    return if (this > 0.6) {
        Color.Green.copy(red = 0.14f, green = 0.86f, blue = 0f)
    } else MaterialTheme.colorScheme.onSurface.copy(0.6f)
}

private fun calculateWith(
    cellCount: Int,
    screenWidth: Int,
    isLargeScreen: Boolean,
    isLandscape: Boolean,
): Dp = if (isLandscape) {
    if (cellCount <= 5) (screenWidth / cellCount).dp
    else MAX_CELL_WIDTH
} else {
    if (cellCount < 5) (screenWidth / cellCount).dp
    else {
        if (isLargeScreen) (screenWidth / cellCount).dp
        else MIN_CELL_WIDTH
    }
}

private val MIN_CELL_WIDTH = 98.dp
private val MAX_CELL_WIDTH = 140.dp