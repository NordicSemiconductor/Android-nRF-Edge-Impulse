package no.nordicsemi.android.ei.comms

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.transform
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ei.ble.BleDevice
import no.nordicsemi.android.ei.ble.DiscoveredBluetoothDevice
import no.nordicsemi.android.ei.model.*
import no.nordicsemi.android.ei.util.MessageTypeAdapter
import no.nordicsemi.android.ei.util.exhaustive
import no.nordicsemi.android.ei.viewmodels.state.DeviceState
import no.nordicsemi.android.ei.websocket.EiWebSocket
import no.nordicsemi.android.ei.websocket.WebSocketState
import okhttp3.OkHttpClient
import okhttp3.Request

@OptIn(ExperimentalCoroutinesApi::class)
class CommsManager(
    private val developmentKeys: DevelopmentKeys,
    device: DiscoveredBluetoothDevice,
    context: Context,
    client: OkHttpClient,
    request: Request,
    scope: CoroutineScope,
) {
    private val bleDevice = BleDevice(
        device = device.bluetoothDevice,
        context = context
    )
    private val webSocket = EiWebSocket(
        client = client,
        request = request,
        coroutineScope = scope,
    )

    private val gson = GsonBuilder()
        .registerTypeAdapter(Message::class.java, MessageTypeAdapter())
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create()

    /** The device ID. Initially set to device MAC address. */
    val deviceId: String = device.deviceId
    /** The device state. */
    var state by mutableStateOf(DeviceState.IN_RANGE)

    init {
        scope.launch {
            webSocket.stateAsFlow().collect { webSocketState ->
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
        scope.launch {
            bleDevice.stateAsFlow().collect { bleState ->
                Log.d("AAAA", "New state: $bleState")
                when (bleState) {
                    // Device started to connect.
                    ConnectionState.Connecting -> state = DeviceState.CONNECTING
                    // Device is connected, service discovery and initialization started.
                    ConnectionState.Initializing -> { /* do nothing */ }
                    // Device is ready and initiated. It has required services.
                    ConnectionState.Ready -> {
                        scope.launch {
                            bleDevice.messagesAsFlow()
                                .transform<String, DeviceMessage> { message ->
                                    Log.d("AAAA", "Received: $message")
                                    val deviceMessage = gson.fromJson(message, DeviceMessage::class.java)
                                    Log.d("AAAA", "Converted to: $deviceMessage")
                                }
                                .collect { message ->
                                    Log.d("AAAA", "Collected to: $message")
                                }
                        }
                        // When the device is connected, open the Web Socket.
                        Log.d("AAAA", "Device is ready, opening socket")
                        state = DeviceState.AUTHENTICATING
                        webSocket.connect()
                    }
                    // Device gets disconnected.
                    ConnectionState.Disconnecting -> { /* do nothing */ }
                    // Device is now disconnected.
                    is ConnectionState.Disconnected -> {
                        Log.d("AAAA", "Device is disconnected")
                        // Use IN_RANGE, so that the device row is clickable.
                        state = DeviceState.IN_RANGE
                        // Close the Web Socket if it's open.
                        webSocket.disconnect()
                    }
                }.exhaustive
            }
        }
    }

    /**
     * Initiates BLE connection to the device.
     */
    fun connect() {
        bleDevice.connect()
    }

    /**
     * Disconnects the BLE device and closes the associated Web Socket.
     */
    fun disconnect() {
        webSocket.disconnect()
        bleDevice.disconnect()
    }

    /**
     * Authenticates the device in the EI Studio.
     */
    private suspend fun authenticate() {
        val deviceMessage = WebSocketMessage(
            message = Message.Hello(
                apiKey = developmentKeys.apiKey,
                deviceId = deviceId,
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
        val deviceMessageJson = JsonParser.parseString(gson.toJson(deviceMessage)).asJsonObject

        //TODO remove this delay and we have to observe the messages emitted from the socket
        delay(5000)
        webSocket.send(deviceMessageJson.get(MESSAGE).asJsonObject)
    }

    //TODO sending messages from the phone to the device
    fun send(deviceMessage: DeviceMessage) {
        val deviceMessageJson = JsonParser.parseString(gson.toJson(deviceMessage)).asJsonObject
        webSocket.send(deviceMessageJson.get(MESSAGE).asJsonObject)
    }
}

private const val MESSAGE = "message"