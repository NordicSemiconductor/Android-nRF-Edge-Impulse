package no.nordicsemi.android.ei.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.EntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.di.UserComponentEntryPoint
import no.nordicsemi.android.ei.di.UserManager
import no.nordicsemi.android.ei.repository.ProjectRepository
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    userManager: UserManager
) : ViewModel() {

    private val repo =
        EntryPoints.get(userManager.userComponent!!, UserComponentEntryPoint::class.java)
            .userDataRepository()

    /**
     * Gets the development keys for a given Project Id
     */
    fun getDevelopmentKeys(projectId: Int) {
        // TODO: Handle No Internet and other exceptions
        viewModelScope.launch {
            projectRepository.developmentKeys(repo.token, projectId).let { response ->
                response.takeIf { it.success }?.apply {
                    // TODO: Do something

                }
            }
        }
    }
}