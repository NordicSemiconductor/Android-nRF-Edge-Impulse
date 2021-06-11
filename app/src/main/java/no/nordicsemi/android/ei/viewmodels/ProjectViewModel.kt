package no.nordicsemi.android.ei.viewmodels

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import dagger.hilt.EntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.ble.BleDevice
import no.nordicsemi.android.ei.comms.CommsManager
import no.nordicsemi.android.ei.di.*
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.Message
import no.nordicsemi.android.ei.model.Sensor
import no.nordicsemi.android.ei.model.WebSocketMessage
import no.nordicsemi.android.ei.repository.ProjectDataRepository
import no.nordicsemi.android.ei.repository.ProjectRepository
import no.nordicsemi.android.ei.repository.UserDataRepository
import no.nordicsemi.android.ei.util.MessageTypeAdapter
import no.nordicsemi.android.ei.util.guard
import no.nordicsemi.android.ei.viewmodels.event.Event
import no.nordicsemi.android.ei.websocket.EiWebSocket
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val userManager: UserManager,
    private val projectRepository: ProjectRepository,
    private val client: OkHttpClient,
    private val request: Request,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : AndroidViewModel(context as Application) {

    var commsManagers = mutableMapOf<BluetoothDevice, CommsManager>()
        private set

    private val gson = GsonBuilder()
        .registerTypeAdapter(Message::class.java, MessageTypeAdapter())
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create()

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

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

    var configuredDevices = mutableStateListOf<Device>()
        private set

    val project
        get() = projectDataRepository.project
    val keys
        get() = projectDataRepository.developmentKeys
    val focusRequester = FocusRequester()
    var selectedDevice: Device? by mutableStateOf(null)
        private set
    var label: String by mutableStateOf("")
        private set
    var selectedSensor: Sensor? by mutableStateOf(null)
        private set
    var selectedFrequency: Number? by mutableStateOf(null)
        private set

    init {
        listDevices()
    }

    fun listDevices(swipedToRefresh: Boolean = false) {
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

    fun onFrequencySelected(frequency: Number) {
        this.selectedFrequency = frequency
    }

    //TODO need to finalize the api
    fun connect(device: BluetoothDevice) {
        val commsManager = CommsManager(
            bleDevice = BleDevice(device = device, context = getApplication()),
            eiWebSocket = EiWebSocket(
                client = client,
                request = request,
                ioDispatcher = ioDispatcher
            )
        )
        commsManagers[device] = commsManager
        authenticate(device = device)
    }

    //TODO needs to be discussed
    private fun authenticate(device: BluetoothDevice) {
        commsManagers[device]?.let {
            viewModelScope.launch {
                val deviceMessage = WebSocketMessage(
                    message = Message.Hello(
                        apiKey = keys.apiKey,
                        deviceId = device.address,
                        deviceType = "NRF5340_DK",
                        connection = "ip",
                        sensors = listOf(
                            Sensor(
                                name = "Accelerometer",
                                maxSampleLengths = 60000,
                                frequencies = listOf(62.5, 100)
                            ),
                            Sensor(
                                name = "Microphone",
                                maxSampleLengths = 4000,
                                frequencies = listOf(16000)
                            )
                        )
                    )
                )
                it.authenticate(deviceMessage)
            }
        }
    }
}