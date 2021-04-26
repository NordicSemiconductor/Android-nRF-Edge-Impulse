package no.nordicsemi.android.ei.ble.state

/**
 * ScannerState that holds the current scanning state
 */
sealed class ScanningState

object Scanning : ScanningState()
data class ScanningStopped(var reason: Reason) : ScanningState()

