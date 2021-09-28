package no.nordicsemi.android.ei.viewmodels.state

/** InferencingState to notify the UI **/
sealed class InferencingState {
    /** Inferencing started state **/
    object Started : InferencingState()

    /** Inferencing stopped state **/
    object Stopped : InferencingState()
}
