package no.nordicsemi.android.ei.ui

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.ble.DiscoveredBluetoothDevice
import no.nordicsemi.android.ei.viewmodels.DevicesViewModel

@Composable
fun Devices(modifier: Modifier = Modifier, viewModel: DevicesViewModel) {
    val listState = rememberLazyListState()
    val discoveredDevices = viewModel.discoveredDevices

    LazyColumn(modifier = modifier.fillMaxSize(), state = listState) {
        item {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.label_devices),
                style = MaterialTheme.typography.h6
            )
        }
        item {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.label_scanner),
                style = MaterialTheme.typography.h6
            )
        }
        discoveredDevices.takeIf { it.isNotEmpty() }?.let { notEmptyDevices ->
            items(items = notEmptyDevices, key = {
                it.device.address
            }) {
                DiscoveredDeviceRow(device = it)
                Divider()
            }
        } ?: run {
            Log.i("AA", "Empty list")
        }
    }
}

@Composable
fun DiscoveredDeviceRow(device: DiscoveredBluetoothDevice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface)
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_uart),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colors.primary,
                    shape = CircleShape
                )
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = device.name,
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.body1
            )
            Text(
                text = device.device.address,
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.caption
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Image(
            painter = painterResource(id = getRssiRes((100.0f * (127.0f + device.rssi) / (127.0f + 20.0f)).toInt())),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .padding(8.dp)
        )
    }
}


@DrawableRes
private fun getRssiRes(rssi: Int): Int = when (rssi) {
    in 0..20 -> R.drawable.ic_signal_0_bar
    in 21..40 -> R.drawable.ic_signal_1_bar
    in 41..60 -> R.drawable.ic_signal_2_bar
    in 61..80 -> R.drawable.ic_signal_3_bar
    else -> R.drawable.ic_signal_4_bar
}