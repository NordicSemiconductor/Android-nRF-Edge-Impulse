package no.nordicsemi.android.ei.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.EntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.di.UserComponentEntryPoint
import no.nordicsemi.android.ei.di.UserManager
import no.nordicsemi.android.ei.model.DevelopmentKeys
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.repository.ProjectsRepository
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val repo: ProjectsRepository,
    userManager: UserManager
) : ViewModel() {

    private val token =
        EntryPoints.get(userManager.userComponent!!, UserComponentEntryPoint::class.java)
            .userDataRepository().token

    private val _projects = MutableLiveData(listOf<Project>())
    val projects: LiveData<List<Project>> = _projects

    private val _pullToRefresh = MutableLiveData(false)
    val pullToRefresh: LiveData<Boolean> = _pullToRefresh

    private val _developmentKeys = MutableLiveData(DevelopmentKeys())
    val developmentKeys: LiveData<DevelopmentKeys> = _developmentKeys

    init {
        loadProjects()
    }

    /**
     * Loads projects from the Edge Impulse Studio.
     *
     * @param withPullToRefresh True if pull to refresh was used to load projects.
     */
    fun loadProjects(withPullToRefresh: Boolean = false) {
        if (withPullToRefresh) {
            _pullToRefresh.value = withPullToRefresh
        }

        viewModelScope.launch {
            repo.projects(token = token).run {
                if (withPullToRefresh)
                    _pullToRefresh.postValue(false)
                Log.i("AA", toString())
                this.takeIf {
                    it.success
                }?.apply {
                    _projects.value = projects
                }
            }
        }
    }

    /**
     * Gets the development keys for a given Project Id
     */
    fun getDevelopmentKeys(projectId: Int) {
        viewModelScope.launch {
            repo.developmentKeys(token = token, projectId = projectId).run {
                Log.i("AA", toString())
                this.takeIf {
                    it.success
                }?.apply {
                    _developmentKeys.value = DevelopmentKeys(apiKey, hmacKey)
                }
            }
        }
    }
}