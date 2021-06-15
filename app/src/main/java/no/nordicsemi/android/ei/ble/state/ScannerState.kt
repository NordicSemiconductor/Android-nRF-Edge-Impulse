package no.nordicsemi.android.ei.ble.state

import android.bluetooth.le.ScanResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import no.nordicsemi.android.ei.ble.DiscoveredBluetoothDevice
import no.nordicsemi.android.ei.ble.state.ScanningState.Stopped.*
import kotlin.math.max

/**
 * ScannerState that holds the current scanning state and the list of discovered devices.
 */
class ScannerState(
    initialState: ScanningState = ScanningState.Stopped(Reason.LocationPermissionNotGranted)
) {
    var scanningState: ScanningState by mutableStateOf(initialState)
        private set

    var discoveredDevices = mutableStateListOf<DiscoveredBluetoothDevice>()
        private set

    fun onDeviceFound(scanResult: ScanResult) {
        discoveredDevices.find {
            it.bluetoothDevice == scanResult.device
        }?.let {
            it.rssi = max(-128, scanResult.rssi)
            it.name = scanResult.scanRecord?.deviceName
        } ?: run {
            discoveredDevices += scanResult.toDiscoveredBluetoothDevice()
        }
    }

    /**
     * Invoked when scanning has been started
     */
    fun onScanningStarted() {
        scanningState = ScanningState.Started
    }

    fun onBluetoothDisabled() {
        scanningState = ScanningState.Stopped(Reason.BluetoothDisabled)
        clearDiscoveredDevices()
    }

    fun onLocationPermissionNotGranted() {
        scanningState = ScanningState.Stopped(Reason.LocationPermissionNotGranted)
        clearDiscoveredDevices()
    }

    fun onLocationTurnedOff() {
        scanningState = ScanningState.Stopped(Reason.LocationTurnedOff)
        clearDiscoveredDevices()
    }

    private fun clearDiscoveredDevices() {
        discoveredDevices.clear()
    }
}

private fun ScanResult.toDiscoveredBluetoothDevice(): DiscoveredBluetoothDevice {
    return DiscoveredBluetoothDevice(
        name = scanRecord?.deviceName,
        rssi = rssi,
        bluetoothDevice = device
    )
}