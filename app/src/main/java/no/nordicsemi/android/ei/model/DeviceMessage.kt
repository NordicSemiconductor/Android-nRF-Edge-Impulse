package no.nordicsemi.android.ei.model

import com.google.gson.annotations.SerializedName
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

data class DataSample(
    val address: String = "https://ingestion.edgeimpulse.com/api/training/data",
    val method: Method = POST,
    val headers: Headers,
    val body: String
) : DeviceMessage() {
    override val type = HTTP

    data class Headers(
        @SerializedName("x-api-key")
        val xApiKey: String,
        @SerializedName("x-label")
        val xLabel: String,
        @SerializedName("x-file-name")
        val xFileName: String,
        @SerializedName("Content-Type")
        val contentType: String,
        @SerializedName("x-disallow-duplicates")
        val xDisallowDuplicates: Int
    )
}

sealed class InferencingMessage : DeviceMessage() {

    sealed class InferencingRequest : DeviceMessage() {
        class Start : InferencingRequest() {
            override val type: Type = START_INFERENCING
        }

        class Stop : InferencingRequest() {
            override val type: Type = STOP_INFERENCING
        }
    }

    sealed class InferencingResponse : DeviceMessage() {
        data class Start(
            val ok: Boolean,
            val error: String?
        ) : InferencingRequest() {
            override val type: Type = START_INFERENCING_RESPONSE
        }

        data class Stop(
            val ok: Boolean,
            val error: String?
        ) : InferencingRequest() {
            override val type: Type = STOP_INFERENCING_RESPONSE
        }
    }

    data class InferenceResults(
        val classification: List<Classification>,
        val anomaly: Double
    ) : DeviceMessage() {
        override val type: Type = INFERENCING_RESULTS
    }
}