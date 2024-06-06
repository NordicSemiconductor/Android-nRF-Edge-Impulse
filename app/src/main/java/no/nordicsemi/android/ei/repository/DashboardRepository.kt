/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.di.IODispatcher
import no.nordicsemi.android.ei.service.EiService
import no.nordicsemi.android.ei.service.param.CreateProjectRequest
import no.nordicsemi.android.ei.service.param.DeleteCurrentUserRequest
import no.nordicsemi.android.ei.service.param.ProjectVisibility
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    service: EiService,
    @IODispatcher ioDispatcher: CoroutineDispatcher
) : BaseRepository(service = service, ioDispatcher = ioDispatcher) {

    suspend fun createProject(token: String, projectName: String, projectVisibility: ProjectVisibility = ProjectVisibility.PRIVATE) = withContext(ioDispatcher) {
        service.createProject(jwt = token, createProjectRequest = CreateProjectRequest(projectName, projectVisibility.value))
    }

    suspend fun developmentKeys(token: String, projectId: Int) = withContext(ioDispatcher) {
        service.developmentKeys(jwt = token, projectId = projectId)
    }

    suspend fun getSocketToken(apiKey: String, projectId: Int) = withContext(ioDispatcher) {
        service.getSocketToken(apiKey = apiKey, projectId = projectId)
    }

    suspend fun deleteCurrentUser(token: String, password: String, code:String?) = withContext(ioDispatcher) {
        service.deleteCurrentUser(jwt = token, deleteCurrentUserRequest = DeleteCurrentUserRequest(password, code))
    }
}