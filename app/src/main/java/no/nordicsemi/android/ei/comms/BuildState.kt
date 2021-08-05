package no.nordicsemi.android.ei.comms

sealed class BuildState {
    object Unknown : BuildState()
    object Started : BuildState()
    object Downloading : BuildState()
    object Finished : BuildState()
    data class Error(val reason: String?) : BuildState()
}
