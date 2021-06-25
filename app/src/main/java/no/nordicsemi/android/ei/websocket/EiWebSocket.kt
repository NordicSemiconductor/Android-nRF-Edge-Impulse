package no.nordicsemi.android.ei.websocket

import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.websocket.WebSocketState.*
import okhttp3.*
import javax.inject.Inject

/**
 * WebSocketManager
 */
class EiWebSocket @Inject constructor(
    private val client: OkHttpClient,
    private val request: Request,
    private val coroutineScope: CoroutineScope,
) {
    private var webSocket: WebSocket? = null
    private val _webSocketState = MutableSharedFlow<WebSocketState>()
    private val _message = MutableSharedFlow<JsonObject>()

    private val webSocketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.i("AAAA", "onOpen webSocket")
            coroutineScope.launch {
                _webSocketState.emit(Open(response = response))
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.i("AAAA", "onMessage webSocket: $text")
            coroutineScope.launch {
                _message.emit(JsonParser.parseString(text).asJsonObject)
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.i("AAAA", "onClosing webSocket: $reason ($code)")
            coroutineScope.launch {
                _webSocketState.emit(Closing(code = code, reason = reason))
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.i("AAAA", "onClosed webSocket: $reason ($code)")
            coroutineScope.launch {
                _webSocketState.emit(Closed(code = code, reason = reason))
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.i("AAAA", "onFailure webSocket", t)
            coroutineScope.launch {
                _webSocketState.emit(Failed(throwable = t, response = response))
            }
        }
    }

    fun stateAsFlow(): Flow<WebSocketState> = _webSocketState

    fun messageAsFlow(): Flow<JsonObject> = _message

    fun connect() {
        Log.i("AAAA", "Connecting to webSocket")
        webSocket = client.newWebSocket(request = request, listener = webSocketListener)
    }

    fun send(json: JsonElement) {
        Log.i("AAAA", "Sending to webSocket?")
        webSocket?.send(text = json.toString())
    }

    //TODO verify reasoning
    fun disconnect() {
        Log.i("AAAA", "Disconnecting from webSocket")
        webSocket?.close(WebSocketStatus.NORMAL_CLOSURE.code, "Finished")
    }
}
