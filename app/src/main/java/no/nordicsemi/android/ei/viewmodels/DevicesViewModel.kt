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
import dagger.hilt.EntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import no.nordicsemi.android.ei.ble.state.ScannerState
import no.nordicsemi.android.ei.ble.state.ScanningState
import no.nordicsemi.android.ei.ble.state.ScanningState.Stopped.Reason
import no.nordicsemi.android.ei.di.ProjectComponentEntryPoint
import no.nordicsemi.android.ei.di.ProjectManager
import no.nordicsemi.android.ei.di.UserComponentEntryPoint
import no.nordicsemi.android.ei.di.UserManager
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.repository.ProjectDataRepository
import no.nordicsemi.android.ei.repository.ProjectRepository
import no.nordicsemi.android.ei.util.Utils.isBluetoothEnabled
import no.nordicsemi.android.ei.util.Utils.isMarshMellowOrAbove
import no.nordicsemi.android.ei.util.guard
import no.nordicsemi.android.ei.viewmodels.event.Event
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val userManager: UserManager,
    private val projectRepository: ProjectRepository
) : AndroidViewModel(context as Application) {
    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    // TODO This needs to be fixed: Possible NPE when switching back to the app.
    private val projectManager: ProjectManager
        get() = EntryPoints
            .get(userManager.userComponent!!, UserComponentEntryPoint::class.java)
            .getProjectManager()

    // TODO This needs to be fixed: Possible NPE when switching back to the app.
    private val projectDataRepository: ProjectDataRepository
        get() = EntryPoints
            .get(projectManager.projectComponent!!, ProjectComponentEntryPoint::class.java)
            .projectDataRepository()

    var configuredDevices: List<Device> by mutableStateOf(listOf())
        private set

    val scannerState = ScannerState(updateScanningState())

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
                        previousState != BluetoothAdapter.STATE_OFF) {
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

        guard(isLocationPermissionGranted(context = getApplication())) {
            scannerState.onLocationPermissionNotGranted()
            return
        }

        guard(isLocationEnabled(context = getApplication())) {
            scannerState.onLocationTurnedOff()
            return
        }

        guard(scannerState.scanningState != ScanningState.Started) {
            return
        }

        //TODO add a scan filter
        //val scanFilter = ScanFilter.Builder().build()
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()
        BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner?.apply {
            scannerState.onScanningStarted()
            startScan(null, scanSettings, scanCallback)
        }
    }

    private fun stopScan() {
        BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
            ?.takeIf { scannerState.scanningState == ScanningState.Started }
            ?.apply { stopScan(scanCallback) }
    }

    private fun stopScan(reason: Reason) {
        stopScan()
        when (reason) {
            is Reason.BluetoothDisabled -> scannerState.onBluetoothDisabled()
            is Reason.LocationPermissionNotGranted -> scannerState.onLocationPermissionNotGranted()
            is Reason.LocationTurnedOff -> scannerState.onLocationTurnedOff()
        }
    }

    private fun updateScanningState(): ScanningState = if (isBluetoothEnabled()) {
        if (isMarshMellowOrAbove()) {
            if (isLocationPermissionGranted(context = getApplication())) {
                if (isLocationEnabled(context = getApplication())) {
                    ScanningState.Started
                } else {
                    ScanningState.Stopped(Reason.LocationTurnedOff)
                }
            } else {
                ScanningState.Stopped(Reason.LocationPermissionNotGranted)
            }
        } else {
            ScanningState.Started
        }
    } else {
        ScanningState.Stopped(Reason.BluetoothDisabled)
    }

    companion object {

        private fun isLocationEnabled(context: Context) =
            !isMarshMellowOrAbove() || LocationManagerCompat.isLocationEnabled(
                context.getSystemService(LocationManager::class.java)
            )

        private fun isLocationPermissionGranted(context: Context) =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    }
}