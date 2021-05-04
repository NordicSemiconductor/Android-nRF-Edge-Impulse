package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.di.DefaultDispatcher
import no.nordicsemi.android.ei.model.DevelopmentKeys
import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.service.param.ListDevicesResponse
import no.nordicsemi.android.ei.service.param.ListSamplesResponse
import javax.inject.Inject

class ProjectRepository @Inject constructor(
    private val service: EiService,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    suspend fun listDevices(projectId: Int, keys: DevelopmentKeys): ListDevicesResponse =
        withContext(defaultDispatcher) {
            service.listDevices(projectId = projectId, apiKey = keys.apiKey)
        }

    suspend fun listSamples(projectId: Int, keys: DevelopmentKeys): ListSamplesResponse =
        withContext(Dispatchers.IO) {
            service.listSamples(projectId = projectId, apiKey = keys.apiKey)
        }
}