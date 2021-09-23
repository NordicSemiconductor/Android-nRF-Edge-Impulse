package no.nordicsemi.android.ei.comms

/**
 * DeploymentState would be used to notify the UI about hte deployment state.
 */
sealed class DeploymentState {
    /** Unknown state **/
    object Unknown : DeploymentState()

    /** When the firmware is being built on the EI backend **/
    sealed class Building : DeploymentState() {
        object Unknown : Building()
        object Started : Building()
        object Finished : Building()
        data class Error(val reason: String?) : Building()
    }

    /** When downloading the firmware from the EI backend **/
    sealed class Downloading : DeploymentState() {
        object Started : Downloading()
        @Suppress("ArrayInDataClass")
        data class Finished(val data: ByteArray) : Downloading()
        data class Error(val error: String) : Downloading()
    }

    /** When the firmware is being validated by the MCU manager **/
    object Verifying : DeploymentState()

    /** When the firmware is being uploaded by the MCU manager to the client **/
    object Uploading : DeploymentState()

    /** When the firmware is being confirmed by the MCU manager **/
    object Confirming : DeploymentState()

    /** When the device is being RESET by the MCU manager **/
    object ApplyingUpdate : DeploymentState()

    /** When the upgrade is being Cancelled by the MCU manager **/
    object Cancelled : DeploymentState()

    /** When the upgrade is being Completed by the MCU manager **/
    object Completed : DeploymentState()

    /** if the upgrade is failed **/
    object Failed : DeploymentState()
}