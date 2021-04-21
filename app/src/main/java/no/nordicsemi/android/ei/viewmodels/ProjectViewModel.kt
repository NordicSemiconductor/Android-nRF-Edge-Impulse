package no.nordicsemi.android.ei.viewmodels

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
import no.nordicsemi.android.ei.model.DevelopmentKeys
import no.nordicsemi.android.ei.repository.ProjectDataRepository
import no.nordicsemi.android.ei.repository.ProjectRepository
import no.nordicsemi.android.ei.repository.UserDataRepository
import no.nordicsemi.android.ei.viewmodels.event.Event
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val userManager: UserManager,
    private val projectRepository: ProjectRepository
) : ViewModel() {
    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    private val userDataRepository: UserDataRepository
        get() = EntryPoints
            .get(userManager.userComponent!!, UserComponentEntryPoint::class.java)
            .userDataRepository()

    private val projectManager: ProjectManager
        get() = EntryPoints
            .get(userManager.userComponent!!, UserComponentEntryPoint::class.java)
            .getProjectManager()

    private val projectDataRepository: ProjectDataRepository
        get() = EntryPoints
            .get(projectManager.projectComponent!!, ProjectComponentEntryPoint::class.java)
            .projectDataRepository()

    val project
        get() = projectDataRepository.project

    lateinit var developmentKeys: DevelopmentKeys

    init {
        getDevelopmentKeys(projectId = project.id)
    }


    /**
     * Gets the development keys for a given Project Id
     */
    private fun getDevelopmentKeys(projectId: Int) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            viewModelScope
                .launch { eventChannel.send(Event.Error(throwable)) }
        }
        viewModelScope.launch(handler) {
            projectRepository.developmentKeys(userDataRepository.token, projectId).let { response ->
                when (response.success) {
                    true -> developmentKeys =
                        DevelopmentKeys(apiKey = response.apiKey, hmacKey = response.hmacKey)
                    false -> eventChannel.send(Event.Error(Throwable(response.error)))
                }
            }
        }
    }
}