package no.nordicsemi.android.ei.ble

import android.bluetooth.BluetoothDevice

/**
 * DiscoveredBluetoothDevice
 * <p>
 *     Constructed based on the Bluetooth ScanResult.
 * </p>
 */
data class DiscoveredBluetoothDevice(
    var name: String?,
    var rssi: Int,
    val device: BluetoothDevice
)

fun DiscoveredBluetoothDevice.rssiAsPercent() = (100.0f * (127.0f + rssi) / (127.0f + 20.0f)).toInt()