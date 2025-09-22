/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.viewmodels

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.EntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.account.AccountHelper
import no.nordicsemi.android.ei.di.ProjectManager
import no.nordicsemi.android.ei.di.UserComponentEntryPoint
import no.nordicsemi.android.ei.di.UserManager
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.model.User
import no.nordicsemi.android.ei.repository.DashboardRepository
import no.nordicsemi.android.ei.repository.UserDataRepository
import no.nordicsemi.android.ei.service.param.ProjectVisibility
import no.nordicsemi.android.ei.service.param.developmentKeys
import no.nordicsemi.android.ei.util.guard
import no.nordicsemi.android.ei.viewmodels.event.Event
import no.nordicsemi.android.ei.viewmodels.event.Event.Error
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val userManager: UserManager,
    private val dashboardRepository: DashboardRepository,
) : AndroidViewModel(context as Application) {
    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    // User is kept outside of refresh state, as it is available also when refreshing.
    var user: User? by mutableStateOf(userDataRepo?.user)
        private set

    var isRefreshing: Boolean by mutableStateOf(false)
        private set

    var isDownloadingDevelopmentKeys: Boolean by mutableStateOf(false)
        private set

    private val userDataRepo: UserDataRepository?
        get() = userManager.userComponent?.let {
            EntryPoints
                .get(it, UserComponentEntryPoint::class.java)
                .userDataRepository()
        }

    // TODO This needs to be fixed: Possible NPE when switching back to the app.
    private val projectManager: ProjectManager
        get() = EntryPoints
            .get(userManager.userComponent!!, UserComponentEntryPoint::class.java)
            .getProjectManager()

    fun refreshUser() {
        isRefreshing = true
        val handler = CoroutineExceptionHandler { _, throwable ->
            viewModelScope
                .launch { eventChannel.send(Error(throwable)) }
                .also { isRefreshing = false }
        }
        viewModelScope.launch(handler) {
            dashboardRepository
                .getCurrentUser(userDataRepo!!.token)
                .let { response ->
                    guard(response.success) {
                        throw Throwable(response.error)
                    }
                    userManager.userLoggedIn(response, userDataRepo!!.token)
                    user = response
                }
                .also { isRefreshing = false }
        }
    }

    fun createProject(
        projectName: String,
        projectVisibility: ProjectVisibility = ProjectVisibility.PRIVATE
    ) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            viewModelScope.launch {
                eventChannel.send(Error(throwable))
            }
        }
        viewModelScope.launch(handler) {
            dashboardRepository
                .createProject(
                    token = userDataRepo!!.token,
                    projectName = projectName,
                    projectVisibility = projectVisibility
                ).let { response ->
                    guard(response.success) {
                        throw Throwable(response.error)
                    }
                    eventChannel.send(Event.Project.Created(projectName))
                    refreshUser()
                }
        }
    }

    fun selectProject(project: Project) {
        isDownloadingDevelopmentKeys = true
        val handler = CoroutineExceptionHandler { _, throwable ->
            viewModelScope
                .launch { eventChannel.send(Error(throwable)) }
                .also { isDownloadingDevelopmentKeys = false }
        }
        viewModelScope.launch(handler) {
            // Retrieve the development keys for the project
            val developmentKeys = dashboardRepository.developmentKeys(
                token = userDataRepo!!.token,
                projectId = project.id
            ).let { response ->
                guard(response.success) {
                    throw Throwable(response.error)
                }
                response.developmentKeys()
            }
            // Retrieve the socket token for the project
            val socketToken = dashboardRepository
                .getSocketToken(developmentKeys.apiKey, projectId = project.id)
                .let { response ->
                    guard(response.success) {
                        throw Throwable(response.error)
                    }
                    response.token
                }
            projectManager.projectSelected(
                project = project,
                keys = developmentKeys,
                socketToken = socketToken
            )
            eventChannel.send(Event.Project.Selected(project))
            isDownloadingDevelopmentKeys = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            handleLogout()
        }
    }

    private fun handleLogout() {
        userDataRepo?.let {
            AccountHelper.invalidateAuthToken(it.token, getApplication())
        }
        userManager.logout()
    }
}