package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.di.DefaultDispatcher
import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.service.param.CreateProjectRequest
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    service: EiService,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
) : BaseRepository(service = service, defaultDispatcher = defaultDispatcher) {

    suspend fun createProject(token: String, projectName: String) = withContext(defaultDispatcher) {
        service.createProject(
            jwt = token,
            createProjectRequest = CreateProjectRequest(projectName = projectName)
        )
    }

    suspend fun developmentKeys(token: String, projectId: Int) = withContext(defaultDispatcher) {
        service.developmentKeys(token, projectId)
    }
}