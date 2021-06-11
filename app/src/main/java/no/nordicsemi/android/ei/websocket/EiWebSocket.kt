package no.nordicsemi.android.ei.websocket

import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.di.IODispatcher
import no.nordicsemi.android.ei.websocket.WebSocketState.*
import okhttp3.*
import javax.inject.Inject

/**
 * WebSocketManager
 */
class EiWebSocket @Inject constructor(
    private val client: OkHttpClient,
    private val request: Request,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private lateinit var webSocket: WebSocket
    private val _webSocketState = MutableSharedFlow<WebSocketState>()
    val webSocketState: SharedFlow<WebSocketState> = _webSocketState
    private val _message = MutableSharedFlow<JsonObject>()
    val message: SharedFlow<JsonObject> = _message
    private val coroutineScope = CoroutineScope(ioDispatcher)

    private val webSocketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            coroutineScope.launch {
                _webSocketState.emit(OnOpen(response = response))
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            coroutineScope.launch {
                Log.i("AAAA", "Response $text")
                _message.emit(JsonParser.parseString(text).asJsonObject)
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            coroutineScope.launch {
                _webSocketState.emit(
                    OnClosing(
                        code = code,
                        reason = reason
                    )
                )
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            coroutineScope.launch {
                _webSocketState.emit(OnClosed(code = code, reason = reason))
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            coroutineScope.launch {
                _webSocketState.emit(OnFailure(throwable = t, response = response))
            }
        }
    }

    fun connect() {
        webSocket = client.newWebSocket(request = request, listener = webSocketListener)
    }

    fun send(json: JsonObject) {
        Log.i("AAAA", "Sending?")
        webSocket.send(text = json.toString())
    }

    //TODO verify reasoning
    fun disconnect() {
        webSocket.close(WebSocketStatus.NORMAL_CLOSURE.ordinal, "Finished")
    }
}
