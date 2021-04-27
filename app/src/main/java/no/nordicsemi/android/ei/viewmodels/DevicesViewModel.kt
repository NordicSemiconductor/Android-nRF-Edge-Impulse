package no.nordicsemi.android.ei.viewmodels

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.ei.ble.state.*
import no.nordicsemi.android.ei.di.UserManager
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.repository.ProjectRepository
import no.nordicsemi.android.ei.util.Utils.isBluetoothEnabled
import no.nordicsemi.android.ei.util.Utils.isMarshMellowOrAbove
import javax.inject.Inject


@HiltViewModel
class DevicesViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val userManager: UserManager,
    private val projectRepository: ProjectRepository
) : AndroidViewModel(context as Application) {

    var configuredDevices: List<Device> by mutableStateOf(listOf())
        private set

    val scannerState = ScannerState(updateScanningState())

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (isBluetoothEnabled())
                result?.let { scanResult ->
                    scannerState.onDeviceFound(scanResult = scanResult)
                }
        }
    }

    private val bluetoothStateChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val currentState =
                intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
            val previousState = intent?.getIntExtra(
                BluetoothAdapter.EXTRA_PREVIOUS_STATE,
                BluetoothAdapter.STATE_OFF
            )
            when (currentState) {
                BluetoothAdapter.STATE_ON -> {
                    startScan()
                }
                BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF -> {
                    if (previousState != BluetoothAdapter.STATE_TURNING_OFF && previousState != BluetoothAdapter.STATE_OFF) {
                        stopScan(BluetoothDisabled)
                    }
                }
            }
        }
    }

    private val locationProviderChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                when (isLocationEnabled(it)) {
                    true -> {
                        startScan()
                    }
                    false -> {
                        stopScan(LocationTurnedOff)
                    }
                }
            }
        }
    }

    init {
        registerBroadcastReceiver(context = context)
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
        if (!isBluetoothEnabled()) {
            scannerState.onBluetoothDisabled()
            return
        }

        if (!isLocationPermissionGranted(context = getApplication())) {
            scannerState.onLocationPermissionNotGranted()
            return
        }

        if (!isLocationEnabled(context = getApplication())) {
            scannerState.onLocationTurnedOff()
            return
        }

        if (scannerState.scanningState == Scanning) {
            return
        }

        //TODO add a scan filter
        //val scanFilter = ScanFilter.Builder().build()
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()
        BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner?.let {
            scannerState.onScanningStarted()
            it.startScan(null, scanSettings, scanCallback)
        }
    }

    private fun stopScan() {
        BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner?.let {
            if (scannerState.scanningState == Scanning) {
                it.stopScan(scanCallback)
            }
        }
    }

    private fun stopScan(reason: Reason) {
        BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner?.let {
            if (scannerState.scanningState == Scanning) {
                it.stopScan(scanCallback)
            }
        }
        when (reason) {
            is NotStarted -> {
                scannerState.onScanningNotStarted()
            }
            is BluetoothDisabled -> {
                scannerState.onBluetoothDisabled()
            }
            is LocationPermissionNotGranted -> {
                scannerState.onLocationPermissionNotGranted()
            }
            is LocationTurnedOff -> {
                scannerState.onLocationTurnedOff()
            }
            is Error -> {
                //TODO Handle some other errors?
            }
        }
    }

    private fun updateScanningState(): ScanningState = if (isBluetoothEnabled()) {
        if (isMarshMellowOrAbove()) {
            if (isLocationPermissionGranted(context = getApplication())) {
                if (isLocationEnabled(context = getApplication())) {
                    ScanningStopped(NotStarted)
                } else {
                    ScanningStopped(reason = LocationTurnedOff)
                }
            } else {
                ScanningStopped(reason = LocationPermissionNotGranted)
            }
        } else {
            ScanningStopped(reason = NotStarted)
        }
    } else {
        ScanningStopped(reason = BluetoothDisabled)
    }

    companion object {

        private fun isLocationEnabled(context: Context): Boolean = if (isMarshMellowOrAbove()) {
            val locationManager = context.getSystemService(LocationManager::class.java)
            LocationManagerCompat.isLocationEnabled(locationManager)
        } else {
            true
        }

        private fun isLocationPermissionGranted(context: Context): Boolean =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }
}