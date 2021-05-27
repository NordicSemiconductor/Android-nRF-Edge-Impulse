package no.nordicsemi.android.ei.util

import android.bluetooth.BluetoothAdapter
import android.os.Build

object Utils {

    fun isMarshMellowOrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun isBluetoothEnabled(): Boolean =
        BluetoothAdapter.getDefaultAdapter()?.isEnabled ?: false

}

val <T> T.exhaustive: T
    get() = this