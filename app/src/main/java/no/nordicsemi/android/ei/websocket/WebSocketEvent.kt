package no.nordicsemi.android.ei.websocket

import okhttp3.Response

sealed class WebSocketEvent {
    data class OnOpen(val deviceId: String, val response: Response) : WebSocketEvent()
    data class OnMessage(val deviceId: String, val text: String) : WebSocketEvent()
    data class OnClosing(val deviceId: String, val code: Int, val reason: String) : WebSocketEvent()
    data class OnClosed(val deviceId: String, val code: Int, val reason: String) :
        WebSocketEvent()

    data class OnFailure(val deviceId: String, val t: Throwable, val response: Response?) :
        WebSocketEvent()
}
