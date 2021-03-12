package no.nordicsemi.android.ei.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.repository.ProjectsRepository
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val token: String,
    private val repo: ProjectsRepository
) : ViewModel() {

    private val _projects = MutableLiveData(listOf<Project>())
    val projects: LiveData<List<Project>> = _projects

    private val _pullToRefresh = MutableLiveData(false)
    val pullToRefresh: LiveData<Boolean> = _pullToRefresh

    init {
        loadProjects()
    }

    /**
     * Loads projects from the Edge Impulse Studio
     *
     * @param withPullToRefresh True if pull to refresh was used to load projects
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
}