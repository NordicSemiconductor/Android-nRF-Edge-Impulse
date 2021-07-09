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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.di.*
import no.nordicsemi.android.ei.model.DevelopmentKeys
import no.nordicsemi.android.ei.model.User
import no.nordicsemi.android.ei.repository.ProjectDataRepository
import no.nordicsemi.android.ei.repository.ProjectRepository
import no.nordicsemi.android.ei.repository.UserDataRepository
import no.nordicsemi.android.ei.util.Engine
import no.nordicsemi.android.ei.util.ModelType
import no.nordicsemi.android.ei.viewmodels.event.Event
import javax.inject.Inject

@HiltViewModel
class DeploymentViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val userManager: UserManager,
    private val projectRepository: ProjectRepository,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : AndroidViewModel(context as Application) {
    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    // User is kept outside of refresh state, as it is available also when refreshing.
    var user: User by mutableStateOf(userDataRepo.user)
        private set

    /** The project associated with the View Model. */
    val project
        get() = projectDataRepository.project

    /** Project development keys. */
    private val keys
        get() = projectDataRepository.developmentKeys

    /** Socket token for deployment */
    private val socketToken
        get() = projectDataRepository.socketToken

    // TODO This needs to be fixed: Possible NPE when switching back to the app.
    private val userDataRepo: UserDataRepository
        get() = EntryPoints
            .get(userManager.userComponent!!, UserComponentEntryPoint::class.java)
            .userDataRepository()

    // TODO This needs to be fixed: Possible NPE when switching back to the app.
    private val projectManager: ProjectManager
        get() = EntryPoints
            .get(userManager.userComponent!!, UserComponentEntryPoint::class.java)
            .getProjectManager()

    // TODO This needs to be fixed: Possible NPE when switching back to the app.
    private val projectDataRepository: ProjectDataRepository
        get() = EntryPoints
            .get(projectManager.projectComponent!!, ProjectComponentEntryPoint::class.java)
            .projectDataRepository()

    init {
        buildOnDeviceModel(
            project.id,
            keys = keys,
            engine = Engine.TFLITE_EON,
            modelType = ModelType.INT_8
        )
    }

    fun buildOnDeviceModel(
        projectId: Int,
        keys: DevelopmentKeys,
        engine: Engine,
        modelType: ModelType
    ) {
        viewModelScope.launch {
            delay(8000)
            projectRepository.buildOnDeviceModels(
                projectId = projectId,
                keys = keys,
                engine = engine,
                modelType = modelType
            )
        }
    }
}