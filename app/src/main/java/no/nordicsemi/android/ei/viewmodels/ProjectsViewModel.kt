package no.nordicsemi.android.ei.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.repository.ProjectsRepository
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val repo: ProjectsRepository
) : ViewModel() {

    private val _projects = MutableLiveData<List<Project>>()
    val projects: LiveData<List<Project>> = _projects

    @ExperimentalCoroutinesApi
    fun projects(token: String) {
        viewModelScope.launch {
            val projectResponse = repo.projects(token = token)
            Log.i("AA", projectResponse.toString())
            projectResponse.takeIf {
                it.success
            }?.apply {
                _projects.value = this.projects
            }
        }
    }
}