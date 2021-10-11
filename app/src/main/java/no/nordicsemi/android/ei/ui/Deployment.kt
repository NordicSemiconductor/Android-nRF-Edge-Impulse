package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.ui.platform.LocalContext
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
import no.nordicsemi.android.ei.ui.layouts.InfoDeviceDisconnectedLayout
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
    progress: Int,
    transferSpeed: Float,
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
            progress = progress,
            transferSpeed = transferSpeed,
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
        Surface(elevation = 2.dp) {
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
    progress: Int,
    transferSpeed: Float,
    onCancelDeployClick: () -> Unit
) {
    val context = LocalContext.current
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
                connectedDevices.takeIf { it.isEmpty() }?.apply {
                    if (deploymentState is Unknown || deploymentState is Cancelled || deploymentState is Completed || deploymentState is Failed)
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
                            InfoDeviceDisconnectedLayout(text = context.getString(R.string.connect_device_for_data_acquisition))
                            Spacer(modifier = Modifier.height(height = 16.dp))
                        }
                } ?: run {
                    if (deploymentTarget == null) {
                        onDeploymentTargetSelected(connectedDevices[0])
                    }
                }
                RowDeploymentState(
                    imageVector = when (deploymentState) {
                        is Building.Error -> Icons.Rounded.Close
                        else -> Icons.Rounded.Check
                    },
                    color = when (deploymentState) {
                        is Building.Started, is Building.Finished, is Downloading.Started, is Downloading.Finished,
                        is Verifying, is Uploading, is Confirming, is ApplyingUpdate, is Completed -> MaterialTheme.colors.primary
                        is Building.Error -> Color.Red
                        else -> Color.Gray
                    },
                    contentAlpha = when (deploymentState) {
                        is Building.Started, is Building.Finished, is Downloading, is Verifying,
                        is Uploading, is Confirming, is ApplyingUpdate, is Completed -> ContentAlpha.high
                        else -> ContentAlpha.disabled
                    },
                    text = stringResource(id = R.string.label_building)
                ) {
                    if (deploymentState is Building.Started) LinearProgressIndicator(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .height(height = 2.dp)
                            .fillMaxWidth()
                    )
                }
                RowDeploymentState(
                    imageVector = when (deploymentState) {
                        is Downloading.Error -> Icons.Rounded.Close
                        else -> Icons.Rounded.Check
                    },
                    color = when (deploymentState) {
                        is Downloading.Started, is Downloading.Finished, is Verifying, is Uploading, is Confirming, is ApplyingUpdate, is Completed -> MaterialTheme.colors.primary
                        is Downloading.Error -> Color.Red
                        else -> Color.Gray
                    },
                    contentAlpha = when (deploymentState) {
                        is Downloading, is Verifying, is Uploading, is Confirming, is ApplyingUpdate, is Completed -> ContentAlpha.high
                        else -> ContentAlpha.disabled
                    },
                    text = stringResource(id = R.string.label_downloading)
                ) {
                    if (deploymentState is Downloading.Started) LinearProgressIndicator(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .height(height = 2.dp)
                            .fillMaxWidth()
                    )
                }
                RowDeploymentState(
                    imageVector = when (deploymentState) {
                        is Cancelled, Failed -> Icons.Rounded.Close
                        else -> Icons.Rounded.Check
                    },
                    color = when (deploymentState) {
                        is Verifying, is Uploading, is Confirming, is ApplyingUpdate, is Completed -> MaterialTheme.colors.primary
                        is Cancelled, Failed -> Color.Red
                        else -> Color.Gray
                    },
                    contentAlpha = when (deploymentState) {
                        is Verifying, is Uploading, is Confirming, is ApplyingUpdate, is Completed -> ContentAlpha.high
                        else -> ContentAlpha.disabled
                    },
                    text = stringResource(id = R.string.label_verifying)
                ) {
                    if (deploymentState is Verifying) LinearProgressIndicator(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .height(height = 2.dp)
                            .fillMaxWidth()
                    )
                }
                RowDeploymentState(
                    imageVector = when (deploymentState) {
                        is Cancelled, Failed -> Icons.Rounded.Close
                        else -> Icons.Rounded.Check
                    },
                    color = when (deploymentState) {
                        is Uploading, is Confirming, is ApplyingUpdate, is Completed -> MaterialTheme.colors.primary
                        is Cancelled, Failed -> Color.Red
                        else -> Color.Gray
                    },
                    contentAlpha = when (deploymentState) {
                        is Uploading, is Confirming, is ApplyingUpdate, is Completed -> ContentAlpha.high
                        else -> ContentAlpha.disabled
                    },
                    text = stringResource(id = R.string.label_uploading),
                    progressText = stringResource(
                        id = R.string.label_transfer_speed,
                        "%.2f".format(transferSpeed)
                    )
                ) {
                    if (deploymentState is Uploading) LinearProgressIndicator(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .height(height = 2.dp)
                            .fillMaxWidth(),
                        progress = progress / 100f
                    )
                }
                RowDeploymentState(
                    imageVector = when (deploymentState) {
                        is Cancelled, Failed -> Icons.Rounded.Close
                        else -> Icons.Rounded.Check
                    },
                    color = when (deploymentState) {
                        is Confirming, is ApplyingUpdate, is Completed -> MaterialTheme.colors.primary
                        is Cancelled, Failed -> Color.Red
                        else -> Color.Gray
                    },
                    contentAlpha = when (deploymentState) {
                        is Confirming, is ApplyingUpdate, is Completed -> ContentAlpha.high
                        else -> ContentAlpha.disabled
                    },
                    text = stringResource(id = R.string.label_confirming)
                ) {
                    if (deploymentState is Confirming) LinearProgressIndicator(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .height(height = 2.dp)
                            .fillMaxWidth()
                    )
                }
                RowDeploymentState(
                    imageVector = when (deploymentState) {
                        is Cancelled, Failed -> Icons.Rounded.Close
                        else -> Icons.Rounded.Check
                    },
                    color = when (deploymentState) {
                        is ApplyingUpdate, is Completed -> MaterialTheme.colors.primary
                        is Cancelled, Failed -> Color.Red
                        else -> Color.Gray
                    },
                    contentAlpha = when (deploymentState) {
                        is ApplyingUpdate, is Completed -> ContentAlpha.high
                        else -> ContentAlpha.disabled
                    },
                    text = stringResource(id = R.string.label_applying_update)
                ) {
                    if (deploymentState is ApplyingUpdate) LinearProgressIndicator(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .height(height = 2.dp)
                            .fillMaxWidth()
                    )
                }
                RowDeploymentState(
                    imageVector = when (deploymentState) {
                        is Cancelled, Failed -> Icons.Rounded.Close
                        else -> Icons.Rounded.Check
                    },
                    color = when (deploymentState) {
                        is Completed -> MaterialTheme.colors.primary
                        is Cancelled, is Failed -> Color.Red
                        else -> Color.Gray
                    },
                    contentAlpha = when (deploymentState) {
                        is Completed -> ContentAlpha.high
                        else -> ContentAlpha.disabled
                    },
                    text = stringResource(id = R.string.label_completed)
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
                enabled = connectedDevices.isNotEmpty(),
                onClick = {
                    if (deploymentState is Unknown || deploymentState is Building.Unknown ||
                        deploymentState is Building.Error || deploymentState is Downloading.Error || deploymentState is Downloading.Finished ||
                        deploymentState is Completed || deploymentState is Cancelled || deploymentState is Failed) onDeployClick(deploymentTarget)
                    else onCancelDeployClick()
                },
                colors = ButtonDefaults.buttonColors(
                    if (deploymentState is Unknown || deploymentState is Building.Unknown ||
                        deploymentState is Building.Error || deploymentState is Downloading.Error || deploymentState is Downloading.Finished ||
                        deploymentState is Completed || deploymentState is Cancelled || deploymentState is Failed) MaterialTheme.colors.primary
                    else Color.Red
                )
            ) {
                Text(
                    modifier = Modifier.defaultMinSize(minWidth = 80.dp),
                    text = stringResource(
                        id = if (deploymentState is Unknown || deploymentState is Building.Unknown ||
                            deploymentState is Building.Error || deploymentState is Downloading.Error || deploymentState is Downloading.Finished ||
                            deploymentState is Completed || deploymentState is Cancelled || deploymentState is Failed) R.string.action_deploy
                        else R.string.action_cancel
                    ).uppercase(
                        Locale.US
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun RowDeploymentState(
    imageVector: ImageVector = Icons.Filled.Check,
    color: Color = Color.Gray,
    contentAlpha: Float = ContentAlpha.disabled,
    text: String,
    progressText: String = "",
    content: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically

    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = color
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
                        modifier = Modifier.weight(1.0f),
                        text = progressText,
                        textAlign = TextAlign.End
                    )
                }
            }
            content()
        }
    }
}
