package no.nordicsemi.android.ei.viewmodels

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import android.util.Pair
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.EntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.runtime.mcumgr.McuMgrTransport
import io.runtime.mcumgr.ble.McuMgrBleTransport
import io.runtime.mcumgr.dfu.FirmwareUpgradeCallback
import io.runtime.mcumgr.dfu.FirmwareUpgradeController
import io.runtime.mcumgr.dfu.FirmwareUpgradeManager
import io.runtime.mcumgr.dfu.FirmwareUpgradeManager.State.*
import io.runtime.mcumgr.exception.McuMgrException
import io.runtime.mcumgr.image.McuMgrImage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ConnectionPriorityRequest
import no.nordicsemi.android.ei.ble.DiscoveredBluetoothDevice
import no.nordicsemi.android.ei.comms.BuildManager
import no.nordicsemi.android.ei.comms.CommsManager
import no.nordicsemi.android.ei.comms.DeploymentState
import no.nordicsemi.android.ei.di.ProjectComponentEntryPoint
import no.nordicsemi.android.ei.di.ProjectManager
import no.nordicsemi.android.ei.di.UserComponentEntryPoint
import no.nordicsemi.android.ei.di.UserManager
import no.nordicsemi.android.ei.model.Category
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.InferencingMessage.InferencingRequest
import no.nordicsemi.android.ei.model.Message.Sample
import no.nordicsemi.android.ei.model.Sensor
import no.nordicsemi.android.ei.repository.ProjectDataRepository
import no.nordicsemi.android.ei.repository.ProjectRepository
import no.nordicsemi.android.ei.util.ZipPackage
import no.nordicsemi.android.ei.util.guard
import no.nordicsemi.android.ei.viewmodels.event.Event
import no.nordicsemi.android.ei.viewmodels.state.DeviceState
import no.nordicsemi.android.ei.viewmodels.state.InferencingState
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val userManager: UserManager,
    private val projectRepository: ProjectRepository,
    private val client: OkHttpClient,
    private val gson: Gson
) : AndroidViewModel(context as Application), FirmwareUpgradeCallback {

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    @Suppress("unused")
    private val bluetoothAdapter = bluetoothManager.adapter

    private val userComponentEntryPoint: UserComponentEntryPoint
        get() = EntryPoints.get(userManager.userComponent!!, UserComponentEntryPoint::class.java)

    private val projectManager: ProjectManager
        get() = userComponentEntryPoint.getProjectManager()

    private val projectDataRepository: ProjectDataRepository
        get() = EntryPoints
            .get(projectManager.projectComponent!!, ProjectComponentEntryPoint::class.java)
            .projectDataRepository()

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

    /** Whether the list of configured devices is refreshing. */
    var isRefreshing by mutableStateOf(false)
        private set

    /** Whether a device rename is been requested. */
    var isDeviceRenameRequested: Boolean by mutableStateOf(false)
        private set

    // ---- Fields used for Recording New Sample --------------
    var dataAcquisitionTarget: Device? by mutableStateOf(null)
        private set

    /** Lable of the acquired data sample */
    var label: String by mutableStateOf("Sample")
        private set

    /** Sensor used for data acquisition */
    var sensor: Sensor? by mutableStateOf(null)
        private set

    /** Sample length used for data acquisition */
    var sampleLength by mutableStateOf(5000)
        private set

    /** Frequency used for data acquisition */
    var frequency: Number? by mutableStateOf(null)
        private set

    /** Sampling state contain the state of sampling*/
    val samplingState by derivedStateOf {
        dataAcquisitionTarget?.let {
            commsManagers[it.deviceId]?.samplingState
        } ?: Sample.Unknown
    }

    /** Sampling state contain the state of sampling*/
    val isSamplingStartedFromDevice by derivedStateOf {
        dataAcquisitionTarget?.let {
            commsManagers[it.deviceId]?.isSamplingRequestedFromDevice
        } ?: false
    }

    /** Contains the combined deployment state from downloading, building and DFU */
    var deploymentState: DeploymentState by mutableStateOf(DeploymentState.Unknown)
        private set

    /** Inferencing target is the device with which inferencing can be done*/
    var inferencingTarget: Device? by mutableStateOf(null)
        private set

    /** Contains the inferencing state */
    var inferencingState = derivedStateOf {
        inferencingTarget?.let {
            commsManagers[it.deviceId]?.inferencingState
        } ?: InferencingState.Stopped
    }
        private set

    /** Inferencing result obtained from the device */
    var inferencingResults = derivedStateOf {
        inferencingTarget?.let {
            commsManagers[it.deviceId]?.inferenceResults
        } ?: mutableStateListOf()
    }
        private set

    /** Deployment job */
    private lateinit var deploymentJob: Job

    /** Creates a deployment manager */
    private var buildManager = BuildManager(
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

    /** DFU target */
    var deploymentTarget: Device? by mutableStateOf(null)
        private set

    /** Firmware upgrade controller used to cancel a firmware upgrade */
    private var firmwareUpgradeController: FirmwareUpgradeController? = null
    private var dfuManager:FirmwareUpgradeManager? = null

    /** DFU transfer speed */
    var transferSpeed by mutableStateOf(0f)
        private set

    /** DFU Progress */
    var progress by mutableStateOf(0)
        private set

    /** Initial bytes used to calculate the DFU transfer speed */
    private var initialBytes = 0

    /** Upload timestamp used to calculate the DFU transfer speed */
    private var uploadStartTimestamp: Long = 0


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
                    commsManagers[it.deviceId]?.let { commsManager ->
                        disconnect(commsManager.device)
                    }
                }
                configuredDevices.apply {
                    clear()
                    addAll(response.devices)
                    // We need to re-assign the selected device name for record sample data screen
                    dataAcquisitionTarget =
                        configuredDevices.find { it.deviceId == dataAcquisitionTarget?.deviceId }
                    inferencingTarget =
                        configuredDevices.find { it.deviceId == inferencingTarget?.deviceId }
                }
            }.also { isRefreshing = false }
        }
    }

    /**
     * Registers Build manager updates
     */
    private fun registerForBuildManager() {
        deploymentJob = viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            viewModelScope
                .launch { eventChannel.send(Event.Error(throwable)) }
        }) {
            buildManager.buildStateAsFlow().collect {
                deploymentState = it
                if (it is DeploymentState.Building.Finished) {
                    downloadBuild()
                }
            }
        }
    }

    /**
     * Selects a data acquisition target
     * @param device
     */
    fun onDataAcquisitionTargetSelected(device: Device) {
        dataAcquisitionTarget = device
        device.sensors.firstOrNull()
            ?.let { onSensorSelected(sensor = it) }
    }

    /**
     * Updates the label
     * @param label updated label
     */
    fun onLabelChanged(label: String) {
        this.label = label
    }

    /**
     * Selects a sensor for data acquisition
     * @param sensor Selected sensor
     */
    fun onSensorSelected(sensor: Sensor) {
        this.sensor = sensor
        sensor.frequencies.firstOrNull()
            ?.let { onFrequencySelected(frequency = it) }
            ?: run { frequency = null }
    }

    /**
     * Selects a frequency
     * @param frequency frequency to do data acquisition.
     */
    fun onFrequencySelected(frequency: Number) {
        this.frequency = frequency
    }

    /**
     * Sets the updates sample length
     * @param sampleLength Sample length
     */
    fun onSampleLengthChanged(sampleLength: Int) {
        this.sampleLength = sampleLength
    }

    /**
     * Connect to a device
     * @param device Discovered bluetooth device.
     */
    fun connect(device: DiscoveredBluetoothDevice) {
        commsManagers.getOrPut(key = device.deviceId, defaultValue = {
            CommsManager(
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
        }).apply {
            connect()
            // Register a collector what would listen to connectivity changes
            viewModelScope.launch {
                this@apply.connectionState().collect {
                    if(it == DeviceState.IN_RANGE) {
                        commsManagers.remove(device.deviceId)
                        resetSelectedTargets(device = device)
                    }
                }
            }
        }
    }

    /**
     * Disconnects a device
     */
    fun disconnect(device: DiscoveredBluetoothDevice) {
        commsManagers.disconnect(device.deviceId)
        resetSelectedTargets(device = device)
    }

    private fun resetSelectedTargets(device: DiscoveredBluetoothDevice){
        deploymentTarget = deploymentTarget?.takeIf {
            it.deviceId == device.deviceId
        }?.let {
            firmwareUpgradeController?.cancel()
            null
        }
        dataAcquisitionTarget = dataAcquisitionTarget?.takeUnless {
            it.deviceId == device.deviceId
        }
        inferencingTarget = inferencingTarget?.takeUnless {
            it.deviceId == device.deviceId
        }
    }

    /**
     * Disconnects all devices
     */
    fun disconnectAllDevices() {
        commsManagers.onEach {
            it.value.disconnect()
        }
        commsManagers.clear()
        if (deploymentState !is DeploymentState.Unknown ||
            deploymentState !is DeploymentState.Cancelled ||
            deploymentState !is DeploymentState.Failed
        )
            cancelDeploy()
    }

    /**
     * Starts sampling via the EI backend
     * @param category sampling category.
     */
    fun startSampling(category: Category) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            viewModelScope
                .launch {
                    eventChannel.send(Event.Error(throwable))
                }
        }) {
            dataAcquisitionTarget?.let { device ->
                sensor?.let { sensor ->
                    frequency?.let { frequency ->
                        resetSamplingState(device = device)
                        commsManagers[device.deviceId]?.startSamplingFromDevice()
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
                            commsManagers[device.deviceId]?.isSamplingRequestedFromDevice =
                                true
                        }
                    }
                }
            }
        }
    }

    /**
     * Selects the deployment target.
     * @param deploymentTarget Deployment target
     */
    fun onDeploymentTargetSelected(deploymentTarget: Device) {
        this.deploymentTarget = deploymentTarget
    }

    /**
     * Starts firmware deployment
     */
    fun deploy() {
        deploymentJob = viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
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

    /**
     * Cancel deployment
     */
    fun cancelDeploy() {
        if (deploymentState is DeploymentState.Building.Started) {
            buildManager.stop()
        }
        firmwareUpgradeController?.cancel()
        deploymentJob.cancel()
    }

    /**
     * Starts bulding a new firmware on the backend
     */
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
                val data = inputStream.readBytes()
                inputStream.close()
                deploymentState = DeploymentState.Downloading.Finished(data = data)
            }
        }.also {
            if (deploymentState is DeploymentState.Downloading.Finished) {
                deploymentTarget?.let {
                    startFirmwareUpgrade(
                        data = (deploymentState as DeploymentState.Downloading.Finished).data,
                        deploymentTarget = it
                    )
                }
            }
        }
    }

    /**
     * Start firmware upgrade
     * @param data              Byte Array containing the firmware
     * @param deploymentTarget  Deployment target
     */
    private fun startFirmwareUpgrade(data: ByteArray, deploymentTarget: Device) {
        commsManagers[deploymentTarget.deviceId]?.device?.bluetoothDevice?.let { bluetoothDevice ->
            val context = getApplication() as Context
            val transport: McuMgrTransport = McuMgrBleTransport(context, bluetoothDevice)
            dfuManager = FirmwareUpgradeManager(transport, this).apply {
                var images = arrayListOf<Pair<Int, ByteArray>>()
                try {
                    McuMgrImage.getHash(data)
                    images.add(Pair(0, data))
                } catch (e: Exception) {
                    try {
                        images = ZipPackage(data).binaries
                    } catch (e1: Exception) {
                        Log.d("AAAA", "Exception? $e1")
                    }
                }
                (transport as McuMgrBleTransport).apply {
                    setLoggingEnabled(true)
                    requestConnPriority(ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH)
                }
                setMode(FirmwareUpgradeManager.Mode.CONFIRM_ONLY)
                start(images, false)
            }
        }
    }

    /**
     * Resets the current sampling state of a DataAcquisitionManager
     */
    fun resetSamplingState() {
        // We should reset the sampling state before starting a new sampling session.
        dataAcquisitionTarget?.let { device ->
            resetSamplingState(device = device)
        }
    }

    /**
     * Resets the current sampling state of a DataAcquisitionManager
     * @param device Reset sampling state for device
     */
    private fun resetSamplingState(device: Device) {
        commsManagers[device.deviceId]?.resetSamplingState()
    }

    /**
     * Rename device
     * @param device    Device to be renamed
     * @param name      New device name
     */
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

    /**
     * Deletes a device from the EI backend
     * @param device to be deleted.
     */
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
                commsManagers[device.deviceId]?.let { commsManager ->
                    disconnect(commsManager.device)
                }
                configuredDevices.remove(device)
            }
        }
    }

    /**
     * Selects an inferencing target
     * @param device Device to send an inferencing request
     */
    fun onInferencingTargetSelected(device: Device) {
        inferencingTarget = device
    }

    /**
     * Sends an inferencing request
     * @param inferencingRequest Inferencing request to be send
     */
    fun sendInferencingRequest(inferencingRequest: InferencingRequest) {
        inferencingTarget?.let { device ->
            commsManagers[device.deviceId]?.sendInferencingRequest(inferencingRequest)
        }
    }

    override fun onUpgradeStarted(controller: FirmwareUpgradeController?) {
        firmwareUpgradeController = controller
        deploymentState = DeploymentState.Verifying
    }

    override fun onStateChanged(
        prevState: FirmwareUpgradeManager.State?,
        newState: FirmwareUpgradeManager.State?
    ) {
        when (newState) {
            NONE -> deploymentState = DeploymentState.Unknown
            VALIDATE -> DeploymentState.Verifying
            UPLOAD -> {
                initialBytes = 0
                deploymentState = DeploymentState.Uploading
            }
            TEST, RESET -> deploymentState = DeploymentState.ApplyingUpdate
            CONFIRM -> DeploymentState.Confirming
        }
    }

    override fun onUploadProgressChanged(bytesSent: Int, imageSize: Int, timestamp: Long) {
        if (initialBytes == 0) {
            uploadStartTimestamp = timestamp
            initialBytes = bytesSent
        } else {
            val bytesSentSinceUploadStarted: Int = bytesSent - initialBytes
            val timeSinceUploadStarted: Long = timestamp - uploadStartTimestamp
            // bytes / ms = KB/s
            transferSpeed =
                (bytesSentSinceUploadStarted / timeSinceUploadStarted).toFloat()
        }
        // When done, reset the counter.
        if (bytesSent == imageSize) {
            initialBytes = 0
        }
        // Convert to percent
        progress = (bytesSent * 100f / imageSize).toInt()
    }

    override fun onUpgradeCompleted() {
        resetDfu()
        deploymentState = DeploymentState.Completed
        // TODO clarify reconnection upon successful connection and needs testing
        // If the upgrade successfully completes let's reconnect to the device.
        deploymentTarget?.let { device ->
            val bluetoothDevice = bluetoothAdapter.getRemoteDevice(device.deviceId)
            connect(
                DiscoveredBluetoothDevice(
                    name = bluetoothDevice.name,
                    rssi = 0,
                    bluetoothDevice = bluetoothDevice
                )
            )
        }
    }

    override fun onUpgradeCanceled(state: FirmwareUpgradeManager.State?) {
        resetDfu()
        deploymentState = DeploymentState.Cancelled
    }

    override fun onUpgradeFailed(
        state: FirmwareUpgradeManager.State?,
        error: McuMgrException?
    ) {
        resetDfu()
        deploymentState = DeploymentState.Failed
    }

    /**
     * Releases the mcu manager ble transport client and other dfu status resources
     */
    private fun resetDfu(){
        dfuManager?.transporter?.release()
        uploadStartTimestamp = 0
        initialBytes = 0
        progress = 0
    }
}

/**
 * Disconnect device
 */
private fun SnapshotStateMap<String, CommsManager>.disconnect(deviceId: String) {
    remove(deviceId)?.apply {
        connectivityState.takeIf {
            it == DeviceState.CONNECTING ||
                    it == DeviceState.AUTHENTICATING ||
                    it == DeviceState.AUTHENTICATED
        }?.apply {
            disconnect()
        }
    }
}