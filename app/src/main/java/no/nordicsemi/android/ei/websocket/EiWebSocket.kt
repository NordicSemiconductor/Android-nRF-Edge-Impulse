package no.nordicsemi.android.ei.websocket

import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import no.nordicsemi.android.ei.websocket.WebSocketState.*
import okhttp3.*
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
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    private val _message =
        MutableSharedFlow<JsonObject>(
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    private val webSocketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.i("AAAA", "onOpen webSocket")
            _webSocketState.tryEmit(Open(response = response))
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.i("AAAA", "onMessage webSocket: $text")
            try {
                _message.tryEmit(JsonParser.parseString(text).asJsonObject)
            } catch (e: Exception) {

            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.i("AAAA", "onClosing webSocket: $reason ($code)")
            _webSocketState.tryEmit(Closing(code = code, reason = reason))
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.i("AAAA", "onClosed webSocket: $reason ($code)")
            _webSocketState.tryEmit(Closed(code = code, reason = reason))
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.i("AAAA", "onFailure webSocket", t)
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
    fun messageAsFlow(): Flow<JsonObject> = _message

    /**
     * Connects to EI websocket.
     */
    fun connect() {
        Log.i("AAAA", "Connecting to webSocket")
        webSocket = client.newWebSocket(request = request, listener = webSocketListener)
    }

    /**
     * Send data to websocket.
     * @param json Json message
     */
    fun send(json: JsonElement) {
        Log.i("AAAA", "Sending to webSocket?")
        webSocket?.send(text = json.toString())
    }

    /**
     * Disconnect websocket
     */
    //TODO verify reasoning
    fun disconnect() {
        Log.i("AAAA", "Disconnecting from webSocket")
        webSocket?.close(WebSocketStatus.NORMAL_CLOSURE.code, "Finished")
    }
}
