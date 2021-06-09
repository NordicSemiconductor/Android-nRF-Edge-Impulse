package no.nordicsemi.android.ei.websocket

import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.di.DefaultDispatcher
import no.nordicsemi.android.ei.websocket.WebSocketEvent.*
import okhttp3.*
import javax.inject.Inject

/**
 * WebSocketManager
 */
class WebSocketManager @Inject constructor(
    private val client: OkHttpClient,
    private val request: Request,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    private lateinit var webSocket: WebSocket
    private val _socketState = MutableSharedFlow<WebSocketEvent>()
    val socketState: SharedFlow<WebSocketEvent> = _socketState
    private val coroutineScope = CoroutineScope(defaultDispatcher)

    private val webSocketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            coroutineScope.launch {
                _socketState.emit(OnOpen(response = response))
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            coroutineScope.launch {
                _socketState.emit(OnMessage( text = text))
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            coroutineScope.launch {
                _socketState.emit(
                    OnClosing(
                        code = code,
                        reason = reason
                    )
                )
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            coroutineScope.launch {
                _socketState.emit(OnClosed(code = code, reason = reason))
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            coroutineScope.launch {
                _socketState.emit(OnFailure(throwable = t, response = response))
            }
        }
    }

    fun connect() {
        webSocket = client.newWebSocket(request = request, listener = webSocketListener)
    }

    suspend fun send(deviceId: String, json: JsonObject) = withContext(defaultDispatcher) {
        webSocket.send(text = json.toString()) ?: false
    }

    //TODO verify reasoning
    suspend fun disconnect(deviceId: String) = withContext(defaultDispatcher) {
        webSocket.close(WebSocketStatus.NORMAL_CLOSURE.ordinal, "Finished") ?: false
    }
}
