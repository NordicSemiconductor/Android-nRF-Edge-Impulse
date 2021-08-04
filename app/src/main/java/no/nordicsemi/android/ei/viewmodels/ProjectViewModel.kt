package no.nordicsemi.android.ei.viewmodels

import android.app.Application
import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.EntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.ble.DiscoveredBluetoothDevice
import no.nordicsemi.android.ei.comms.CommsManager
import no.nordicsemi.android.ei.comms.DeploymentManager
import no.nordicsemi.android.ei.di.*
import no.nordicsemi.android.ei.model.BuildLog
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.Sensor
import no.nordicsemi.android.ei.repository.ProjectDataRepository
import no.nordicsemi.android.ei.repository.ProjectRepository
import no.nordicsemi.android.ei.repository.UserDataRepository
import no.nordicsemi.android.ei.util.Engine
import no.nordicsemi.android.ei.util.ModelType
import no.nordicsemi.android.ei.util.guard
import no.nordicsemi.android.ei.viewmodels.event.Event
import no.nordicsemi.android.ei.viewmodels.state.DeviceState
import okhttp3.OkHttpClient
import okhttp3.internal.filterList
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val userManager: UserManager,
    private val projectRepository: ProjectRepository,
    private val client: OkHttpClient,
    private val gson: Gson,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : AndroidViewModel(context as Application) {

    /** The channel for emitting one-time events. */
    private val eventChannel = Channel<Event>(Channel.BUFFERED)

    /** The flow that emits events. */
    val eventFlow = eventChannel.receiveAsFlow()

    /** The project associated with the View Model. */
    val project
        get() = projectDataRepository.project

    /** Project development keys. */
    private val keys
        get() = projectDataRepository.developmentKeys

    /** A map of device managers. */
    var commsManagers = mutableStateMapOf<String, CommsManager>()
        private set

    /** A list of configured devices obtained from the service. */
    var configuredDevices = mutableStateListOf<Device>()
        private set

    /** A list of connected devices derived using the configuredDevices and the commsManagers. */
    var connectedDevices = derivedStateOf {
        configuredDevices.filterList {
            commsManagers[deviceId]?.state == DeviceState.AUTHENTICATED
        }
    }
        private set

    /** Whether the list of configured devices is refreshing. */
    var isRefreshing: Boolean by mutableStateOf(false)
        private set

    // TODO This needs to be fixed: NPE when switching back to the app.
    private val userComponentEntryPoint: UserComponentEntryPoint
        get() = EntryPoints.get(userManager.userComponent!!, UserComponentEntryPoint::class.java)

    private val userDataRepository: UserDataRepository
        get() = userComponentEntryPoint.userDataRepository()

    private val projectManager: ProjectManager
        get() = userComponentEntryPoint.getProjectManager()

    // TODO This needs to be fixed: Possible NPE when switching back to the app.
    private val projectDataRepository: ProjectDataRepository
        get() = EntryPoints
            .get(projectManager.projectComponent!!, ProjectComponentEntryPoint::class.java)
            .projectDataRepository()

    // ---- Fields used for Recording New Sample --------------
    val focusRequester = FocusRequester()
    var selectedDevice: Device? by mutableStateOf(null)
        private set
    var label: String by mutableStateOf("")
        private set
    var selectedSensor: Sensor? by mutableStateOf(null)
        private set
    var sampleLength by mutableStateOf(10000)
        //TODO fix hardcoded sample length
        private set
    var selectedFrequency: Number? by mutableStateOf(null)
        private set
    var logs = mutableStateListOf<BuildLog>()
        private set

    /** Whether a build is in progress. */
    var isBuilding: Boolean by mutableStateOf(false)
        private set

    // ---- Implementation ------------------------------------
    init {
        // When the view model is created, load the configured devices from the service.
        listDevices(swipedToRefresh = false)
    }

    /**
     * Lists configured devices from the service.
     *
     * Calling this method will display the swipe to refresh indicator.
     */
    fun listDevices() {
        listDevices(swipedToRefresh = true)
    }

    /**
     * Lists configured devices from the service.
     * @param swipedToRefresh Should the refresh indicator be displayed.
     */
    private fun listDevices(swipedToRefresh: Boolean = false) {
        if (swipedToRefresh)
            isRefreshing = true
        val handler = CoroutineExceptionHandler { _, throwable ->
            viewModelScope.launch {
                eventChannel
                    .send(Event.Error(throwable = throwable))
                    .also { isRefreshing = false }
            }
        }
        viewModelScope.launch(handler) {
            projectRepository.listDevices(
                projectId = projectDataRepository.project.id,
                keys = projectDataRepository.developmentKeys
            ).let { response ->
                guard(response.success) {
                    throw Throwable(response.error)
                }
                // If the user decides to delete a device from the web while being connected to i t from the phone,
                // We should disconnect from that device
                configuredDevices.filter { !response.devices.contains(it) }.onEach { device ->
                    commsManagers.remove(device.deviceId)?.apply {
                        state.takeIf {
                            it == DeviceState.CONNECTING ||
                                    it == DeviceState.AUTHENTICATING ||
                                    it == DeviceState.AUTHENTICATED
                        }?.apply {
                            disconnect()
                        }
                    }
                }
                configuredDevices.apply {
                    clear()
                    addAll(response.devices)
                }
            }.also { isRefreshing = false }
        }
    }

    fun onDeviceSelected(device: Device) {
        selectedDevice = device
        device.sensors.firstOrNull()
            ?.let { onSensorSelected(sensor = it) }
    }

    fun onLabelChanged(label: String) {
        this.label = label
    }

    fun onSensorSelected(sensor: Sensor) {
        this.selectedSensor = sensor
        sensor.frequencies.firstOrNull()
            ?.let { onFrequencySelected(frequency = it) }
            ?: run { selectedFrequency = null }
    }

    fun onSampleLengthChanged(sampleLength: Int) {
        this.sampleLength = sampleLength
    }

    fun onFrequencySelected(frequency: Number) {
        this.selectedFrequency = frequency
    }

    //TODO need to finalize the api
    fun connect(device: DiscoveredBluetoothDevice): Unit =
        commsManagers.getOrPut(key = device.deviceId, defaultValue = {
            CommsManager(
                scope = viewModelScope,
                gson = gson,
                developmentKeys = keys,
                device = device,
                context = getApplication(),
                client = client
            )
        }).run {
            connect()
        }

    fun disconnect(device: DiscoveredBluetoothDevice) {
        commsManagers[device.deviceId]?.disconnect()
        commsManagers.remove(device.deviceId)
    }

    fun buildOnDeviceModel(
        engine: Engine,
        modelType: ModelType
    ) {
        isBuilding = true
        val handler = CoroutineExceptionHandler { _, throwable ->
            viewModelScope.launch {
                eventChannel
                    .send(Event.Error(throwable = throwable))
                    .also { isBuilding = false }
            }
        }
        viewModelScope.launch(handler) {
            projectRepository.buildOnDeviceModels(
                projectId = project.id,
                keys = keys,
                engine = engine,
                modelType = modelType
            ).let { response ->
                guard(response.success) {
                    throw Throwable(response.error)
                }
                logs.clear()
                DeploymentManager(
                    scope = viewModelScope,
                    gson = gson,
                    jobId = response.id,
                    socketToken = projectDataRepository.socketToken,
                    client = client
                ).logsAsFlow().collect {
                    logs.add(it)
                    if (it is BuildLog.Finished) {
                        isBuilding = false
                    }
                }
            }
        }
    }
}

