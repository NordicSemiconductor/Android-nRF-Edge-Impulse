/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

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
    val bluetoothDevice: BluetoothDevice
) {
    /** The device ID. */
    val deviceId = bluetoothDevice.address
}

fun DiscoveredBluetoothDevice.rssiAsPercent() = (100.0f * (127.0f + rssi) / (127.0f + 20.0f)).toInt()