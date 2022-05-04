package no.nordicsemi.android.ei.comms

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.comms.DeploymentState.*
import no.nordicsemi.android.ei.model.BuildLog
import no.nordicsemi.android.ei.model.SocketToken
import no.nordicsemi.android.ei.websocket.EiWebSocket
import no.nordicsemi.android.ei.websocket.WebSocketState
import okhttp3.OkHttpClient
import okhttp3.Request


@OptIn(ExperimentalCoroutinesApi::class)
class BuildManager(
    private val gson: Gson,
    scope: CoroutineScope,
    exceptionHandler: CoroutineExceptionHandler,
    socketToken: SocketToken,
    client: OkHttpClient
) {
    private var deploymentWebSocket: EiWebSocket = EiWebSocket(
        client = client,
        request = Request.Builder()
            .url("wss://studio.edgeimpulse.com/socket.io/?transport=websocket&EIO=3&token=${socketToken.socketToken}")
            .build()
    )
    var jobId = 0
        private set
    private var _buildState = MutableStateFlow<DeploymentState>(NotStarted)

    init {
        scope.launch(exceptionHandler) { registerToWebSocketStateChanges() }
        scope.launch(exceptionHandler) { registerToWebSocketMessages() }
    }

    /**
     * Returns the build state as a flow
     */
    fun buildStateAsFlow(): StateFlow<DeploymentState> = _buildState

    /**
     * Initiates a websocket connection to obtain deployment messages.
     */
    fun start(jobId: Int) {
        this.jobId = jobId
        _buildState.tryEmit(Building)
        deploymentWebSocket.connect()
    }

    /**
     * Disconnect from the deployment websocket
     */
    fun stop() {
        _buildState.tryEmit(Canceled(state = Building))
        disconnect()
    }

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
                    _buildState.tryEmit(Failed(state = Building))
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
                            "job-finished-$jobId" -> {
                                gson.fromJson(
                                    jsonArray[1] as JsonObject,
                                    BuildLog.Finished::class.java
                                )?.let { finished ->
                                    //Set the build state before disconnecting
                                    _buildState.tryEmit(
                                        when (finished.success) {
                                            true -> Downloading
                                            false -> Failed(state = Building)
                                        }
                                    )
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
}

private val regex = Regex("^[0-9]+")