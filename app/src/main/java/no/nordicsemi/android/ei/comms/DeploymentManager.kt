package no.nordicsemi.android.ei.comms

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.model.BuildLog
import no.nordicsemi.android.ei.model.SocketToken
import no.nordicsemi.android.ei.websocket.EiWebSocket
import no.nordicsemi.android.ei.websocket.WebSocketState
import okhttp3.OkHttpClient
import okhttp3.Request

@OptIn(ExperimentalCoroutinesApi::class)
class DeploymentManager(
    private val scope: CoroutineScope,
    private val gson: Gson,
    val jobId: Int,
    socketToken: SocketToken,
    client: OkHttpClient
) {
    private var deploymentWebSocket: EiWebSocket = EiWebSocket(
        client = client,
        request = Request.Builder()
            .url("wss://studio.edgeimpulse.com/socket.io/?transport=websocket&EIO=3&token=${socketToken.socketToken}")
            .build()
    )

    private val _logs = MutableSharedFlow<BuildLog>()

    /**
     * Returns the messages received via the WebSocket as flow.
     */
    fun logsAsFlow(): Flow<BuildLog> = _logs

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
            when (webSocketState) {
                is WebSocketState.Open -> {
                }
                is WebSocketState.Closed -> {
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
                                )?.takeIf { data ->
                                    data.data != "\n"
                                }?.let {
                                    _logs.emit(it)
                                }
                            }
                            "job-finished-$jobId" -> {
                                val o = jsonArray[1] as JsonObject
                                val l = gson.fromJson(
                                    o,
                                    BuildLog.Finished::class.java
                                )
                                _logs.emit(l)
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

private const val MESSAGE = "message"
private val regex = Regex("^[0-9]+")