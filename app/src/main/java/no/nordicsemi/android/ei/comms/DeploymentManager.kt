package no.nordicsemi.android.ei.comms

import android.util.Log
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
    private val scope: CoroutineScope,
    private val gson: Gson,
    var jobId: Int,
    socketToken: SocketToken,
    client: OkHttpClient
) {
    private var deploymentWebSocket: EiWebSocket = EiWebSocket(
        client = client,
        request = Request.Builder()
            .url("wss://studio.edgeimpulse.com/socket.io/?transport=websocket&EIO=3&token=${socketToken.socketToken}")
            .build()
    )
    var buildState by mutableStateOf<BuildState>(BuildState.Finished)
        private set
    var logs = mutableStateListOf<BuildLog>()
        private set

    init {
        scope.launch { registerToWebSocketStateChanges() }
        scope.launch { registerToWebSocketMessages() }
    }

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

    /**
     * Initiates a websocket connection to obtain deployment messages.
     */
    private fun connect() {
        scope.launch {
            deploymentWebSocket.connect()
        }
    }

    /**
     * Disconnect from the deployment websocket
     */
    private fun disconnect() {
        scope.launch {
            // Close the Web Socket if it's open.
            deploymentWebSocket.disconnect()
        }
    }

    private suspend fun registerToWebSocketStateChanges() {
        deploymentWebSocket.stateAsFlow().collect { webSocketState ->
            when (webSocketState) {
                is WebSocketState.Closed -> {
                    Log.d("AAAA", "Did we get a close?")
                    buildState = BuildState.Finished
                }
                is WebSocketState.Failed -> {
                    buildState = BuildState.Error(webSocketState.throwable.message)
                }
                else -> {
                }
            }
        }
    }

    private suspend fun registerToWebSocketMessages() {
        deploymentWebSocket.messageAsFlow().collect { message ->
            JsonParser.parseString(regex.replace(message, "")).let { jsonElement ->
                if (jsonElement.isJsonObject) {
                    // TODO this log message can be ignored but needs claficiation
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
                                }?.also { success ->
                                    buildState = when (success) {
                                        true -> BuildState.Finished
                                        false -> BuildState.Error(reason = "Error while building")
                                    }
                                }
                                disconnect()
                            }
                            else -> {

                            }
                        }
                    }
                }
            }
        }
    }
}

private val regex = Regex("^[0-9]+")