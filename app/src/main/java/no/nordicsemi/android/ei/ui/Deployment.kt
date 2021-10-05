package no.nordicsemi.android.ei.ui

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.annotation.CallSuper
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
import java.util.*

@Composable
fun Deployment(
    project: Project,
    connectedDevices: List<Device>,
    deploymentState: DeploymentState,
    onDeployClick: (Device?) -> Unit,
    progress: Int,
    transferSpeed: Float
) {
    Column(modifier = Modifier.verticalScroll(state = rememberScrollState())) {
        DesignImpulse(project = project)
        DeployImpulse(
            connectedDevices = connectedDevices,
            deploymentState = deploymentState,
            onDeployClick = onDeployClick,
            progress = progress,
            transferSpeed = transferSpeed
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
    deploymentState: DeploymentState,
    onDeployClick: (Device?) -> Unit,
    progress: Int,
    transferSpeed: Float
) {

    var selectedDevice by remember {
        mutableStateOf(connectedDevices.firstOrNull())
    }
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
                RowDeploymentState(
                    color = when (deploymentState) {
                        is Building, is Downloading, is Verifying, is Uploading, is Confirming, is ApplyingUpdate, is Completed -> MaterialTheme.colors.primary
                        else -> Color.Gray
                    },
                    contentAlpha = when (deploymentState) {
                        is Building, is Downloading, is Verifying, is Uploading, is Confirming, is ApplyingUpdate, is Completed -> ContentAlpha.high
                        else -> ContentAlpha.disabled
                    },
                    text = stringResource(id = R.string.label_building)
                ) {
                    when (deploymentState) {
                        is Building.Started -> LinearProgressIndicator(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .height(height = 2.dp)
                                .fillMaxWidth()
                        )
                        is Building.Finished, is Downloading, is Verifying, is Uploading, is Confirming, is ApplyingUpdate, is Completed -> {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .height(height = 2.dp)
                                    .fillMaxWidth(),
                                progress = 1f
                            )
                        }
                        else -> {
                        }
                    }
                }
                RowDeploymentState(
                    color = when (deploymentState) {
                        is Downloading, is Verifying, is Uploading, is Confirming, is ApplyingUpdate, is Completed -> MaterialTheme.colors.primary
                        else -> Color.Gray
                    },
                    contentAlpha = when (deploymentState) {
                        is Downloading, is Verifying, is Uploading, is Confirming, is ApplyingUpdate, is Completed -> ContentAlpha.high
                        else -> ContentAlpha.disabled
                    },
                    text = stringResource(id = R.string.label_downloading)
                ) {
                    when (deploymentState) {
                        is Downloading.Started -> LinearProgressIndicator(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .height(height = 2.dp)
                                .fillMaxWidth()
                        )
                        is Downloading.Finished, is Verifying, is Uploading, is Confirming, is ApplyingUpdate, is Completed -> LinearProgressIndicator(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .height(height = 2.dp)
                                .fillMaxWidth(),
                            progress = 1f
                        )
                        else -> {
                        }
                    }
                }
                RowDeploymentState(
                    color = when (deploymentState) {
                        is Verifying, is Uploading, is Confirming, is ApplyingUpdate, is Completed -> MaterialTheme.colors.primary
                        else -> Color.Gray
                    },
                    contentAlpha = when (deploymentState) {
                        is Verifying, is Uploading, is Confirming, is ApplyingUpdate, is Completed -> ContentAlpha.high
                        else -> ContentAlpha.disabled
                    },
                    text = stringResource(id = R.string.label_verifying)
                ) {
                    when (deploymentState) {
                        is Verifying -> LinearProgressIndicator(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .height(height = 2.dp)
                                .fillMaxWidth()
                        )
                        is Uploading, is Confirming, is ApplyingUpdate, is Completed -> LinearProgressIndicator(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .height(height = 2.dp)
                                .fillMaxWidth(),
                            progress = 1f
                        )
                        else -> {
                        }
                    }
                }
                RowDeploymentState(
                    color = when (deploymentState) {
                        is Uploading, is Confirming, is ApplyingUpdate, is Completed -> MaterialTheme.colors.primary
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
                    when (deploymentState) {
                        is Uploading ->
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .height(height = 2.dp)
                                    .fillMaxWidth(),
                                progress = progress / 100f
                            )
                        is Confirming, is ApplyingUpdate, is Completed -> LinearProgressIndicator(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .height(height = 2.dp)
                                .fillMaxWidth(),
                            progress = 1f
                        )
                        else -> {
                        }
                    }
                }
                RowDeploymentState(
                    color = when (deploymentState) {
                        is Confirming, is ApplyingUpdate, is Completed -> MaterialTheme.colors.primary
                        else -> Color.Gray
                    },
                    contentAlpha = when (deploymentState) {
                        is Confirming, is ApplyingUpdate, is Completed -> ContentAlpha.high
                        else -> ContentAlpha.disabled
                    },
                    text = stringResource(id = R.string.label_confirming)
                ) {
                    when (deploymentState) {
                        is Confirming ->
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .height(height = 2.dp)
                                    .fillMaxWidth()
                            )
                        is ApplyingUpdate, is Completed -> LinearProgressIndicator(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .height(height = 2.dp)
                                .fillMaxWidth(),
                            progress = 1f
                        )
                        else -> {
                        }
                    }
                }
                RowDeploymentState(
                    color = when (deploymentState) {
                        is ApplyingUpdate, is Completed -> MaterialTheme.colors.primary
                        else -> Color.Gray
                    },
                    contentAlpha = when (deploymentState) {
                        is ApplyingUpdate, is Completed -> ContentAlpha.high
                        else -> ContentAlpha.disabled
                    },
                    text = stringResource(id = R.string.label_applying_update)
                ) {
                    when (deploymentState) {
                        is ApplyingUpdate ->
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .height(height = 2.dp)
                                    .fillMaxWidth()
                            )
                        is Completed -> LinearProgressIndicator(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .height(height = 2.dp)
                                .fillMaxWidth(),
                            progress = 1f
                        )
                        else -> {
                        }
                    }
                }
                RowDeploymentState(
                    imageVector = when (deploymentState) {
                        is Cancelled, Failed -> Icons.Rounded.Close
                        else -> Icons.Rounded.Check
                    },
                    color = when (deploymentState) {
                        is Completed -> MaterialTheme.colors.primary
                        is Cancelled -> Color.Red
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
                //enabled = deploymentState is Unknown || deploymentState is Completed,
                onClick = {
                    onDeployClick(selectedDevice)
                }) {
                Text(
                    text = stringResource(id = R.string.action_deploy).uppercase(
                        Locale.US
                    )
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

@Composable
private fun getColor(deploymentState: DeploymentState): Color = when (deploymentState) {
    is Building, is Downloading, is Verifying, is Uploading, is Confirming, is ApplyingUpdate, is Completed -> MaterialTheme.colors.primary
    else -> Color.Gray
}

private class CreateZipFile : CreateDocument() {
    @CallSuper
    override fun createIntent(context: Context, input: String): Intent =
        super.createIntent(context, input).setType(MIME_TYPE)
}

private const val MIME_TYPE = "application/zip"
