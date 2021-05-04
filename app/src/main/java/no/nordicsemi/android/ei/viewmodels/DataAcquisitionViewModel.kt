package no.nordicsemi.android.ei.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
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
import no.nordicsemi.android.ei.model.Sample
import no.nordicsemi.android.ei.repository.ProjectDataRepository
import no.nordicsemi.android.ei.repository.ProjectRepository
import no.nordicsemi.android.ei.viewmodels.event.Error
import no.nordicsemi.android.ei.viewmodels.event.Event
import javax.inject.Inject

@HiltViewModel
class DataAcquisitionViewModel @Inject constructor(
    private val userManager: UserManager,
    private val projectRepository: ProjectRepository
) : ViewModel() {

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

    var isRefreshing: Boolean by mutableStateOf(false)
        private set
    var samples: List<Sample> by mutableStateOf(listOf())
        private set
    var pageCount: Int = pageCount()
        private set

    val focusRequester = FocusRequester()
    var selectedDevice: Device? by mutableStateOf(null)
        private set
    var label: String by mutableStateOf("")
        private set
    var selectedSensor: Device.Sensor? by mutableStateOf(null)
        private set
    var selectedFrequency: Number? by mutableStateOf(null)
        private set

    init {
        listSamples()
    }

    fun listSamples() {
        val handler = CoroutineExceptionHandler { _, throwable ->
            viewModelScope.launch {
                eventChannel.send(Error(throwable = throwable)).also { isRefreshing = false }
            }
        }
        viewModelScope.launch(handler) {
            projectRepository.listSamples(
                projectId = projectDataRepository.project.id,
                keys = projectDataRepository.developmentKeys
            ).let { response ->
                when (response.success) {
                    true -> samples = response.samples
                    false -> eventChannel.send(Error(throwable = Throwable("Unknown error")))
                }.also { isRefreshing = false }
            }
        }
    }

    fun onDeviceSelected(device: Device) {
        selectedDevice = device
        device.sensors.takeIf { sensors -> sensors.isNotEmpty() }
            ?.let { sensors -> onSensorSelected(sensor = sensors[0]) }
    }

    fun onLabelChanged(label: String) {
        this.label = label
    }

    fun onSensorSelected(sensor: Device.Sensor) {
        this.selectedSensor = sensor
        sensor.frequencies
            .takeIf { frequencies ->
                frequencies.isNotEmpty()
            }?.let { frequencies ->
                onFrequencySelected(frequency = frequencies[0])
            } ?: run { selectedFrequency = null }
    }

    fun onFrequencySelected(frequency: Number) {
        this.selectedFrequency = frequency
    }

    private fun pageCount(): Int = when (samples.size % 10) {
        0 -> samples.size / 10
        else -> samples.size % 10
    }
}