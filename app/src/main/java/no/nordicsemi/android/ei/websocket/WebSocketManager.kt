package no.nordicsemi.android.ei.websocket

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.di.DefaultDispatcher
import no.nordicsemi.android.ei.websocket.WebSocketEvent.*
import okhttp3.*
import okio.ByteString.Companion.toByteString
import javax.inject.Inject

/**
 * WebSocketManager
 */
class WebSocketManager @Inject constructor(
    private val client: OkHttpClient,
    private val request: Request,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    private val webSockets = mutableMapOf<String, WebSocket>()
    private val _socketState = MutableSharedFlow<WebSocketEvent>()
    val socketState: SharedFlow<WebSocketEvent> = _socketState
    private val coroutineScope = CoroutineScope(defaultDispatcher)

    fun connect(deviceId: String) {
        webSockets[deviceId] =
            client.newWebSocket(request = request, listener = object : WebSocketListener() {

                override fun onOpen(webSocket: WebSocket, response: Response) {
                    coroutineScope.launch {
                        getDevice(webSocket = webSocket)?.let {
                            _socketState.emit(OnOpen(deviceId = it, response = response))
                        }
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    coroutineScope.launch {
                        getDevice(webSocket = webSocket)?.let {
                            _socketState.emit(OnMessage(deviceId = it, text = text))
                        }
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    coroutineScope.launch {
                        getDevice(webSocket = webSocket)?.let {
                            _socketState.emit(
                                OnClosing(
                                    deviceId = it,
                                    code = code,
                                    reason = reason
                                )
                            )
                        }
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    coroutineScope.launch {
                        getDevice(webSocket = webSocket)?.let {
                            _socketState.emit(OnClosed(deviceId = it, code = code, reason = reason))
                        }
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    coroutineScope.launch {
                        getDevice(webSocket = webSocket)?.let {
                            _socketState.emit(OnFailure(deviceId = it, t = t, response = response))
                        }
                    }
                }
            })
    }

    suspend fun send(deviceId: String, data: ByteArray) = withContext(defaultDispatcher) {
        webSockets[deviceId]?.send(data.toByteString()) ?: false
    }

    //TODO verify reasoning
    suspend fun disconnect(deviceId: String) = withContext(defaultDispatcher) {
        webSockets[deviceId]?.close(WebSocketStatus.NORMAL_CLOSURE.ordinal, "Finished") ?: false
    }

    private fun getDevice(webSocket: WebSocket): String? {
        webSockets.forEach { entry ->
            entry.takeIf {
                it.value == webSocket
            }?.let {
                return it.key
            }
        }
        return null
    }
}
