package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.di.DefaultDispatcher
import no.nordicsemi.android.ei.model.DevelopmentKeys
import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.websocket.WebSocketManager
import javax.inject.Inject

class ProjectRepository @Inject constructor(
    private val service: EiService,
    val webSocketManager: WebSocketManager,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    suspend fun listDevices(projectId: Int, keys: DevelopmentKeys) =
        withContext(defaultDispatcher) {
            service.listDevices(apiKey = keys.apiKey, projectId = projectId)
        }

    suspend fun listSamples(
        projectId: Int,
        keys: DevelopmentKeys,
        category: String,
        offset: Int,
        limit: Int
    ) = withContext(defaultDispatcher) {
        service.listSamples(
            apiKey = keys.apiKey,
            projectId = projectId,
            category = category,
            offset = offset,
            limit = limit
        )
    }
}