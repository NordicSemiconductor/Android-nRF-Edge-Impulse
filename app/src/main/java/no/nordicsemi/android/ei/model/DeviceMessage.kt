package no.nordicsemi.android.ei.model

import no.nordicsemi.android.ei.model.Direction.Send
import no.nordicsemi.android.ei.model.Type.WebSocket

data class DeviceMessage(
    val type: Type = WebSocket,
    val direction: Direction = Send,
    val address: String = "wss://studio.edgeimpulse.com",
    val message: Message
)