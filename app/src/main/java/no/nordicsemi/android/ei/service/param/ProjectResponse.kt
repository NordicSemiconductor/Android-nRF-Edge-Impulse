package no.nordicsemi.android.ei.service.param

import no.nordicsemi.android.ei.model.Project

data class ProjectResponse(
    val projects: List<Project>,
    val success: Boolean = false,
    val error: String?
)
