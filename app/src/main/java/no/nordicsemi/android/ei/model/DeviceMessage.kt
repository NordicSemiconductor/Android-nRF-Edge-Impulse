package no.nordicsemi.android.ei.model

import no.nordicsemi.android.ei.model.Direction.SEND
import no.nordicsemi.android.ei.model.Method.POST
import no.nordicsemi.android.ei.model.Type.*

abstract class DeviceMessage {
    abstract val type: Type
}

data class WebSocketMessage(
    val direction: Direction = SEND,
    val address: String = "wss://studio.edgeimpulse.com",
    val message: Message
) : DeviceMessage() {
    override val type = WEBSOCKET
}

data class ConfigureMessage(
    val message: Message
) : DeviceMessage() {
    override val type = CONFIGURE
}

@Suppress("ArrayInDataClass")
data class SendDataMessage(
    val address: String = "https://ingestion.edgeimpulse.com/api/training/data",
    val method: Method = POST,
    val headers: Headers = Headers(),
    val body: ByteArray
) : DeviceMessage() {
    override val type = HTTP
}