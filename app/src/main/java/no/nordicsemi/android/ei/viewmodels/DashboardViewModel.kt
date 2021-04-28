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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.account.AccountHelper
import no.nordicsemi.android.ei.di.ProjectManager
import no.nordicsemi.android.ei.di.UserComponentEntryPoint
import no.nordicsemi.android.ei.di.UserManager
import no.nordicsemi.android.ei.model.DevelopmentKeys
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.model.User
import no.nordicsemi.android.ei.repository.DashboardRepository
import no.nordicsemi.android.ei.repository.UserDataRepository
import no.nordicsemi.android.ei.viewmodels.event.Error
import no.nordicsemi.android.ei.viewmodels.event.Event
import no.nordicsemi.android.ei.viewmodels.event.ProjectCreated
import no.nordicsemi.android.ei.viewmodels.event.ProjectSelected
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
    var user: User by mutableStateOf(userDataRepo.user)
        private set

    var isRefreshing: Boolean by mutableStateOf(false)
        private set

    var isDownloadingDevelopmentKeys: Boolean by mutableStateOf(false)
        private set

    private val userDataRepo: UserDataRepository
        get() = EntryPoints
            .get(userManager.userComponent!!, UserComponentEntryPoint::class.java)
            .userDataRepository()

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
                .getCurrentUser(userDataRepo.token)
                .apply {
                    when (success) {
                        true -> {
                            userManager.userLoggedIn(this, userDataRepo.token)
                            user = this
                        }
                        false -> throw Throwable(error)
                    }
                }
                .also { isRefreshing = false }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun createProject(projectName: String) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            viewModelScope.launch { eventChannel.send(Error(throwable)) }
        }
        viewModelScope.launch(handler) {
            dashboardRepository
                .createProject(userDataRepo.token, projectName)
                .apply {
                    when(success){
                        true -> eventChannel.send(ProjectCreated(projectName = projectName))
                        false -> throw Throwable(error)
                    }
                }
        }
    }

    fun selectProject(project: Project) {
        getDevelopmentKeys(project = project)
    }

    private fun getDevelopmentKeys(project: Project) {
        isDownloadingDevelopmentKeys = true
        val handler = CoroutineExceptionHandler { _, throwable ->
            viewModelScope
                .launch { eventChannel.send(Error(throwable)) }
                .also { isDownloadingDevelopmentKeys = false }
        }
        viewModelScope.launch(handler) {
            dashboardRepository.developmentKeys(
                token = userDataRepo.token,
                projectId = project.id
            ).apply {
                when (success) {
                    true -> {
                        projectManager.projectSelected(
                            project = project,
                            keys = DevelopmentKeys(apiKey = apiKey, hmacKey = hmacKey)
                        )
                        eventChannel.send(ProjectSelected)
                    }
                    false -> throw Throwable(error)
                }
            }.also {
                isDownloadingDevelopmentKeys = false
            }
        }
    }

    fun logout() {
        AccountHelper.invalidateAuthToken(userDataRepo.token, getApplication())
        userManager.logout()
    }
}