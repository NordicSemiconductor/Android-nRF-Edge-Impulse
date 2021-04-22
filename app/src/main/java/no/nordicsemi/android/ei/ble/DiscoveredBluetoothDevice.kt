package no.nordicsemi.android.ei.ble

import android.bluetooth.BluetoothDevice

/**
 * DiscoveredBluetoothDevice
 * <p>
 *     Constructed based on the Bluetooth ScanResult.
 * </p>
 */
data class DiscoveredBluetoothDevice(
    var name: String = "Unknown",
    var rssi: Int,
    val device: BluetoothDevice
)