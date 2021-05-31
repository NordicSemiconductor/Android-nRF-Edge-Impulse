package no.nordicsemi.android.ei.ble.state

/**
 * ScannerState that holds the current scanning state
 */
sealed class ScanningState {
    object Started: ScanningState()
    data class Stopped(var reason: Reason): ScanningState() {
        /**
         * Reason
         */
        sealed class Reason {
            object NotStarted : Reason()
            object BluetoothDisabled : Reason()
            object LocationPermissionNotGranted : Reason()
            object LocationTurnedOff : Reason()
            data class Unknown(val throwable: Throwable) : Reason()
        }
    }
}