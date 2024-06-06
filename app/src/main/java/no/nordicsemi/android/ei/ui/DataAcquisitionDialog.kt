package no.nordicsemi.android.ei.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.ui.layouts.AlertDialog


@Composable
fun DataAcquisitionDialog(
    imageVector: ImageVector,
    title: String,
    isDismissEnabled: Boolean = true,
    onDismissRequest: () -> Unit = {},
    isConfirmEnabled : Boolean = true,
    onConfirm: () -> Unit = {},
    properties: DialogProperties,
    content: @Composable () -> Unit
) {
    AlertDialog(
        imageVector = imageVector,
        title = title,
        text = content,
        isDismissEnabled = isDismissEnabled,
        dismissText = stringResource(id = R.string.action_cancel),
        onDismiss = onDismissRequest,
        isConfirmEnabled = isConfirmEnabled,
        confirmText = stringResource(id = R.string.action_start_sampling),
        onConfirm = onConfirm,
        properties = properties
    )
}