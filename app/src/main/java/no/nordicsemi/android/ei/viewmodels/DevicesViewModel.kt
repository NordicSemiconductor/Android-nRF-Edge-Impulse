package no.nordicsemi.android.ei.viewmodels

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.ei.ble.DiscoveredBluetoothDevice
import no.nordicsemi.android.ei.di.UserManager
import no.nordicsemi.android.ei.repository.ProjectRepository
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val userManager: UserManager,
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val scanner: BluetoothLeScanner? =
        BluetoothAdapter.getDefaultAdapter()?.bluetoothLeScanner

    var discoveredDevices: List<DiscoveredBluetoothDevice> by mutableStateOf(listOf())
        private set

    private var isScanning = false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let { scanResult ->
                discoveredDevices
                    .find {
                        it.device == scanResult.device
                    }?.let {
                        it.rssi = when {
                            result.rssi <= -128 -> -128
                            else -> result.rssi
                        }
                        it.name = result.scanRecord?.deviceName ?: "Unknown"
                    } ?: run {
                    discoveredDevices = discoveredDevices + scanResult.toDiscoveredBluetoothDevice()
                }
            }
        }
    }

    init {
        startScan()
    }

    override fun onCleared() {
        super.onCleared()
        stopScanning()
    }

    private fun startScan() {
        //TODO handle permissions
        if (isScanning)
            return

        //TODO add a scan filter
        //val scanFilter = ScanFilter.Builder().build()
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()
        scanner?.let {
            isScanning = true
            it.startScan(null, scanSettings, scanCallback)
        }
    }

    private fun stopScanning() {
        scanner?.let {
            if (isScanning) {
                isScanning = false
                it.stopScan(scanCallback)
            }
        }
    }
}

private fun ScanResult.toDiscoveredBluetoothDevice(): DiscoveredBluetoothDevice {
    return DiscoveredBluetoothDevice(
        name = scanRecord?.deviceName ?: "Unknown",
        rssi = rssi,
        device = device
    )
}