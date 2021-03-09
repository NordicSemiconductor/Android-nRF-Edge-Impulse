package no.nordicsemi.android.ei.service.param

import no.nordicsemi.android.ei.model.Project

/**
 * Retrieve list of active projects.
 *
 * @see <a href="https://docs.edgeimpulse.com/reference#listprojects">Docs: List active projects</a>
 */
data class ProjectResponse(
    val projects: List<Project>,
    val success: Boolean,
    val error: String?
)
