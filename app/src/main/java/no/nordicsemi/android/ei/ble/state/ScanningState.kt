package no.nordicsemi.android.ei.ble.state

/**
 * ScannerState that holds the current scanning state.
 */
sealed class ScanningState {
    object Started: ScanningState()
    data class Stopped(var reason: Reason): ScanningState() {

        sealed class Reason {
            object BluetoothDisabled : Reason()
            object LocationPermissionNotGranted : Reason()
            object LocationTurnedOff : Reason()
        }
    }
}