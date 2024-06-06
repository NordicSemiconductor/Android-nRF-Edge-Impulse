package no.nordicsemi.android.ei.ui.layouts

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.DialogProperties


@Composable
fun AlertDialog(
    imageVector: ImageVector,
    title: String,
    text: @Composable () -> Unit = {},
    dismissText: String? = null,
    isDismissEnabled: Boolean = true,
    onDismiss: () -> Unit = {},
    isConfirmEnabled: Boolean = true,
    confirmText: String? = null,
    onConfirm: () -> Unit = {},
    properties: DialogProperties = DialogProperties(),
    isDestructive: Boolean = false
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = when {
                    isDestructive -> MaterialTheme.colorScheme.error
                    else -> LocalContentColor.current
                }
            )
        },
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        text = text,
        onDismissRequest = onDismiss,
        dismissButton = {
            dismissText?.let {
                TextButton(onClick = onDismiss, enabled = isDismissEnabled) {
                    Text(text = it)
                }
            }
        },
        confirmButton = {
            confirmText?.let {
                TextButton(onClick = onConfirm, enabled = isConfirmEnabled) {
                    Text(
                        text = it,
                        color = when {
                            isDestructive -> MaterialTheme.colorScheme.error
                            else -> LocalContentColor.current
                        }
                    )
                }
            }
        },
        properties = properties
    )
}