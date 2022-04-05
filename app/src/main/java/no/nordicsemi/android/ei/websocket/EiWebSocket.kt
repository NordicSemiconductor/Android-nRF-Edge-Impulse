package no.nordicsemi.android.ei.websocket

import com.google.gson.JsonElement
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import no.nordicsemi.android.ei.websocket.WebSocketState.*
import okhttp3.*
import java.util.*
import javax.inject.Inject

/**
 * WebSocketManager
 */
class EiWebSocket @Inject constructor(
    private val client: OkHttpClient,
    private val request: Request
) {
    private var webSocket: WebSocket? = null
    private val _webSocketState =
        MutableSharedFlow<WebSocketState>(
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    private val _message =
        MutableSharedFlow<String>(
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    private lateinit var pingTask: TimerTask

    private val webSocketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            _webSocketState.tryEmit(Open(response = response))
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            _message.tryEmit(text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            stopPinging()
            _webSocketState.tryEmit(Closing(code = code, reason = reason))
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            _webSocketState.tryEmit(Closed(code = code, reason = reason))
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            _webSocketState.tryEmit(Failed(throwable = t, response = response))
        }
    }

    /**
     * Returns the state of the WebSocketState as flow.
     */
    fun stateAsFlow(): Flow<WebSocketState> = _webSocketState

    /**
     * Returns the messages received via the WebSocket as flow.
     */
    fun messageAsFlow(): Flow<String> = _message

    /**
     * Connects to EI websocket.
     */
    fun connect() {
        webSocket = client.newWebSocket(request = request, listener = webSocketListener)
    }

    /**
     * Send data to websocket.
     * @param json Json message
     */
    fun send(json: JsonElement) {
        webSocket?.send(text = json.toString())
    }

    /**
     * Disconnect websocket
     */
    //TODO verify reasoning
    fun disconnect() {
        webSocket?.close(WebSocketStatus.NORMAL_CLOSURE.code, "Finished")
    }

    /**
     * Starts pinging every 20 seconds. Note this is only to be used with the Deployment Manager,
     * hence should not be used in the DataAcquisitionManager
     */
    fun startPinging() {
        pingTask = object : TimerTask() {
            override fun run() {
                webSocket?.send("2")
            }
        }
        Timer().scheduleAtFixedRate(pingTask, 20000, 20000)
    }

    /**
     * Stops pinging. Note this is only to be used with the Deployment Manager,
     * hence should not be used in the DataAcquisitionManager
     */
    fun stopPinging() {
        if (this::pingTask.isInitialized)
            pingTask.cancel()
    }
}
