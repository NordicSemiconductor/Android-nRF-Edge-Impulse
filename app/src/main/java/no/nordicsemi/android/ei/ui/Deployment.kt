package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.Launch
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.comms.DeploymentState
import no.nordicsemi.android.ei.comms.DeploymentState.*
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.ui.layouts.DeviceDisconnected
import no.nordicsemi.android.ei.ui.theme.NordicBlue
import java.util.*

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
            style = MaterialTheme.typography.h6
        )
        Surface(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
            Column(
                modifier = Modifier
                    .padding(all = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.title_create_impulse),
                    style = MaterialTheme.typography.h6
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
                        imageVector = Icons.Rounded.Launch,
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
    var width by rememberSaveable { mutableStateOf(0) }

    Column(modifier = Modifier.padding(bottom = 72.dp)) {
        Surface(elevation = 2.dp) {
            Column(
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.title_deploy_impulse),
                    style = MaterialTheme.typography.h6
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .onSizeChanged { width = it.width },
                    value = deploymentTarget?.name ?: stringResource(id = R.string.empty),
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
                            tint = MaterialTheme.colors.onSurface
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            enabled = connectedDevices.isNotEmpty() &&
                                    (deploymentState is NotStarted || deploymentState is Canceled ||
                                            deploymentState is Failed || deploymentState is Complete),
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
                connectedDevices.takeIf { it.isNotEmpty() }?.apply {
                    if (deploymentTarget == null) {
                        onDeploymentTargetSelected(connectedDevices[0])
                    }
                }
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
                        NotStarted, is Canceled, is Failed, is Complete -> MaterialTheme.colors.primary
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
    text: String,
    transferSpeed: String = "",
    contentAlpha: Float = ContentAlpha.disabled,
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
                CompositionLocalProvider(LocalContentAlpha provides contentAlpha) {
                    Text(modifier = Modifier.weight(1.0f), text = text, textAlign = TextAlign.Start)
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
            text = "Building...",
            contentAlpha = ContentAlpha.high
        ) {
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
            text = "Build failed",
            contentAlpha = ContentAlpha.high
        )
        state is Canceled && state.state is Building -> StateRow(
            icon = Icons.Outlined.Construction,
            tint = Color.Red,
            text = "Build canceled",
            contentAlpha = ContentAlpha.high
        )
        state > Building -> StateRow(
            icon = Icons.Outlined.Construction,
            tint = NordicBlue,
            text = "Built",
            contentAlpha = ContentAlpha.high
        )
        else -> StateRow(icon = Icons.Outlined.Construction, text = "Build")
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
            text = "Downloading...",
            contentAlpha = ContentAlpha.high
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
            text = "Download failed",
            contentAlpha = ContentAlpha.high
        )
        state is Canceled && state.state is Downloading -> StateRow(
            icon = Icons.Outlined.CloudDownload,
            tint = Color.Red,
            text = "Download canceled",
            contentAlpha = ContentAlpha.high
        )
        state > Downloading -> StateRow(
            icon = Icons.Outlined.CloudDownload,
            tint = NordicBlue,
            text = "Downloaded",
            contentAlpha = ContentAlpha.high
        )
        else -> StateRow(icon = Icons.Outlined.CloudDownload, text = "Download")
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
            text = "Verifying...",
            contentAlpha = ContentAlpha.high
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
            text = "Verification failed",
            contentAlpha = ContentAlpha.high
        )
        state is Canceled && state.state is Verifying -> StateRow(
            icon = Icons.Outlined.Verified,
            tint = Color.Red,
            text = "Verification canceled",
            contentAlpha = ContentAlpha.high
        )
        state > Verifying -> StateRow(
            icon = Icons.Outlined.Verified,
            tint = NordicBlue,
            text = "Verified",
            contentAlpha = ContentAlpha.high
        )
        else -> StateRow(icon = Icons.Outlined.Verified, text = "Verify")
    }
}

