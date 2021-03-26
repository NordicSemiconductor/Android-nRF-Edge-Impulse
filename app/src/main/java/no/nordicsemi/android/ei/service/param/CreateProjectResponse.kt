package no.nordicsemi.android.ei.service.param

/**
 * Create new project response.
 *
 * @see <a href="https://docs.edgeimpulse.com/reference#createproject">Docs: Create new project</a>
 */
data class CreateProjectResponse(
    val projectId: Int,
    val success: Boolean = false,
    val error: String?
)
