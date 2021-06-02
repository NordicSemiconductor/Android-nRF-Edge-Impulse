package no.nordicsemi.android.ei.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.EntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val project = projectDataRepository.project
    private val keys = projectDataRepository.developmentKeys

    val trainingSamples =
        Pager(PagingConfig(pageSize = PAGE_SIZE, prefetchDistance = 2)) {
            SamplePagingSource(
                project,
                keys,
                context.getString(Training.category),
                projectRepository
            )
        }.flow.cachedIn(viewModelScope).catch {
            e -> eventChannel.send(element = Error(Throwable(e)))
        }

    var testingSamples =
        Pager(PagingConfig(pageSize = PAGE_SIZE, prefetchDistance = 2)) {
            SamplePagingSource(
                project,
                keys,
                context.getString(Testing.category),
                projectRepository
            )
        }.flow.cachedIn(viewModelScope).catch {
            e -> eventChannel.send(element = Error(Throwable(e)))
        }

    companion object {
        const val PAGE_SIZE = 50
    }
}