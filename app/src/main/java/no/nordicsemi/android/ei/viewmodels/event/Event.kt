package no.nordicsemi.android.ei.viewmodels.event

sealed class Event

object DismissDialog : Event()
data class ProjectCreated(val projectName: String) : Event()
object ProjectSelected : Event()
data class Error(val throwable: Throwable) : Event()
