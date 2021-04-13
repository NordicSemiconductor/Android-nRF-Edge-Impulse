package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.service.param.CreateProjectRequest
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    service: EiService
) : BaseRepository(service = service) {

    suspend fun createProject(token: String, projectName: String) = withContext(Dispatchers.IO) {
        service.createProject(
            jwt = token,
            createProjectRequest = CreateProjectRequest(projectName = projectName)
        )
    }
}