package no.nordicsemi.android.ei.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.EntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.di.ProjectComponentEntryPoint
import no.nordicsemi.android.ei.di.ProjectManager
import no.nordicsemi.android.ei.di.UserComponentEntryPoint
import no.nordicsemi.android.ei.di.UserManager
import no.nordicsemi.android.ei.repository.ProjectDataRepository
import no.nordicsemi.android.ei.repository.ProjectRepository
import no.nordicsemi.android.ei.repository.UserDataRepository
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val userManager: UserManager,
    private val projectRepository: ProjectRepository
) : ViewModel() {

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


    /**
     * Gets the development keys for a given Project Id
     */
    fun getDevelopmentKeys(projectId: Int) {
        // TODO: Handle No Internet and other exceptions
        viewModelScope.launch {
            projectRepository.developmentKeys(userDataRepository.token, projectId).let { response ->
                response.takeIf { it.success }?.apply {
                    // TODO: Do something

                }
            }
        }
    }
}