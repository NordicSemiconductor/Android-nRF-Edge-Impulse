package no.nordicsemi.android.ei.comms

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ei.ble.BleDevice
import no.nordicsemi.android.ei.ble.DiscoveredBluetoothDevice
import no.nordicsemi.android.ei.model.*
import no.nordicsemi.android.ei.model.Message.*
import no.nordicsemi.android.ei.model.Message.Sample
import no.nordicsemi.android.ei.model.Message.Sample.*
import no.nordicsemi.android.ei.util.exhaustive
import no.nordicsemi.android.ei.viewmodels.event.Event
import no.nordicsemi.android.ei.viewmodels.state.DeviceState
import no.nordicsemi.android.ei.websocket.EiWebSocket
import no.nordicsemi.android.ei.websocket.WebSocketState
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException


@OptIn(ExperimentalCoroutinesApi::class)
class DataAcquisitionManager(
    val device: DiscoveredBluetoothDevice,
    private val scope: CoroutineScope,
    private val gson: Gson,
    private val developmentKeys: DevelopmentKeys,
    private val client: OkHttpClient,
    private val eventChannel: Channel<Event>,
    context: Context,
) {
    private val bleDevice = BleDevice(
        device = device.bluetoothDevice,
        context = context
    )
    private val dataAcquisitionWebSocket = EiWebSocket(
        client = client,
        request = okhttp3.Request.Builder().url("wss://remote-mgmt.edgeimpulse.com").build()
    )

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        scope.launch {
            eventChannel
                .send(Event.Error(throwable = throwable))
        }
    }

    var samplingState by mutableStateOf<Sample>(Unknown)
        private set

    var isSamplingRequestedFromDevice by mutableStateOf(false)

    /** The device ID. Initially set to device MAC address. */
    private val deviceId: String = device.deviceId

    /** The device state. */
    var state by mutableStateOf(DeviceState.IN_RANGE)
        private set

    init {
        scope.launch(exceptionHandler) { registerToWebSocketStateChanges() }
        scope.launch(exceptionHandler) { registerToWebSocketMessages() }
        scope.launch(exceptionHandler) { registerToDeviceNotifications() }
        scope.launch(exceptionHandler) { registerToDeviceStateChanges() }
    }

    /**
     * Initiates BLE connection to the device by launching a coroutine
     */
    fun connect() {
        scope.launch((exceptionHandler)) {
            bleDevice.connect()
        }
    }

    /**
     * Disconnects the BLE device and closes the associated Web Socket after the disconnection is completed.
     */
    fun disconnect() {
        scope.launch((exceptionHandler)) {
            bleDevice.disconnectDevice().also {
                // Close the Web Socket if it's open.
                dataAcquisitionWebSocket.disconnect()
            }
        }
    }

    /**
     * Resets the current sampling state.
     */
    fun resetSamplingState() {
        samplingState = Unknown
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
        dataAcquisitionWebSocket.messageAsFlow().transform { json ->
            emit(gson.fromJson(json, Message::class.java))
        }.collect { message ->
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
                is Request -> {
                    samplingState = message
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
            .collect { json ->
                val deviceMessage = gson.fromJson(json, DeviceMessage::class.java)
                when (deviceMessage) {
                    is WebSocketMessage -> {
                        when (deviceMessage.message) {
                            is Hello -> {
                                // Let's confirm if
                                deviceMessage.message.apiKey.takeIf { apiKey ->
                                    apiKey.isNotEmpty() && apiKey != developmentKeys.apiKey
                                }?.let {
                                    bleDevice.send(
                                        generateDeviceMessage(
                                            message = ConfigureMessage(
                                                message = Configure(
                                                    apiKey = developmentKeys.apiKey
                                                )
                                            )
                                        )
                                    )
                                } ?: run {
                                    deviceMessage.message.deviceId = bleDevice.device.address
                                    dataAcquisitionWebSocket.send(
                                        gson.toJsonTree(
                                            deviceMessage.message,
                                            Message::class.java
                                        )
                                    )
                                }
                            }
                            is Response -> {
                                samplingState = deviceMessage.message
                                send(json = json)
                            }
                            is ProgressEvent -> {
                                samplingState = deviceMessage.message
                                send(json = json)
                            }
                            is Finished -> {
                                isSamplingRequestedFromDevice = false
                            }
                            else -> {
                                send(json = json)
                            }
                        }.exhaustive
                    }
                    is DataSample -> {
                        postDataSample(
                            headersJson = JsonParser.parseString(json).asJsonObject.getAsJsonObject(
                                "headers"
                            ),
                            dataSample = deviceMessage
                        )
                    }
                    else -> {
                        //TODO check other messages
                    }
                }.exhaustive
            }
    }

    fun send(json: String) {
        isSamplingRequestedFromDevice.takeIf { !it }?.let {
            scope.launch {
                dataAcquisitionWebSocket.send(JsonParser.parseString(json).asJsonObject.get(MESSAGE))
            }
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
    @Suppress("unused")
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
                    message = Request(
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

    private fun postDataSample(headersJson: JsonObject, dataSample: DataSample) {
        val headersBuilder = Headers.Builder()
        val headers = headersJson.keySet().toList().onEach {
            headersBuilder.add(name = it, value = headersJson[it].asString)
        }.let { headersBuilder.build() }
        Log.d("AAAA", "Data Sample $dataSample")
        val request: okhttp3.Request = okhttp3.Request.Builder()
            .headers(headers = headers)
            .url(dataSample.address)
            .post(
                body = Base64.decode(
                    dataSample.body,
                    Base64.DEFAULT
                ).toRequestBody()
            )
            .build()
        client.newCall(request = request)
            .enqueue(responseCallback = object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    samplingState = Finished(false, e.message)
                }

                override fun onResponse(call: Call, response: okhttp3.Response) {
                    samplingState = Finished(
                        sampleFinished = true,
                        error = response.takeIf {
                            !it.isSuccessful
                        }?.let {
                            "Error while uploading sample. ${
                                it.body?.let { body ->
                                    String(body.bytes())
                                }
                            }"
                        } ?: run { "Data sample successfully uploaded." }
                    )
                }
            })
    }
}


private val regex = Regex("^[0-9]+")
private const val MESSAGE = "message"