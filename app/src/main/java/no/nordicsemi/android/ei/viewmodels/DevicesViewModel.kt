/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.ei.ble.BleDevice
import no.nordicsemi.android.ei.ble.DiscoveredBluetoothDevice
import no.nordicsemi.android.ei.ble.state.ScannerState
import no.nordicsemi.android.ei.ble.state.ScanningState
import no.nordicsemi.android.ei.comms.CommsManager
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.util.guard
import no.nordicsemi.android.ei.viewmodels.state.DeviceState
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : AndroidViewModel(context as Application) {

    val scannerState = ScannerState(ScanningState.Initializing)

    var device by mutableStateOf<Device?>(null)
        private set

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.takeIf { bluetoothAdapter.isEnabled }?.let { scanResult ->
                scannerState.onDeviceFound(scanResult = scanResult)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        guard(bluetoothAdapter.isEnabled) {
            scannerState.onBluetoothDisabled()
            return
        }

        guard(scannerState.scanningState != ScanningState.Started) {
            return
        }
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(BleDevice.serviceUuid))
            .build()
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()
        bluetoothAdapter.bluetoothLeScanner?.apply {
            scannerState.onScanningStarted()
            startScan(listOf(scanFilter), scanSettings, scanCallback)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        //guard(scannerState.scanningState != ScanningState.Stopped) { return }
        bluetoothAdapter.bluetoothLeScanner?.apply { stopScan(scanCallback) }
    }

    fun discoveredBluetoothDevice(configuredDevice: Device): DiscoveredBluetoothDevice? =
        scannerState.discoveredDevices.find { it.deviceId == configuredDevice.deviceId }

    fun deviceState(
        configuredDevice: Device,
        activeDevices: Map<String, CommsManager>
    ): DeviceState =
        discoveredBluetoothDevice(configuredDevice = configuredDevice)
            ?.let {
                activeDevices[configuredDevice.deviceId]?.connectivityState
                    ?: DeviceState.IN_RANGE
            } ?: DeviceState.NOT_IN_RANGE

    fun onDeviceSelected(device: Device) {
        this.device = device
    }
}