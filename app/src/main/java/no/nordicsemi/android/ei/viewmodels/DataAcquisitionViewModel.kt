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
import no.nordicsemi.android.ei.HorizontalPagerTab
import no.nordicsemi.android.ei.HorizontalPagerTab.*
import no.nordicsemi.android.ei.di.ProjectComponentEntryPoint
import no.nordicsemi.android.ei.di.ProjectManager
import no.nordicsemi.android.ei.di.UserComponentEntryPoint
import no.nordicsemi.android.ei.di.UserManager
import no.nordicsemi.android.ei.model.Sample
import no.nordicsemi.android.ei.repository.ProjectDataRepository
import no.nordicsemi.android.ei.repository.ProjectRepository
import no.nordicsemi.android.ei.viewmodels.event.Error
import no.nordicsemi.android.ei.viewmodels.event.Event
import javax.inject.Inject

@HiltViewModel
class DataAcquisitionViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val userManager: UserManager,
    private val projectRepository: ProjectRepository
) : AndroidViewModel(context as Application) {

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    private val projectManager: ProjectManager
        get() = EntryPoints
            .get(userManager.userComponent!!, UserComponentEntryPoint::class.java)
            .getProjectManager()

    private val projectDataRepository: ProjectDataRepository
        get() = EntryPoints
            .get(projectManager.projectComponent!!, ProjectComponentEntryPoint::class.java)
            .projectDataRepository()

    var isRefreshingTrainingData: Boolean by mutableStateOf(false)
        private set
    var isRefreshingTestData: Boolean by mutableStateOf(false)
        private set
    var isRefreshingAnomalyData: Boolean by mutableStateOf(false)
        private set
    var trainingSamples: List<Sample> by mutableStateOf(listOf())
        private set
    var testingSamples: List<Sample> by mutableStateOf(listOf())
        private set
    var anomalySamples: List<Sample> by mutableStateOf(listOf())
        private set

    init {
        listSamples(Training)
        listSamples(Testing)
        listSamples(Anomaly)
    }

    private fun listSamples(pagerTab: HorizontalPagerTab, swipedToRefresh: Boolean = false) {
        val context = getApplication() as Context
        val category = when (pagerTab) {
            is Training -> {
                isRefreshingTrainingData = swipedToRefresh
                context.getString(pagerTab.category)
            }
            is Testing -> {
                isRefreshingTestData = swipedToRefresh
                context.getString(pagerTab.category)
            }
            is Anomaly -> {
                isRefreshingAnomalyData = swipedToRefresh
                context.getString(pagerTab.category)
            }
        }
        val handler = CoroutineExceptionHandler { _, throwable ->
            viewModelScope.launch {
                eventChannel.send(Error(throwable = throwable))
                    .also { isRefreshingTrainingData = false }
            }
        }
        viewModelScope.launch(handler) {
            projectRepository.listSamples(
                projectId = projectDataRepository.project.id,
                keys = projectDataRepository.developmentKeys,
                category = category
            ).let { response ->
                when (response.success) {
                    true -> when (pagerTab) {
                        is Training -> trainingSamples = response.samples
                        is Testing -> testingSamples = response.samples
                        is Anomaly -> anomalySamples = response.samples
                    }
                    false -> throw Throwable("Unknown error")
                }.also {
                    when (pagerTab) {
                        is Training -> {
                            isRefreshingTrainingData = false
                        }
                        is Testing -> {
                            isRefreshingTestData = false
                        }
                        is Anomaly -> {
                            isRefreshingAnomalyData = false
                        }
                    }
                }
            }
        }
    }
}