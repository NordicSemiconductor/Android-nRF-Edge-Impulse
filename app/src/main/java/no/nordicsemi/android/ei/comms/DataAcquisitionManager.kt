package no.nordicsemi.android.ei.comms

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ei.ble.BleDevice
import no.nordicsemi.android.ei.ble.DiscoveredBluetoothDevice
import no.nordicsemi.android.ei.model.*
import no.nordicsemi.android.ei.model.Message.*
import no.nordicsemi.android.ei.util.exhaustive
import no.nordicsemi.android.ei.viewmodels.state.DeviceState
import no.nordicsemi.android.ei.websocket.EiWebSocket
import no.nordicsemi.android.ei.websocket.WebSocketState
import okhttp3.OkHttpClient
import okhttp3.Request

@OptIn(ExperimentalCoroutinesApi::class)
class CommsManager(
    val device: DiscoveredBluetoothDevice,
    private val scope: CoroutineScope,
    private val gson: Gson,
    private val developmentKeys: DevelopmentKeys,
    client: OkHttpClient,
    context: Context,
) {
    private val bleDevice = BleDevice(
        device = device.bluetoothDevice,
        context = context
    )
    private val dataAcquisitionWebSocket = EiWebSocket(
        client = client,
        request = Request.Builder().url("wss://remote-mgmt.edgeimpulse.com").build()
    )

    /** The device ID. Initially set to device MAC address. */
    private val deviceId: String = device.deviceId

    /** The device state. */
    var state by mutableStateOf(DeviceState.IN_RANGE)

    init {
        scope.launch { registerToWebSocketStateChanges() }
        scope.launch { registerToWebSocketMessages() }
        scope.launch { registerToDeviceNotifications() }
        scope.launch { registerToDeviceStateChanges() }
    }

    /**
     * Initiates BLE connection to the device by launching a coroutine
     */
    fun connect() {
        scope.launch {
            bleDevice.connect()
        }
    }

    /**
     * Disconnects the BLE device and closes the associated Web Socket after the disconnection is completed.
     */
    fun disconnect() {
        scope.launch {
            bleDevice.disconnectDevice().also {
                // Close the Web Socket if it's open.
                dataAcquisitionWebSocket.disconnect()
            }
        }
    }
    private suspend fun registerToWebSocketStateChanges() {
        dataAcquisitionWebSocket.stateAsFlow().collect { webSocketState ->
            when (webSocketState) {
                // The Web Socket is open.
                is WebSocketState.Open -> {
                    // Initialize notifications. This will enable notifications and cause a
                    // Hello message to be sent from the device.
                    Log.d("AAAA", "Web Socket opened, enabling notifications")
                    bleDevice.initialize()
                }
                else -> {
                }
            }.exhaustive
        }
    }

    private suspend fun registerToWebSocketMessages() {
        dataAcquisitionWebSocket.messageAsFlow().collect { json ->
            val message = gson.fromJson(json, Message::class.java)
            Log.d("AAAA", "Received message from WebSocket: $message")
            when (message) {
                is HelloResponse -> {
                    // if the Hello message returned with a success wrap the received response and send it to the device
                    message.takeIf {
                        it.hello
                    }?.let {
                        bleDevice.send(
                            generateDeviceMessage(
                                message = WebSocketMessage(
                                    direction = Direction.RECEIVE,
                                    message = HelloResponse(hello = true)
                                )
                            )
                        )
                        state = DeviceState.AUTHENTICATED
                    } ?: run {
                        bleDevice.send(
                            generateDeviceMessage(
                                message = ConfigureMessage(
                                    message = Configure(
                                        apiKey = developmentKeys.apiKey
                                    )
                                )
                            )
                        )
                    }
                }
                is SampleRequest -> {
                    bleDevice.send(
                        generateDeviceMessage(
                            message = WebSocketMessage(
                                direction = Direction.RECEIVE,
                                message = message
                            )
                        )
                    )
                }
                else -> {

                }
            }.exhaustive
        }
    }

    private suspend fun registerToDeviceStateChanges() {
        bleDevice.stateAsFlow().collect { bleState ->
            Log.d("AAAA", "New state: $bleState")
            when (bleState) {
                // Device started to connect.
                ConnectionState.Connecting -> state = DeviceState.CONNECTING
                // Device is connected, service discovery and initialization started.
                ConnectionState.Initializing -> { /* do nothing */
                }
                // Device is ready and initiated. It has required services.
                ConnectionState.Ready -> {
                    // When the device is connected, open the Web Socket.
                    Log.d("AAAA", "Device is ready, opening socket")
                    state = DeviceState.AUTHENTICATING
                    dataAcquisitionWebSocket.connect()
                }
                // Device gets disconnected.
                ConnectionState.Disconnecting -> { /* do nothing */
                }
                // Device is now disconnected.
                is ConnectionState.Disconnected -> {
                    Log.d("AAAA", "Device is disconnected")
                    // Use IN_RANGE, so that the device row is clickable.
                    state = DeviceState.IN_RANGE
                }
            }.exhaustive
        }
    }

    private suspend fun registerToDeviceNotifications() {
        bleDevice.messagesAsFlow()
            .transform<String, DeviceMessage> { json ->
                emit(gson.fromJson(json, DeviceMessage::class.java))
            }
            .collect { deviceMessage ->
                Log.d("AAAA", "Collected to: $deviceMessage")
                when (deviceMessage) {
                    is WebSocketMessage -> {
                        when (deviceMessage.message) {
                            is Hello -> {
                                deviceMessage.message.deviceId = bleDevice.device.address
                                dataAcquisitionWebSocket.send(
                                    gson.toJsonTree(
                                        deviceMessage.message,
                                        Message::class.java
                                    )
                                )
                            }
                            else -> {
                                //TODO check other messages
                            }
                        }.exhaustive
                    }
                    else -> {
                        //TODO check other messages
                    }
                }.exhaustive
            }
    }

    //TODO sending messages from the phone to the device
    fun send(deviceMessage: DeviceMessage) {
        scope.launch {
            val deviceMessageJson = JsonParser.parseString(gson.toJson(deviceMessage)).asJsonObject
            dataAcquisitionWebSocket.send(deviceMessageJson.get(MESSAGE))
        }
    }

    /**
     * Starts sampling on the connected device.
     *
     * @param label                 Sample label
     * @param sampleLength          Sample length
     * @param selectedFrequency     Selected frequency
     * @param selectedSensor        Selected sensor
     */
    fun startSampling(
        label: String,
        sampleLength: Int,
        selectedFrequency: Int,
        selectedSensor: Sensor
    ) {
        bleDevice.send(
            generateDeviceMessage(
                message = WebSocketMessage(
                    direction = Direction.RECEIVE,
                    message = SampleRequest(
                        label = label,
                        length = sampleLength,
                        hmacKey = developmentKeys.hmacKey,
                        interval = selectedFrequency,
                        sensor = selectedSensor.name
                    )
                )
            )
        )
    }

    /**
     * Generates a device message in the format of a Json string with a new line character appended to indicate the end of the message.
     */
    private fun generateDeviceMessage(message: DeviceMessage): String = StringBuilder(
        gson.toJson(
            message
        )
    ).appendLine().toString()
}

private const val MESSAGE = "message"