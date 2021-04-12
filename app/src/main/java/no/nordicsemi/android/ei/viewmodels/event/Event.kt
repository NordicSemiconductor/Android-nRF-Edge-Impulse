package no.nordicsemi.android.ei.viewmodels.event

sealed class Event {
    object DismissDialog : Event()
    data class ShowSnackbar(val message: String) : Event()
}