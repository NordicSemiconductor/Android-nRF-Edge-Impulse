package no.nordicsemi.android.ei.ble.state

/**
 * Reason
 */
sealed class Reason

object NotStarted : Reason()
object BluetoothDisabled : Reason()
object LocationPermissionNotGranted : Reason()
object LocationTurnedOff : Reason()
data class Unknown(val throwable: Throwable) : Reason()

