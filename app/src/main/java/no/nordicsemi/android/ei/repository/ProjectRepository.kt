package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.service.EiService
import javax.inject.Inject

class ProjectRepository @Inject constructor(private val service: EiService) {

    suspend fun developmentKeys(token: String, projectId: Int) = withContext(Dispatchers.IO) {
        service.developmentKeys(token, projectId)
    }
}