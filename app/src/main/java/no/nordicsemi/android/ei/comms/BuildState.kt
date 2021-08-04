package no.nordicsemi.android.ei.comms

sealed class BuildState {
    object Started : BuildState()
    object Finished : BuildState()
    data class Error(val reason: String?) : BuildState()
}
