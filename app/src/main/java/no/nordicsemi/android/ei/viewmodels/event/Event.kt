package no.nordicsemi.android.ei.viewmodels.event

import no.nordicsemi.android.ei.model.Project

sealed class Event

data class ProjectCreated(val projectName: String) : Event()
data class ProjectSelected(val project: Project) : Event()
data class Error(val throwable: Throwable) : Event()
