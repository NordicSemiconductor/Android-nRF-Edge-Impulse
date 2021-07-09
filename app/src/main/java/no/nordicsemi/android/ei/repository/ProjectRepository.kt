package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.di.IODispatcher
import no.nordicsemi.android.ei.model.DevelopmentKeys
import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.service.param.BuildOnDeviceModelRequest
import no.nordicsemi.android.ei.util.Engine
import no.nordicsemi.android.ei.util.ModelType
import javax.inject.Inject

class ProjectRepository @Inject constructor(
    private val service: EiService,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun listDevices(projectId: Int, keys: DevelopmentKeys) =
        withContext(ioDispatcher) {
            service.listDevices(apiKey = keys.apiKey, projectId = projectId)
        }

    suspend fun listSamples(
        projectId: Int,
        keys: DevelopmentKeys,
        category: String,
        offset: Int,
        limit: Int
    ) = withContext(ioDispatcher) {
        service.listSamples(
            apiKey = keys.apiKey,
            projectId = projectId,
            category = category,
            offset = offset,
            limit = limit
        )
    }

    suspend fun buildOnDeviceModels(
        projectId: Int,
        keys: DevelopmentKeys,
        engine: Engine,
        modelType: ModelType
    ) = withContext(ioDispatcher) {
        service.buildOnDevice(
            apiKey = keys.apiKey,
            projectId = projectId,
            buildOnDeviceModels = BuildOnDeviceModelRequest(
                engine = engine.engine,
                modelType = modelType.modelType
            )
        )
    }
}