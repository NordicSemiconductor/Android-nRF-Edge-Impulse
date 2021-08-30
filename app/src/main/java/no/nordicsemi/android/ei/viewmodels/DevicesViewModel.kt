package no.nordicsemi.android.ei.viewmodels

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
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
import no.nordicsemi.android.ei.ble.state.ScanningState.Stopped.Reason
import no.nordicsemi.android.ei.comms.DataAcquisitionManager
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.util.Utils.isAndroidS
import no.nordicsemi.android.ei.util.Utils.isBluetoothEnabled
import no.nordicsemi.android.ei.util.Utils.isBluetoothPermissionsGranted
import no.nordicsemi.android.ei.util.Utils.isLocationEnabled
import no.nordicsemi.android.ei.util.Utils.isLocationPermissionGranted
import no.nordicsemi.android.ei.util.Utils.isMarshMellowOrAbove
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
    private val bluetoothAdapter = bluetoothManager.adapter

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.takeIf { isBluetoothEnabled() }?.let { scanResult ->
                scannerState.onDeviceFound(scanResult = scanResult)
            }
        }
    }

    private val bluetoothStateChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val currentState = intent?.getIntExtra(
                BluetoothAdapter.EXTRA_STATE,
                BluetoothAdapter.STATE_OFF
            )
            val previousState = intent?.getIntExtra(
                BluetoothAdapter.EXTRA_PREVIOUS_STATE,
                BluetoothAdapter.STATE_OFF
            )
            when (currentState) {
                BluetoothAdapter.STATE_ON -> {
                    startScan()
                }
                BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF -> {
                    if (previousState != BluetoothAdapter.STATE_TURNING_OFF &&
                        previousState != BluetoothAdapter.STATE_OFF
                    ) {
                        stopScan(Reason.BluetoothDisabled)
                    }
                }
            }
        }
    }

    private val locationProviderChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                when (isLocationEnabled(it)) {
                    true -> startScan()
                    false -> stopScan(Reason.LocationTurnedOff)
                }
            }
        }
    }

    init {
        registerBroadcastReceiver(context)
        startScan()
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
        unregisterBroadcastReceiver(context = getApplication())
    }

    private fun registerBroadcastReceiver(context: Context) {
        context.registerReceiver(
            bluetoothStateChangedReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
        if (isMarshMellowOrAbove()) {
            context.registerReceiver(
                locationProviderChangedReceiver,
                IntentFilter(LocationManager.MODE_CHANGED_ACTION)
            )
        }
    }

    private fun unregisterBroadcastReceiver(context: Context) {
        context.unregisterReceiver(bluetoothStateChangedReceiver)
        if (isMarshMellowOrAbove()) {
            context.unregisterReceiver(locationProviderChangedReceiver)
        }
    }

    fun startScan() {
        guard(isBluetoothEnabled()) {
            scannerState.onBluetoothDisabled()
            return
        }
        if (isAndroidS()) {
            // Bluetooth permissions Scan and Connect are required from Andriod S onwards.
            guard(isBluetoothPermissionsGranted(context = getApplication())) {
                scannerState.onBluetoothScanPermissionNotGranted()
                return
            }
        } else {
            // Location permission is still required for versions in between Android marshmallow and S
            guard(isLocationPermissionGranted(context = getApplication())) {
                scannerState.onLocationPermissionNotGranted()
                return
            }
            guard(isLocationEnabled(context = getApplication())) {
                scannerState.onLocationTurnedOff()
                return
            }
        }


        guard(scannerState.scanningState != ScanningState.Started) {
            return
        }

        // TODO Add a scan filter
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(BleDevice.serviceUuid))
            .build()
        val filters = listOf(scanFilter)
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()
        bluetoothAdapter.bluetoothLeScanner?.apply {
            scannerState.onScanningStarted()
            startScan(filters, scanSettings, scanCallback)
        }
    }

    private fun stopScan() {
        guard(scannerState.scanningState == ScanningState.Started) {
            return
        }
        bluetoothAdapter.bluetoothLeScanner?.apply {
            stopScan(scanCallback)
        }
    }

    private fun stopScan(reason: Reason) {
        stopScan()
        when (reason) {
            is Reason.BluetoothDisabled -> scannerState.onBluetoothDisabled()
            is Reason.BluetoothScanPermissionNotGranted -> scannerState.onLocationPermissionNotGranted()
            is Reason.LocationPermissionNotGranted -> scannerState.onLocationPermissionNotGranted()
            is Reason.LocationTurnedOff -> scannerState.onLocationTurnedOff()
        }
    }

    fun discoveredBluetoothDevice(configuredDevice: Device): DiscoveredBluetoothDevice? =
        scannerState.discoveredDevices.find { it.deviceId == configuredDevice.deviceId }

    fun deviceState(
        configuredDevice: Device,
        activeDevices: Map<String, DataAcquisitionManager>
    ): DeviceState =
        discoveredBluetoothDevice(configuredDevice = configuredDevice)
            ?.let {
                activeDevices[configuredDevice.deviceId]?.state
                    ?: DeviceState.IN_RANGE
            } ?: DeviceState.NOT_IN_RANGE

    fun onDeviceSelected(device: Device) {
        this.device = device
    }
}