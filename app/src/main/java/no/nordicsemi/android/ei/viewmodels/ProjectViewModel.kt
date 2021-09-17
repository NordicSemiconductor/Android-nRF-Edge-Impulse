package no.nordicsemi.android.ei.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.EntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.ble.DiscoveredBluetoothDevice
import no.nordicsemi.android.ei.comms.BuildManager
import no.nordicsemi.android.ei.comms.DataAcquisitionManager
import no.nordicsemi.android.ei.comms.DeploymentState
import no.nordicsemi.android.ei.di.ProjectComponentEntryPoint
import no.nordicsemi.android.ei.di.ProjectManager
import no.nordicsemi.android.ei.di.UserComponentEntryPoint
import no.nordicsemi.android.ei.di.UserManager
import no.nordicsemi.android.ei.model.Category
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.Message.Sample
import no.nordicsemi.android.ei.model.Sensor
import no.nordicsemi.android.ei.repository.ProjectDataRepository
import no.nordicsemi.android.ei.repository.ProjectRepository
import no.nordicsemi.android.ei.repository.UserDataRepository
import no.nordicsemi.android.ei.util.guard
import no.nordicsemi.android.ei.viewmodels.event.Event
import no.nordicsemi.android.ei.viewmodels.state.DeviceState
import okhttp3.OkHttpClient
import javax.inject.Inject


