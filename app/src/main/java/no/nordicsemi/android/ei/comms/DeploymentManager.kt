package no.nordicsemi.android.ei.comms

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.model.DeviceMessage
import no.nordicsemi.android.ei.model.SocketToken
import no.nordicsemi.android.ei.viewmodels.state.DeviceState
import no.nordicsemi.android.ei.websocket.EiWebSocket
import okhttp3.OkHttpClient
import okhttp3.Request

@OptIn(ExperimentalCoroutinesApi::class)
class DeploymentManager(
    private val scope: CoroutineScope,
    private val gson: Gson,
    socketToken: SocketToken,
    client: OkHttpClient
) {
    private var deploymentWebSocket: EiWebSocket = EiWebSocket(
        client = client,
        request = Request.Builder()
            .url("wss://studio.edgeimpulse.com/socket.io/?transport=websocket&EIO=3&token=${socketToken.socketToken}")
            .build()
    )

    /** The device state. */
    var state by mutableStateOf(DeviceState.IN_RANGE)

    init {
        scope.launch { registerToWebSocketStateChanges() }
        scope.launch { registerToWebSocketMessages() }
        connect()
    }

    /**
     * Initiates a websocket connection to obtain deployment messages.
     */
    fun connect() {
        scope.launch {
            deploymentWebSocket.connect()
        }
    }

    /**
     * Disconnect from the deployment websocket
     */
    fun disconnect() {
        scope.launch {
            // Close the Web Socket if it's open.
            deploymentWebSocket.disconnect()
        }
    }

    private suspend fun registerToWebSocketStateChanges() {
        deploymentWebSocket.stateAsFlow().collect { webSocketState ->
            Log.d("AAAA", "Deployment manager websocket state: $webSocketState")
        }
    }

    private suspend fun registerToWebSocketMessages() {
        deploymentWebSocket.messageAsFlow().collect { json ->
            Log.d("AAAA", "Received message from WebSocket: $json")
        }
    }

    //TODO sending messages from the phone to the device
    fun send(deviceMessage: DeviceMessage) {
        scope.launch {
            val deviceMessageJson = JsonParser.parseString(gson.toJson(deviceMessage)).asJsonObject
            deploymentWebSocket.send(deviceMessageJson.get(MESSAGE))
        }
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