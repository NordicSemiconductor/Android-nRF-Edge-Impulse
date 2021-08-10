package no.nordicsemi.android.ei.model

import no.nordicsemi.android.ei.model.Direction.RECEIVE
import no.nordicsemi.android.ei.model.Direction.SEND
import no.nordicsemi.android.ei.model.Method.POST
import no.nordicsemi.android.ei.model.Type.*

sealed class DeviceMessage {
    abstract val type: Type
}

object InvalidMessage : DeviceMessage() {
    override val type = INVALID
}

data class WebSocketMessage(
    val direction: Direction = SEND,
    val address: String = "wss://studio.edgeimpulse.com",
    val message: Message
) : DeviceMessage() {
    override val type = WEBSOCKET
}

data class ConfigureMessage(
    val direction: Direction = RECEIVE,
    val message: Message
) : DeviceMessage() {
    override val type = CONFIGURE
}

data class SendDataMessage(
    val address: String = "https://ingestion.edgeimpulse.com/api/training/data",
    val method: Method = POST,
    val headers: Headers,
    val body: String
) : DeviceMessage() {
    override val type = HTTP
}