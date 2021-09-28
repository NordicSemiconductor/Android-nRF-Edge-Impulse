package no.nordicsemi.android.ei.ui.layouts

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.ui.theme.NordicRed

@Composable
fun InfoDeviceDisconnectedLayout(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier
                .size(36.dp),
            tint = NordicRed,
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = text
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text)
    }
}