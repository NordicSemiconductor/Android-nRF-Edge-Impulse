package no.nordicsemi.android.ei.comms

sealed class DeploymentState {

    object Unknown : DeploymentState()

    sealed class Building : DeploymentState() {
        object Unknown : Building()
        object Started : Building()
        object Finished : Building()
        data class Error(val reason: String?) : Building()
    }

    sealed class Downloading : DeploymentState() {
        object Started : Downloading()
        data class Finished(val data: ByteArray) : Downloading()
        data class Error(val error: String) : Downloading()
    }

    object Verifying : DeploymentState()

    object Uploading : DeploymentState()

    object Testing : DeploymentState()

    object ApplyingUpdate : DeploymentState()

    object Confirming : DeploymentState()

    object Completed : DeploymentState()
}