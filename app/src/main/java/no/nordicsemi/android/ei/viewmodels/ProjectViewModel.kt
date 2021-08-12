package no.nordicsemi.android.ei.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.ble.DiscoveredBluetoothDevice
import no.nordicsemi.android.ei.comms.DataAcquisitionManager
import no.nordicsemi.android.ei.comms.DeploymentManager
import no.nordicsemi.android.ei.di.*
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
    var dataAcquisitionManager = mutableStateMapOf<String, DataAcquisitionManager>()
        private set

    /** A list of configured devices obtained from the service. */
    var configuredDevices = mutableStateListOf<Device>()
        private set

    /** A list of connected devices derived using the configuredDevices and the commsManagers. */
    var connectedDevices = derivedStateOf {
        configuredDevices.filterList {
            dataAcquisitionManager[deviceId]?.state == DeviceState.AUTHENTICATED
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
    var sampleLength by mutableStateOf(2000)
        //TODO fix hardcoded sample length
        private set
    var selectedFrequency: Number? by mutableStateOf(null)
        private set

    /** Creates a deployment manager */
    private var deploymentManager = DeploymentManager(
        context = context,
        scope = viewModelScope,
        gson = gson,
        socketToken = projectDataRepository.socketToken,
        client = client
    )

    var samplingState = derivedStateOf {
        selectedDevice?.let {
            dataAcquisitionManager[it.deviceId]?.samplingState
        }
    }
        private set

    /** Whether a build is in progress. */
    var buildState = derivedStateOf {
        deploymentManager.buildState
    }

    var logs = derivedStateOf {
        deploymentManager.logs
    }

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
                    dataAcquisitionManager.remove(device.deviceId)?.apply {
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
        dataAcquisitionManager.getOrPut(key = device.deviceId, defaultValue = {
            DataAcquisitionManager(
                scope = viewModelScope,
                gson = gson,
                developmentKeys = keys,
                device = device,
                client = client,
                eventChannel = eventChannel,
                context = getApplication()
            )
        }).run {
            connect()
        }

    fun disconnect(device: DiscoveredBluetoothDevice) {
        dataAcquisitionManager[device.deviceId]?.disconnect()
        dataAcquisitionManager.remove(device.deviceId)
    }

    fun startSampling(category: String = "training") {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            viewModelScope
                .launch { eventChannel.send(Event.Error(throwable)) }
        }) {
            selectedDevice?.let { device ->
                selectedSensor?.let { sensor ->
                    selectedFrequency?.let { frequency ->
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
                        }
                    }
                }
            }
        }
    }

    /**
     * Build device firmware.
     * @param engine        Engine type
     * @param modelType     ModelType to download.
     */
    fun buildOnDeviceModel(
        engine: Engine,
        modelType: ModelType
    ) {
        deploymentManager.build(buildOnDeviceModel = {
            projectRepository.buildOnDeviceModels(
                projectId = project.id,
                keys = keys,
                engine = engine,
                modelType = modelType
            )
        })
    }

    /**
     * Download build.
     * @param context       Context.
     * @param modelType     ModelType to download.
     * @param uri           Uri to save the file to.
     */
    fun downloadBuild(context: Context, modelType: ModelType, uri: Uri) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            viewModelScope
                .launch { eventChannel.send(Event.Error(throwable)) }
        }) {
            projectRepository.downloadBuild(
                projectId = project.id,
                keys = keys,
                modelType = modelType
            ).let { response ->
                val bytes = response.byteStream().readBytes()
                response.byteStream().close()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(bytes)
                    outputStream.close()
                }
            }
        }
    }
}