@HiltViewModel
class ProjectViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val userManager: UserManager,
    private val projectRepository: ProjectRepository,
    private val client: OkHttpClient,
    private val gson: Gson
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
    var dataAcquisitionManagers = mutableStateMapOf<String, DataAcquisitionManager>()
        private set

    /** A list of configured devices obtained from the service. */
    var configuredDevices = mutableStateListOf<Device>()
        private set

    /** Whether the list of configured devices is refreshing. */
    var isRefreshing by mutableStateOf(false)
        private set

    /** Whether a device rename is been requested. */
    var isDeviceRenameRequested: Boolean by mutableStateOf(false)
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
    var label: String by mutableStateOf("Sample")
        private set
    var selectedSensor: Sensor? by mutableStateOf(null)
        private set
    var sampleLength by mutableStateOf(20000)
        //TODO fix hardcoded sample length
        private set
    var selectedFrequency: Number? by mutableStateOf(null)
        private set

    /** Creates a deployment manager */
    var buildManager = BuildManager(
        scope = viewModelScope,
        gson = gson,
        exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            viewModelScope.launch {
                eventChannel
                    .send(Event.Error(throwable = throwable))
            }
        },
        socketToken = projectDataRepository.socketToken,
        client = client
    )
        private set

    var deploymentState: DeploymentState by mutableStateOf(DeploymentState.Unknown)
        private set

    var samplingState = derivedStateOf {
        selectedDevice?.let {
            dataAcquisitionManagers[it.deviceId]?.samplingState
        } ?: Sample.Unknown
    }
        private set

    // ---- Implementation ------------------------------------
    init {
        // When the view model is created, load the configured devices from the service.
        listDevices(swipedToRefresh = false)
        registerForBuildManager()

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
                // If the user decides to delete a device from the web while being connected to it from the phone,
                // We should disconnect from that device
                configuredDevices.filter { device ->
                    device.deviceId !in response.devices.map {
                        it.deviceId
                    }
                }.onEach {
                    dataAcquisitionManagers.disconnect(it.deviceId)
                }
                configuredDevices.apply {
                    clear()
                    addAll(response.devices)
                    // We need to re-assign the selected device name for record sample data screen
                    selectedDevice =
                        configuredDevices.find { it.deviceId == selectedDevice?.deviceId }
                }
            }.also { isRefreshing = false }
        }
    }

    private fun registerForBuildManager() {
        viewModelScope.launch {
            buildManager.buildStateAsFlow().collect {
                deploymentState = it
                when (it) {
                    is DeploymentState.Building.Started -> {
                        Log.d("AAAA", "Started")
                    }
                    is DeploymentState.Building.Finished -> {
                        downloadBuild()
                    }
                    is DeploymentState.Building.Error -> {
                        throw Throwable(it.reason ?: "Error while building firmware")
                    }
                    else -> {

                    }
                }
            }
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

    fun onFrequencySelected(frequency: Number) {
        this.selectedFrequency = frequency
    }

    fun onSampleLengthChanged(sampleLength: Int) {
        this.sampleLength = sampleLength
    }

    //TODO need to finalize the api
    fun connect(device: DiscoveredBluetoothDevice): Unit =
        dataAcquisitionManagers.getOrPut(key = device.deviceId, defaultValue = {
            DataAcquisitionManager(
                scope = viewModelScope,
                gson = gson,
                developmentKeys = keys,
                device = device,
                client = client,
                exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                    viewModelScope.launch {
                        eventChannel
                            .send(Event.Error(throwable = throwable))
                    }
                },
                context = getApplication()
            )
        }).run {
            connect()
        }

    fun disconnect(device: DiscoveredBluetoothDevice) {
        dataAcquisitionManagers.disconnect(device.deviceId)
        dataAcquisitionManagers.remove(device.deviceId)
    }

    fun disconnectAllDevices() {
        dataAcquisitionManagers.onEach {
            it.value.disconnect()
        }
        dataAcquisitionManagers.clear()
        buildManager.stop()
    }

    fun startSampling(category: Category) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            viewModelScope
                .launch {
                    eventChannel.send(Event.Error(throwable))
                }
        }) {
            selectedDevice?.let { device ->
                selectedSensor?.let { sensor ->
                    selectedFrequency?.let { frequency ->
                        resetSamplingState(device = device)
                        projectRepository.startSampling(
                            keys = keys,
                            projectId = project.id,
                            deviceId = device.deviceId,
                            label = label,
                            lengthMs = sampleLength,
                            category = category,
                            intervalMs = 1.div(frequency.toFloat()).times(1000),
                            sensor = sensor.name
                        ).let { response ->
                            guard(response.success) {
                                throw Throwable(response.error)
                            }
                            dataAcquisitionManagers[device.deviceId]?.isSamplingRequestedFromDevice =
                                true
                        }
                    }
                }
            }
        }
    }

    fun deploy() {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            viewModelScope
                .launch { eventChannel.send(Event.Error(throwable)) }
        }) {
            projectRepository.deploymentInfo(
                projectId = project.id,
                keys = keys
            ).let { response ->
                guard(response.success) {
                    throw Throwable(response.error)
                }
                if (response.hasDeployment) {
                    deploymentState = DeploymentState.Building.Finished
                    downloadBuild()
                } else {
                    build()
                }
            }
        }
    }

    private suspend fun build() {
        projectRepository.buildOnDeviceModels(
            projectId = project.id,
            keys = keys
        ).let { response ->
            guard(response.success) {
                // Disconnect the websocket in case the build command fails
                throw Throwable(response.error)
            }
            buildManager.start(response.id)
        }
    }

    /**
     * Download build.
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun downloadBuild() {
        deploymentState = DeploymentState.Downloading.Started
        var data: ByteArray
        projectRepository.downloadBuild(
            projectId = project.id,
            keys = keys
        ).let { response ->
            guard(response.isSuccessful) {
                deploymentState = DeploymentState.Unknown
                throw Throwable(
                    response.errorBody()?.string() ?: "Error while downloading firmware"
                )
            }
            response.body()?.byteStream()?.let { inputStream ->
                data = inputStream.readBytes()
                inputStream.close()
                deploymentState = DeploymentState.Downloading.Finished(data = data)
            }
        }.also {
            if (deploymentState is DeploymentState.Downloading.Finished) {
                deploymentState = DeploymentState.Verifying
            }
        }
    }

    /**
     * Resets the current sampling state of a DataAcquisitionManager
     */
    fun resetSamplingState() {
        // We should reset the sampling state before starting a new sampling session.
        selectedDevice?.let { device ->
            resetSamplingState(device = device)
        }
    }

    /**
     * Resets the current sampling state of a DataAcquisitionManager
     */
    private fun resetSamplingState(device: Device) {
        dataAcquisitionManagers[device.deviceId]?.resetSamplingState()
    }

    fun rename(device: Device, name: String) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            viewModelScope
                .launch {
                    eventChannel.send(Event.Error(throwable))
                        .also { isDeviceRenameRequested = false }
                }
        }) {
            projectRepository.renameDevice(
                apiKey = keys.apiKey,
                projectId = project.id,
                deviceId = device.deviceId,
                name = name
            ).let { response ->
                guard(response.success) {
                    throw Throwable(response.error)
                }
                listDevices()
            }
        }
    }

    fun delete(device: Device) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            viewModelScope
                .launch {
                    eventChannel.send(Event.Error(throwable))
                        .also { isDeviceRenameRequested = false }
                }
        }) {
            projectRepository.deleteDevice(
                apiKey = keys.apiKey,
                projectId = project.id,
                deviceId = device.deviceId
            ).let { response ->
                guard(response.success) {
                    throw Throwable(response.error)
                }
                dataAcquisitionManagers.disconnect(deviceId = device.deviceId)
                configuredDevices.remove(device)
            }
        }
    }
}

/**
 * Disconnect device
 */
private fun SnapshotStateMap<String, DataAcquisitionManager>.disconnect(deviceId: String) {
    remove(deviceId)?.apply {
        state.takeIf {
            it == DeviceState.CONNECTING ||
                    it == DeviceState.AUTHENTICATING ||
                    it == DeviceState.AUTHENTICATED
        }?.apply {
            disconnect()
        }
    }
}

private val ATTACHMENT_HEADER = "content-disposition"


