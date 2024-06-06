package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.ui.layouts.AlertDialog


@Composable
fun ShowDownloadingDevelopmentKeysDialog() {
    AlertDialog(
        imageVector = Icons.Outlined.HourglassTop,
        title = stringResource(id = R.string.label_please_wait),
        text = {
            Column {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = stringResource(id = R.string.label_fetching_development_keys_socket_token),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
}