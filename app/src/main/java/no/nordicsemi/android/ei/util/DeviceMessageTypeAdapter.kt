package no.nordicsemi.android.ei.util

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import no.nordicsemi.android.ei.model.*
import java.lang.reflect.Type

class DeviceMessageTypeAdapter: JsonDeserializer<DeviceMessage> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): DeviceMessage = json?.asJsonObject?.let { root ->
        val targetType: Type = when (root.get("type")?.asString) {
            "ws" -> WebSocketMessage::class.java
            "configure" -> ConfigureMessage::class.java
            "http" -> SendDataMessage::class.java
            else -> return InvalidMessage
        }
        return context!!.deserialize(json, targetType)
    } ?: InvalidMessage
}