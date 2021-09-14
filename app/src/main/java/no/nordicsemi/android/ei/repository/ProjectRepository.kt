package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.di.IODispatcher
import no.nordicsemi.android.ei.model.Category
import no.nordicsemi.android.ei.model.DevelopmentKeys
import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.service.param.BuildOnDeviceModelRequest
import no.nordicsemi.android.ei.service.param.RenameDeviceRequest
import no.nordicsemi.android.ei.service.param.StartSamplingRequest
import no.nordicsemi.android.ei.util.Engine
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

    suspend fun startSampling(
        keys: DevelopmentKeys,
        projectId: Int,
        deviceId: String,
        label: String,
        lengthMs: Number,
        category: Category,
        intervalMs: Float,
        sensor: String
    ) = withContext(ioDispatcher) {
        service.startSampling(
            apiKey = keys.apiKey,
            projectId = projectId,
            deviceId = deviceId,
            startSamplingRequest = StartSamplingRequest(
                label = label,
                lengthMs = lengthMs,
                category = category.type,
                intervalMs = intervalMs,
                sensor = sensor
            )
        )
    }

    suspend fun buildOnDeviceModels(
        projectId: Int,
        keys: DevelopmentKeys,
        engine: Engine
    ) = withContext(ioDispatcher) {
        service.buildOnDevice(
            apiKey = keys.apiKey,
            projectId = projectId,
            buildOnDeviceModels = BuildOnDeviceModelRequest(
                engine = engine.engine
            )
        )
    }

    suspend fun deploymentInfo(
        projectId: Int,
        keys: DevelopmentKeys
    ) = withContext(ioDispatcher) {
        service.deploymentInfo(
            apiKey = keys.apiKey,
            projectId = projectId
        )
    }

     suspend fun downloadBuild(
        projectId: Int,
        keys: DevelopmentKeys
    ) = withContext(ioDispatcher) {
        service.downloadBuild(
            apiKey = keys.apiKey,
            projectId = projectId
        )
    }

    suspend fun renameDevice(
        apiKey: String,
        projectId: Int,
        deviceId: String,
        name: String
    ) = withContext(ioDispatcher) {
        service.renameDevice(
            apiKey = apiKey,
            projectId = projectId,
            /*Encode thr url manually as retrofit does not correctly encode https://github.com/square/retrofit/issues/3080*/
            deviceId = deviceId.replace(":", "%3A"),
            renameDeviceRequest = RenameDeviceRequest(name = name)
        )
    }

    suspend fun deleteDevice(
        apiKey: String,
        projectId: Int,
        deviceId: String
    ) = withContext(ioDispatcher) {
        service.deleteDevice(
            apiKey = apiKey,
            projectId = projectId,
            /*Encode thr url manually as retrofit does not correctly encode https://github.com/square/retrofit/issues/3080*/
            deviceId = deviceId.replace(":", "%3A")
        )
    }
}