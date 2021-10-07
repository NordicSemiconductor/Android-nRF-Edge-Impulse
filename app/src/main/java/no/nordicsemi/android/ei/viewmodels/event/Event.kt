package no.nordicsemi.android.ei.viewmodels.event

sealed class Event {

    sealed class Project {
        data class Created(val projectName: String): Event()
        data class Selected(val project: no.nordicsemi.android.ei.model.Project): Event()
    }

    data class Error(val throwable: Throwable): Event()
}

