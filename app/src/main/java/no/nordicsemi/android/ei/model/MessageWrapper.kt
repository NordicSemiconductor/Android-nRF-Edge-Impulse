package no.nordicsemi.android.ei.model

import no.nordicsemi.android.ei.model.Direction.Send

data class MessageWrapper(
    val type: String = "ws",
    val direction: String = Send.direction,
    val address: String = "wss://studio.edgeimpulse.com",
    val message: Message
)