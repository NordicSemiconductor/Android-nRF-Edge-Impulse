package no.nordicsemi.android.ei.websocket

import okhttp3.*
import okio.ByteString
import okio.ByteString.Companion.toByteString
import javax.inject.Inject

/**
 * EiWebSocketListener
 */
class EiWebSocketListener @Inject constructor(
    private val client: OkHttpClient,
    private val request: Request
) : WebSocketListener() {

    private lateinit var webSocket: WebSocket

    /**
     * Start listener
     */
    fun start() {
        webSocket = client.newWebSocket(request = request, this)
    }

    fun send(data: ByteArray) {
        webSocket.send(data.toByteString(0, data.size))
    }

    /**
     * Close listener
     */
    //TODO verify reasoning
    fun close() = webSocket.close(
        WebSocketStatus.CLIENT_SERVER_NEGOTIATION_FAILURE.ordinal,
        "Endpoint closing connection"
    )


    override fun onOpen(webSocket: WebSocket, response: Response) {
        // TODO onOpen
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        // TODO onMessage
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        // TODO onClosing
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        // TODO onClosed
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        // TODO onFailure
    }
}