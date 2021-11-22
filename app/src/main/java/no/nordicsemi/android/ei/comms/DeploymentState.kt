package no.nordicsemi.android.ei.comms

/**
 * DeploymentState would be used to notify the UI about hte deployment state.
 * @param order Defines the order of each state
 */
sealed class DeploymentState(private val order: Int) : Comparable<DeploymentState> {

    /** Not started **/
    object NotStarted : DeploymentState(0)

    /** When the firmware is being built on the EI backend **/
    object Building : DeploymentState(1)

    /** When downloading the firmware from the EI backend **/
    object Downloading : DeploymentState(2)

    /** When the firmware is being validated by the MCU manager **/
    object Verifying : DeploymentState(3)

    /** When the firmware is being uploaded by the MCU manager to the client **/
    data class Uploading(val transferSpeed: Float = 0f, val percent: Int = 0) : DeploymentState(4)

    /** When the firmware is being confirmed by the MCU manager **/
    object Confirming : DeploymentState(5)

    /** When the device is being RESET by the MCU manager **/
    object ApplyingUpdate : DeploymentState(6)

    /** When the upgrade is being Completed by the MCU manager **/
    object Complete : DeploymentState(7)

    /** if the upgrade is failed **/
    data class Failed(val state: DeploymentState) : DeploymentState(state.order)

    /** When the upgrade is being Cancelled by the MCU manager **/
    data class Canceled(val state: DeploymentState) : DeploymentState(state.order)

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: DeploymentState): Int = order - other.order
}