@Composable
fun UploadRow(
    state: DeploymentState
) {
    var transferSpeed by rememberSaveable {
        mutableStateOf(0f)
    }
    when {
        state is Uploading -> {
            transferSpeed = state.transferSpeed
            StateRow(
                icon = Icons.Outlined.Upload,
                tint = NordicBlue,
                text = "Uploading...",
                contentAlpha = ContentAlpha.high,
                transferSpeed = stringResource(R.string.label_transfer_speed, transferSpeed)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .height(height = 2.dp)
                        .fillMaxWidth(),
                    progress = state.percent / 100f
                )
            }
        }
        state is Failed && state.state is Uploading -> StateRow(
            icon = Icons.Outlined.Upload,
            tint = Color.Red,
            text = "Upload failed",
            contentAlpha = ContentAlpha.high
        )
        state is Canceled && state.state is Uploading -> StateRow(
            icon = Icons.Outlined.Upload,
            tint = Color.Red,
            text = "Upload canceled",
            contentAlpha = ContentAlpha.high
        )
        state > Uploading(transferSpeed = transferSpeed) -> StateRow(
            icon = Icons.Outlined.Upload,
            tint = NordicBlue,
            text = "Uploaded",
            contentAlpha = ContentAlpha.high,
            transferSpeed = stringResource(R.string.label_transfer_speed, transferSpeed)
        )
        else -> StateRow(icon = Icons.Outlined.Upload, text = "Upload")
    }
}

@Composable
fun ConfirmRow(
    state: DeploymentState
) {
    when {
        state is Confirming -> StateRow(
            icon = Icons.Outlined.Grading,
            tint = NordicBlue,
            text = "Confirming...",
            contentAlpha = ContentAlpha.high
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .height(height = 2.dp)
                    .fillMaxWidth()
            )
        }
        state is Failed && state.state is Confirming -> StateRow(
            icon = Icons.Outlined.Grading,
            tint = Color.Red,
            text = "Confirmation failed",
            contentAlpha = ContentAlpha.high
        )
        state is Canceled && state.state is Confirming -> StateRow(
            icon = Icons.Outlined.Grading,
            tint = Color.Red,
            text = "Confirmation canceled",
            contentAlpha = ContentAlpha.high
        )
        state > Confirming -> StateRow(
            icon = Icons.Outlined.Grading,
            tint = NordicBlue,
            text = "Confirmed",
            contentAlpha = ContentAlpha.high
        )
        else -> StateRow(icon = Icons.Outlined.Grading, text = "Confirm")
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
            text = "Applying update...",
            contentAlpha = ContentAlpha.high
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .height(height = 2.dp)
                    .fillMaxWidth()
            )
        }
        state is Failed && state.state is ApplyingUpdate -> StateRow(
            icon = Icons.Outlined.SystemUpdateAlt,
            tint = Color.Red,
            text = "Applying update failed",
            contentAlpha = ContentAlpha.high
        )
        state is Canceled && state.state is ApplyingUpdate -> StateRow(
            icon = Icons.Outlined.SystemUpdateAlt,
            tint = Color.Red,
            text = "Applying update canceled",
            contentAlpha = ContentAlpha.high
        )
        state > ApplyingUpdate -> StateRow(
            icon = Icons.Outlined.SystemUpdateAlt,
            tint = NordicBlue,
            text = "Update Applied",
            contentAlpha = ContentAlpha.high
        )
        else -> StateRow(icon = Icons.Outlined.SystemUpdateAlt, text = "Apply update")
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
            text = "Completed",
            contentAlpha = ContentAlpha.high
        )
        state is Failed && state.state is Complete -> StateRow(
            icon = Icons.Outlined.DoneAll,
            tint = Color.Red,
            text = "Failed",
            contentAlpha = ContentAlpha.high
        )
        state is Canceled && state.state is Complete -> StateRow(
            icon = Icons.Outlined.DoneAll,
            tint = Color.Red,
            text = "Canceled",
            contentAlpha = ContentAlpha.high
        )
        state > Complete -> StateRow(
            icon = Icons.Outlined.DoneAll,
            tint = NordicBlue,
            text = "Completed",
            contentAlpha = ContentAlpha.high
        )
        else -> StateRow(icon = Icons.Outlined.DoneAll, text = "Completing")
    }
}