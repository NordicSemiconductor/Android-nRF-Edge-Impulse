package no.nordicsemi.android.ei.comms

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.model.BuildLog
import no.nordicsemi.android.ei.model.SocketToken
import no.nordicsemi.android.ei.service.param.BuildOnDeviceModelResponse
import no.nordicsemi.android.ei.util.guard
import no.nordicsemi.android.ei.websocket.EiWebSocket
import no.nordicsemi.android.ei.websocket.WebSocketState
import okhttp3.OkHttpClient
import okhttp3.Request


@OptIn(ExperimentalCoroutinesApi::class)
class DeploymentManager(
    private val context: Context,
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
    private var jobId = 0
    var buildState by mutableStateOf<BuildState>(BuildState.Unknown)
        private set
    var logs = mutableStateListOf<BuildLog>()
        private set

    init {
        scope.launch { registerToWebSocketStateChanges() }
        scope.launch { registerToWebSocketMessages() }
    }

    /**
     * Initiates a websocket connection to obtain deployment messages.
     */
    private fun connect() {
        deploymentWebSocket.connect()
    }

    /**
     * Disconnect from the deployment websocket
     */
    private fun disconnect() {
        deploymentWebSocket.disconnect()
    }

    private suspend fun registerToWebSocketStateChanges() {
        deploymentWebSocket.stateAsFlow().collect { webSocketState ->
            when (webSocketState) {
                is WebSocketState.Open -> {
                    // We need to start sending pings for the deployment websocket.
                    // NOTE: This is is not needed in the DataAcquisitionWebsocket.
                    deploymentWebSocket.startPinging()
                }
                is WebSocketState.Closing -> {
                    // We need to stop pinging when the WebSocket is closing.
                    deploymentWebSocket.stopPinging()
                }
                is WebSocketState.Closed -> {
                }
                is WebSocketState.Failed -> {
                    // We need to stop pinging when the WebSocket throws an error.
                    deploymentWebSocket.stopPinging()
                    buildState = BuildState.Error(webSocketState.throwable.message)
                }
            }
        }
    }

    private suspend fun registerToWebSocketMessages() {
        deploymentWebSocket.messageAsFlow().collect { message ->
            JsonParser.parseString(regex.replace(message, "")).let { jsonElement ->
                if (jsonElement.isJsonObject) {
                    // TODO this log message can be ignored but needs clarification
                    //_logs.emit(gson.fromJson(it.asJsonObject, BuildLog::class.java))
                } else if (jsonElement.isJsonArray) {
                    jsonElement.asJsonArray.let { jsonArray ->
                        when (jsonArray.first().asString) {
                            "job-data-$jobId" -> {
                                gson.fromJson(
                                    jsonArray[1] as JsonObject,
                                    BuildLog.Data::class.java
                                )?.takeIf { dataLog ->
                                    dataLog.data != "\n"
                                }?.let { data ->
                                    logs.add(data)
                                }
                            }
                            "job-finished-$jobId" -> {
                                gson.fromJson(
                                    jsonArray[1] as JsonObject,
                                    BuildLog.Finished::class.java
                                )?.let { finished ->
                                    logs.add(finished)
                                    //Set the build state before disconnecting
                                    buildState = when (finished.success) {
                                        true -> BuildState.Finished
                                        false -> BuildState.Error(reason = "Error while building")
                                    }
                                    disconnect()
                                }
                            }
                            else -> {

                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Calls buildOnDeviceModel api on the Edge impulse backend
     * @param buildOnDeviceModel buildOnDeviceModel API call as a suspending lambda.
     */
    fun build(buildOnDeviceModel: suspend () -> BuildOnDeviceModelResponse) {
        // Establish a socket connection right before calling build to avoid timeout
        connect()
        scope.launch {
            buildState = BuildState.Started
            buildOnDeviceModel().let { response ->
                guard(response.success) {
                    // Disconnect the websocket in case the build command fails
                    disconnect()
                    throw Throwable(response.error)
                }
                jobId = response.id
            }
        }
    }
}

private val regex = Regex("^[0-9]+")