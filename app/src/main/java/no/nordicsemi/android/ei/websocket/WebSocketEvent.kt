package no.nordicsemi.android.ei.websocket

import okhttp3.Response

sealed class WebSocketEvent {
    data class OnOpen(val response: Response) : WebSocketEvent()
    data class OnMessage(val text: String) : WebSocketEvent()
    data class OnClosing(val code: Int, val reason: String) : WebSocketEvent()
    data class OnClosed(val code: Int, val reason: String) :
        WebSocketEvent()

    data class OnFailure(val throwable: Throwable, val response: Response?) :
        WebSocketEvent()
}
