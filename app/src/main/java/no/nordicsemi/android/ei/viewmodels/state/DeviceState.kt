package no.nordicsemi.android.ei.viewmodels.state

import androidx.compose.ui.graphics.Color
import no.nordicsemi.android.ei.ui.theme.NordicDarkGrey
import no.nordicsemi.android.ei.ui.theme.NordicFall
import no.nordicsemi.android.ei.ui.theme.NordicLake
import no.nordicsemi.android.ei.ui.theme.NordicSun

enum class DeviceState {
    /** The device is not in range, or does not support Bluetooth LE. */
    NOT_IN_RANGE,
    /** The device has been scanned, but is not connected. User can tap to connect. */
    IN_RANGE,
    /** The device is currently being set up. */
    CONNECTING,
    /** The device is connected and needs to be authenticated. */
    AUTHENTICATING,
    /** The device is ready.. */
    AUTHENTICATED,
}

fun DeviceState.indicatorColor() = when (this) {
    DeviceState.NOT_IN_RANGE -> NordicDarkGrey
    DeviceState.IN_RANGE -> NordicLake
    DeviceState.CONNECTING -> NordicFall
    DeviceState.AUTHENTICATING -> NordicSun
    DeviceState.AUTHENTICATED -> Color.Green
}