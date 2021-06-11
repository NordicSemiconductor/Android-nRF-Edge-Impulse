package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.di.IODispatcher
import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.service.param.CreateProjectRequest
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    service: EiService,
    @IODispatcher ioDispatcher: CoroutineDispatcher
) : BaseRepository(service = service, ioDispatcher = ioDispatcher) {

    suspend fun createProject(token: String, projectName: String) = withContext(ioDispatcher) {
        service.createProject(jwt = token, createProjectRequest = CreateProjectRequest(projectName))
    }

    suspend fun developmentKeys(token: String, projectId: Int) = withContext(ioDispatcher) {
        service.developmentKeys(jwt = token, projectId = projectId)
    }
}