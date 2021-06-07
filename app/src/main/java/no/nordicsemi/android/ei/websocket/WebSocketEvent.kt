package no.nordicsemi.android.ei.websocket

import no.nordicsemi.android.ei.model.Device
import okhttp3.Response
import okio.ByteString

sealed class WebSocketEvent {
    data class OnOpen(val device: Device, val response: Response) : WebSocketEvent()
    data class OnMessage(val device: Device, val bytes: ByteString) : WebSocketEvent()
    data class OnClosing(val device: Device, val code: Int, val reason: String) : WebSocketEvent()
    data class OnClosed(val device: Device, val code: Int, val reason: String) :
        WebSocketEvent()

    data class OnFailure(val device: Device, val t: Throwable, val response: Response?) :
        WebSocketEvent()
}
