/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Grading
import androidx.compose.material.icons.automirrored.rounded.Launch
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.SystemUpdateAlt
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.comms.DeploymentState
import no.nordicsemi.android.ei.comms.DeploymentState.ApplyingUpdate
import no.nordicsemi.android.ei.comms.DeploymentState.Building
import no.nordicsemi.android.ei.comms.DeploymentState.Canceled
import no.nordicsemi.android.ei.comms.DeploymentState.Complete
import no.nordicsemi.android.ei.comms.DeploymentState.Confirming
import no.nordicsemi.android.ei.comms.DeploymentState.Downloading
import no.nordicsemi.android.ei.comms.DeploymentState.Failed
import no.nordicsemi.android.ei.comms.DeploymentState.NotStarted
import no.nordicsemi.android.ei.comms.DeploymentState.Uploading
import no.nordicsemi.android.ei.comms.DeploymentState.Verifying
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.ui.layouts.DeviceDisconnected
import no.nordicsemi.android.ei.ui.theme.NordicBlue
import java.util.Locale

@Composable
fun Deployment(
    modifier: Modifier,
    project: Project,
    connectedDevices: List<Device>,
    deploymentTarget: Device?,
    onDeploymentTargetSelected: (Device) -> Unit,
    deploymentState: DeploymentState,
    onDeployClick: (Device?) -> Unit,
    onCancelDeployClick: () -> Unit
) {
    Column(modifier = modifier.verticalScroll(state = rememberScrollState())) {
        DesignImpulse(project = project)
        DeployImpulse(
            connectedDevices = connectedDevices,
            deploymentTarget = deploymentTarget,
            onDeploymentTargetSelected = onDeploymentTargetSelected,
            deploymentState = deploymentState,
            onDeployClick = onDeployClick,
            onCancelDeployClick = onCancelDeployClick
        )
    }
}

@Composable
private fun DesignImpulse(
    project: Project
) {
    val localUriHandler = LocalUriHandler.current
    val uri by remember { mutableStateOf("https://studio.edgeimpulse.com/studio/${project.id}/create-impulse") }
    Column {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            text = stringResource(R.string.title_design_impulse),
            style = MaterialTheme.typography.titleLarge
        )
        Surface(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(all = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.title_create_impulse),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.size(size = 16.dp))
                Text(
                    text = stringResource(R.string.label_create_impulse_rationale)
                )

            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    localUriHandler.openUri(uri = uri)
                }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = R.string.action_ei_studio).uppercase(
                            Locale.US
                        )
                    )
                    Icon(
                        modifier = Modifier.padding(start = 8.dp),
                        imageVector = Icons.AutoMirrored.Rounded.Launch,
                        contentDescription = null
                    )
                }
            }
        }

    }
}

@Composable
private fun DeployImpulse(
    connectedDevices: List<Device>,
    deploymentTarget: Device?,
    onDeploymentTargetSelected: (Device) -> Unit,
    deploymentState: DeploymentState,
    onDeployClick: (Device?) -> Unit,
    onCancelDeployClick: () -> Unit
) {
    var isDevicesMenuExpanded by remember { mutableStateOf(false) }
    var width by rememberSaveable { mutableIntStateOf(0) }

    connectedDevices.takeIf { it.isNotEmpty() }?.apply {
        if (deploymentTarget == null) {
            onDeploymentTargetSelected(connectedDevices[0])
        }
    }
    Column(modifier = Modifier.padding(bottom = 72.dp)) {
        Surface {
            Column(
                modifier = Modifier.padding(all = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.title_deploy_impulse),
                    style = MaterialTheme.typography.titleLarge
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .onSizeChanged { width = it.width },
                    value = deploymentTarget?.name ?: stringResource(id = R.string.empty),
                    enabled = shouldEnable(
                        connectedDevices = connectedDevices,
                        deploymentState = deploymentState
                    ),
                    onValueChange = { },
                    readOnly = true,
                    label = {
                        DeviceDisconnected(connectedDevices = connectedDevices)
                    },
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
                        IconButton(
                            enabled = shouldEnable(
                                connectedDevices = connectedDevices,
                                deploymentState = deploymentState
                            ),
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
                                    onDeploymentTargetSelected(it)
                                },
                                onDismiss = {
                                    isDevicesMenuExpanded = false
                                }
                            )
                        }
                    },
                    singleLine = true
                )
                BuildRow(state = deploymentState)
                DownloadRow(state = deploymentState)
                VerifyRow(state = deploymentState)
                UploadRow(state = deploymentState)
                ConfirmRow(state = deploymentState)
                ApplyingUpdateRow(state = deploymentState)
                CompletedRow(state = deploymentState)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                enabled = connectedDevices.isNotEmpty(),
                onClick = {
                    when (deploymentState) {
                        NotStarted, is Canceled, is Failed, is Complete -> onDeployClick(
                            deploymentTarget
                        )

                        else -> onCancelDeployClick()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    when (deploymentState) {
                        NotStarted, is Canceled, is Failed, is Complete -> MaterialTheme.colorScheme.primary
                        else -> Color.Red
                    }
                )
            ) {
                Text(
                    modifier = Modifier.defaultMinSize(minWidth = 80.dp),
                    text = stringResource(
                        when (deploymentState) {
                            NotStarted, is Canceled, is Failed, is Complete -> R.string.action_deploy
                            else -> R.string.action_cancel
                        }
                    ).uppercase(
                        Locale.US
                    ),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun StateRow(
    icon: ImageVector = Icons.Filled.Check,
    tint: Color = Color.Gray,
    mainText: String,
    transferSpeed: String = "",
    contentAlpha: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    content: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically

    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint
        )
        Column(
            modifier = Modifier
                .weight(1.0f)
                .padding(start = 16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                CompositionLocalProvider(LocalContentColor provides contentAlpha) {
                    Text(
                        modifier = Modifier.weight(1.0f),
                        text = mainText,
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = transferSpeed,
                        textAlign = TextAlign.End
                    )
                }
            }
            content()
        }
    }
}

