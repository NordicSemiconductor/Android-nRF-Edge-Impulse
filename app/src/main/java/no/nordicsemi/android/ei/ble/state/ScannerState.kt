package no.nordicsemi.android.ei.ble.state

import android.bluetooth.le.ScanResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import no.nordicsemi.android.ei.ble.DiscoveredBluetoothDevice

/**
 * ScannerState that holds the current scanning state and the list of discovered devices.
 */
class ScannerState(state: ScanningState = ScanningStopped(NotStarted)) {
    var scanningState: ScanningState by mutableStateOf(state)
        private set

    var discoveredDevices: List<DiscoveredBluetoothDevice> by mutableStateOf(listOf())
        private set

    fun onDeviceFound(scanResult: ScanResult) {
        discoveredDevices.find {
            it.device == scanResult.device
        }?.let {
            it.rssi = when {
                scanResult.rssi <= -128 -> -128
                else -> scanResult.rssi
            }
            it.name = scanResult.scanRecord?.deviceName ?: "Unknown"
        } ?: run {
            discoveredDevices =
                discoveredDevices + scanResult.toDiscoveredBluetoothDevice()
        }
    }

    /**
     * Invoked when scanning has been started
     */
    fun onScanningStarted() {
        clearDiscoveredDevices()
        scanningState = Scanning
    }

    fun onScanningNotStarted() {
        clearDiscoveredDevices()
        scanningState = ScanningStopped(NotStarted)
    }

    fun onBluetoothDisabled() {
        scanningState = ScanningStopped(BluetoothDisabled)
        clearDiscoveredDevices()
    }

    fun onLocationPermissionNotGranted() {
        scanningState = ScanningStopped(LocationPermissionNotGranted)
        clearDiscoveredDevices()
    }

    fun onLocationTurnedOff() {
        scanningState = ScanningStopped(LocationTurnedOff)
        clearDiscoveredDevices()
    }

    private fun clearDiscoveredDevices() {
        discoveredDevices = discoveredDevices - discoveredDevices
    }
}

private fun ScanResult.toDiscoveredBluetoothDevice(): DiscoveredBluetoothDevice {
    return DiscoveredBluetoothDevice(
        name = scanRecord?.deviceName ?: "Unknown",
        rssi = rssi,
        device = device
    )
}