package no.nordicsemi.android.ei.model

import no.nordicsemi.android.ei.model.Direction.Send
import no.nordicsemi.android.ei.model.Type.WebSocket

abstract class DeviceMessage {
    abstract val type: Type
}

data class WebSocketMessage(
    val direction: Direction = Send,
    val address: String = "wss://studio.edgeimpulse.com",
    val message: Message
) : DeviceMessage() {
    override val type: Type = WebSocket
}

data class ConfigureMessage(
    val message: Message
) : DeviceMessage() {
    override val type: Type = Type.Configure
}