@Composable
fun BuildRow(
    state: DeploymentState
) {
    when {
        state is Building -> StateRow(
            icon = Icons.Outlined.Construction,
            tint = NordicBlue,
            mainText = "Building...",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        ) {
            Text(
                text = "This may take few minutes.",
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal
            )
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .height(height = 2.dp)
                    .fillMaxWidth()
            )
        }

        state is Failed && state.state is Building -> StateRow(
            icon = Icons.Outlined.Construction,
            tint = Color.Red,
            mainText = "Build failed",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        state is Canceled && state.state is Building -> StateRow(
            icon = Icons.Outlined.Construction,
            tint = Color.Red,
            mainText = "Build canceled",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        state > Building -> StateRow(
            icon = Icons.Outlined.Construction,
            tint = NordicBlue,
            mainText = "Built",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        else -> StateRow(icon = Icons.Outlined.Construction, mainText = "Build")
    }
}

@Composable
fun DownloadRow(
    state: DeploymentState
) {
    when {
        state is Downloading -> StateRow(
            icon = Icons.Outlined.CloudDownload,
            tint = NordicBlue,
            mainText = "Downloading...",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .height(height = 2.dp)
                    .fillMaxWidth()
            )
        }

        state is Failed && state.state is Downloading -> StateRow(
            icon = Icons.Outlined.CloudDownload,
            tint = Color.Red,
            mainText = "Download failed",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        state is Canceled && state.state is Downloading -> StateRow(
            icon = Icons.Outlined.CloudDownload,
            tint = Color.Red,
            mainText = "Download canceled",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        state > Downloading -> StateRow(
            icon = Icons.Outlined.CloudDownload,
            tint = NordicBlue,
            mainText = "Downloaded",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        else -> StateRow(icon = Icons.Outlined.CloudDownload, mainText = "Download")
    }
}

@Composable
fun VerifyRow(
    state: DeploymentState
) {
    when {
        state is Verifying -> StateRow(
            icon = Icons.Outlined.Verified,
            tint = NordicBlue,
            mainText = "Verifying...",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .height(height = 2.dp)
                    .fillMaxWidth()
            )
        }

        state is Failed && state.state is Verifying -> StateRow(
            icon = Icons.Outlined.Verified,
            tint = Color.Red,
            mainText = "Verification failed",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        state is Canceled && state.state is Verifying -> StateRow(
            icon = Icons.Outlined.Verified,
            tint = Color.Red,
            mainText = "Verification canceled",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        state > Verifying -> StateRow(
            icon = Icons.Outlined.Verified,
            tint = NordicBlue,
            mainText = "Verified",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        else -> StateRow(icon = Icons.Outlined.Verified, mainText = "Verify")
    }
}

@Composable
fun UploadRow(
    state: DeploymentState
) {
    var transferSpeed by rememberSaveable { mutableFloatStateOf(0f) }
    when {
        state is Uploading -> {
            transferSpeed = state.transferSpeed
            StateRow(
                icon = Icons.Outlined.Upload,
                tint = NordicBlue,
                mainText = "Uploading...",
                transferSpeed = stringResource(R.string.label_transfer_speed, transferSpeed),
                contentAlpha = MaterialTheme.colorScheme.onSurface
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .height(height = 2.dp)
                        .fillMaxWidth(),
                    progress = { state.percent / 100f }
                )
            }
        }

        state is Failed && state.state is Uploading -> StateRow(
            icon = Icons.Outlined.Upload,
            tint = Color.Red,
            mainText = "Upload failed",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        state is Canceled && state.state is Uploading -> StateRow(
            icon = Icons.Outlined.Upload,
            tint = Color.Red,
            mainText = "Upload canceled",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        state > Uploading(transferSpeed = transferSpeed) -> StateRow(
            icon = Icons.Outlined.Upload,
            tint = NordicBlue,
            mainText = "Uploaded",
            transferSpeed = stringResource(R.string.label_transfer_speed, transferSpeed),
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        else -> StateRow(icon = Icons.Outlined.Upload, mainText = "Upload")
    }
}

@Composable
fun ConfirmRow(
    state: DeploymentState
) {
    when {
        state is Confirming -> StateRow(
            icon = Icons.AutoMirrored.Outlined.Grading,
            tint = NordicBlue,
            mainText = "Confirming...",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .height(height = 2.dp)
                    .fillMaxWidth()
            )
        }

        state is Failed && state.state is Confirming -> StateRow(
            icon = Icons.AutoMirrored.Outlined.Grading,
            tint = Color.Red,
            mainText = "Confirmation failed",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        state is Canceled && state.state is Confirming -> StateRow(
            icon = Icons.AutoMirrored.Outlined.Grading,
            tint = Color.Red,
            mainText = "Confirmation canceled",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        state > Confirming -> StateRow(
            icon = Icons.AutoMirrored.Outlined.Grading,
            tint = NordicBlue,
            mainText = "Confirmed",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        else -> StateRow(icon = Icons.AutoMirrored.Outlined.Grading, mainText = "Confirm")
    }
}

@Composable
fun ApplyingUpdateRow(
    state: DeploymentState
) {
    when {
        state is ApplyingUpdate -> StateRow(
            icon = Icons.Outlined.SystemUpdateAlt,
            tint = NordicBlue,
            mainText = "Applying update...",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .height(height = 2.dp)
                    .fillMaxWidth(),
                progress = {
                    state.percent / 100f
                }
            )
        }

        state is Failed && state.state is ApplyingUpdate -> StateRow(
            icon = Icons.Outlined.SystemUpdateAlt,
            tint = Color.Red,
            mainText = "Applying update failed",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        state is Canceled && state.state is ApplyingUpdate -> StateRow(
            icon = Icons.Outlined.SystemUpdateAlt,
            tint = Color.Red,
            mainText = "Applying update canceled",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        state > ApplyingUpdate() -> StateRow(
            icon = Icons.Outlined.SystemUpdateAlt,
            tint = NordicBlue,
            mainText = "Update Applied",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        else -> StateRow(icon = Icons.Outlined.SystemUpdateAlt, mainText = "Apply update")
    }
}

@Composable
fun CompletedRow(
    state: DeploymentState
) {
    when {
        state is Complete -> StateRow(
            icon = Icons.Outlined.DoneAll,
            tint = NordicBlue,
            mainText = "Completed",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        state is Failed && state.state is Complete -> StateRow(
            icon = Icons.Outlined.DoneAll,
            tint = Color.Red,
            mainText = "Failed",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        state is Canceled && state.state is Complete -> StateRow(
            icon = Icons.Outlined.DoneAll,
            tint = Color.Red,
            mainText = "Canceled",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        state > Complete -> StateRow(
            icon = Icons.Outlined.DoneAll,
            tint = NordicBlue,
            mainText = "Completed",
            contentAlpha = MaterialTheme.colorScheme.onSurface
        )

        else -> StateRow(icon = Icons.Outlined.DoneAll, mainText = "Complete")
    }
}

@Composable
private fun shouldEnable(
    connectedDevices: List<Device>,
    deploymentState: DeploymentState
): Boolean =
    connectedDevices.isNotEmpty() &&
            (deploymentState is NotStarted || deploymentState is Canceled ||
                    deploymentState is Failed || deploymentState is Complete)