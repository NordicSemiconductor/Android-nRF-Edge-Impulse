package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.ui.layouts.AlertDialog

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        imageVector = Icons.Outlined.Info,
        title = stringResource(id = R.string.action_about),
        text = {
            Column {
                Text(text = stringResource(id = R.string.label_about))
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Text(
                        modifier = Modifier.weight(1.0f),
                        text = stringResource(R.string.label_version)
                    )
                    Text(
                        modifier = Modifier.weight(1.0f),
                        text = context.packageManager.getPackageInfo(
                            context.packageName,
                            0
                        ).versionName ?: "Unknown",
                        textAlign = TextAlign.End
                    )
                }
            }
        },
        confirmText = stringResource(id = R.string.action_ok),
        onConfirm = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    )
}