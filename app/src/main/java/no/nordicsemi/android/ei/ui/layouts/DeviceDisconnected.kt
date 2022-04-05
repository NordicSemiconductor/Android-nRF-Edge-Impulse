package no.nordicsemi.android.ei.ui.layouts

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Device

@Composable
fun DeviceDisconnected(connectedDevices: List<Device>) {
    if (connectedDevices.isEmpty()) {
        Text(text = stringResource(R.string.label_no_devices_connected))
    }
}
