package no.nordicsemi.android.ei.websocket

import okhttp3.Response

sealed class WebSocketState {
    data class OnOpen(val response: Response) : WebSocketState()
    data class OnClosing(val code: Int, val reason: String) : WebSocketState()
    data class OnClosed(val code: Int, val reason: String) :
        WebSocketState()

    data class OnFailure(val throwable: Throwable, val response: Response?) :
        WebSocketState()
}
