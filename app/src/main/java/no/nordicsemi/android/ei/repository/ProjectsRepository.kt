package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.service.EiService

class ProjectsRepository(private val service: EiService) {

    suspend fun projects(token: String) = withContext(Dispatchers.IO) {
        service.projects("jwt=$token")
    }

    suspend fun developmentKeys(token: String, projectId: Int) = withContext(Dispatchers.IO) {
        service.developmentKeys("jwt=$token", projectId)
    }
}