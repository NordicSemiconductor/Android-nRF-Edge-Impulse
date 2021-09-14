package no.nordicsemi.android.ei.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.annotation.CallSuper
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.Launch
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.viewmodels.state.DownloadState
import java.util.*

@Composable
fun Deployment(
    project: Project,
    connectedDevices: List<Device>,
    downloadState: DownloadState,
    onDownloadFirmwareClick: () -> Unit,
    onSaveClick: (Uri, ByteArray) -> Unit
) {
    var selectedDevice by remember {
        mutableStateOf(connectedDevices.firstOrNull())
    }
    Column(modifier = Modifier.verticalScroll(state = rememberScrollState())) {
        DesignImpulse(project = project)
        DeployImpulse(
            connectedDevices = connectedDevices,
            selectedDevice = selectedDevice,
            onDeviceSelected = {
                selectedDevice = it
            },
            downloadState = downloadState,
            onDownloadFirmwareClick = onDownloadFirmwareClick,
            onSaveClick = onSaveClick,
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
    selectedDevice: Device?,
    onDeviceSelected: (Device) -> Unit,
    downloadState: DownloadState,
    onDownloadFirmwareClick: () -> Unit,
    onSaveClick: (Uri, ByteArray) -> Unit
) {
    val context = LocalContext.current
    var isDevicesMenuExpanded by remember { mutableStateOf(false) }
    var width by rememberSaveable { mutableStateOf(0) }
    var fileName by rememberSaveable {
        mutableStateOf("")
    }
    var fileSize by rememberSaveable {
        mutableStateOf(0)
    }
    val fileSaveLauncher = rememberLauncherForActivityResult(
        contract = CreateZipFile("application/zip"),
        onResult = { uri ->
            if (downloadState is DownloadState.Saving) {
                context.contentResolver?.query(uri, null, null, null, null)?.let { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.getColumnIndex(OpenableColumns.SIZE)
                    cursor.moveToFirst()
                    fileName = cursor.getString(nameIndex)
                    fileSize = downloadState.data.size/1000
                    cursor.close()
                }
                onSaveClick(uri, downloadState.data)
            }
        }
    )
    if (downloadState is DownloadState.Saving) {
        SideEffect {
            fileName = downloadState.fileName
            fileSaveLauncher.launch(fileName)
        }
    }
    Column(modifier = Modifier.padding(bottom = 72.dp)) {
        Surface(elevation = 2.dp) {
            Column(
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(1.0f),
                        text = stringResource(R.string.label_select_firmware),
                        style = MaterialTheme.typography.h6
                    )
                    if (downloadState !is DownloadState.Downloading || downloadState !is DownloadState.Saving) {
                        IconButton(
                            onClick = { onDownloadFirmwareClick() }) {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = null
                            )
                        }
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(
                        onClick = { }) {
                        Icon(imageVector = Icons.Outlined.FolderOpen, contentDescription = null)
                    }
                }
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = stringResource(R.string.label_select_firmware_rationale)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.label_file_name),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(height = 8.dp))
                        Text(
                            text = stringResource(R.string.label_file_size),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        if (fileName.isNotEmpty())
                            Text(
                                text = fileName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        Spacer(modifier = Modifier.height(height = 8.dp))
                        if (fileSize != 0)
                            Text(
                                text = stringResource(R.string.label_kb, fileSize),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                    }
                }
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
                                    onDeviceSelected(it)
                                },
                                onDismiss = {
                                    isDevicesMenuExpanded = false
                                }
                            )
                        }
                    },
                    singleLine = true
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
                onClick = {}) {
                Text(
                    text = stringResource(id = R.string.action_deploy).uppercase(
                        Locale.US
                    )
                )
            }
        }
    }
}
private class CreateZipFile(private val fileType:String) : CreateDocument() {
    @CallSuper
    override fun createIntent(context: Context, input: String): Intent =
        super.createIntent(context, input).setType(fileType)
}
