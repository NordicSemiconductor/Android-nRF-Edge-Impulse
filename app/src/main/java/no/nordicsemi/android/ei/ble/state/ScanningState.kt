package no.nordicsemi.android.ei.ble.state

/**
 * ScannerState that holds the current scanning state.
 */
sealed class ScanningState {
    object Initializing : ScanningState()
    object Started : ScanningState()
    object Stopped : ScanningState()
}