package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.di.DefaultDispatcher
import no.nordicsemi.android.ei.model.DevelopmentKeys
import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.service.param.ListDevicesResponse
import javax.inject.Inject

class ProjectRepository @Inject constructor(
    private val service: EiService,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    suspend fun listDevices(projectId: Int, keys: DevelopmentKeys): ListDevicesResponse =
        withContext(defaultDispatcher) {
            service.listDevices(projectId = projectId, apiKey = keys.apiKey)
        }
}