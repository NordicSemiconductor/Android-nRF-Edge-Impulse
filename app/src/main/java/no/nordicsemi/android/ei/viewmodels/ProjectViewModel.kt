package no.nordicsemi.android.ei.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.EntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.di.ProjectComponentEntryPoint
import no.nordicsemi.android.ei.di.ProjectManager
import no.nordicsemi.android.ei.di.UserComponentEntryPoint
import no.nordicsemi.android.ei.di.UserManager
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.repository.ProjectDataRepository
import no.nordicsemi.android.ei.repository.ProjectRepository
import no.nordicsemi.android.ei.repository.UserDataRepository
import no.nordicsemi.android.ei.viewmodels.event.Error
import no.nordicsemi.android.ei.viewmodels.event.Event
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val userManager: UserManager,
    private val projectRepository: ProjectRepository
) : ViewModel() {
    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    var isRefreshing: Boolean by mutableStateOf(false)
        private set

    private val userComponentEntryPoint: UserComponentEntryPoint
        get() = EntryPoints.get(userManager.userComponent!!, UserComponentEntryPoint::class.java)

    private val userDataRepository: UserDataRepository
        get() = userComponentEntryPoint.userDataRepository()

    private val projectManager: ProjectManager
        get() = userComponentEntryPoint.getProjectManager()

    private val projectDataRepository: ProjectDataRepository
        get() = EntryPoints
            .get(projectManager.projectComponent!!, ProjectComponentEntryPoint::class.java)
            .projectDataRepository()


    var configuredDevices: List<Device> by mutableStateOf(listOf())
        private set

    val project
        get() = projectDataRepository.project

    init {
        listDevices()
    }

    fun listDevices(swipedToRefresh: Boolean = false) {
        if (swipedToRefresh)
            isRefreshing = true
        val handler = CoroutineExceptionHandler { _, throwable ->
            viewModelScope.launch {
                eventChannel.send(Error(throwable = throwable)).also { isRefreshing = false }
            }
        }
        viewModelScope.launch(handler) {
            projectRepository.listDevices(
                projectId = projectDataRepository.project.id,
                keys = projectDataRepository.developmentKeys
            ).let { response ->
                when (response.success) {
                    true -> configuredDevices = response.devices
                    false -> eventChannel.send(Error(throwable = Throwable(response.error)))
                }.also { isRefreshing = false }
            }
        }
    }
}