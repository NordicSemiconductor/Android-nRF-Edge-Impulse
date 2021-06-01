package no.nordicsemi.android.ei.model

data class MessageWrapper(
    val type: String = "ws",
    val direction: String = "tx",
    val address: String = "wss://studio.edgeimpulse.com",
    val message: Message
)