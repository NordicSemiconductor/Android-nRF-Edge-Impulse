package no.nordicsemi.android.ei.util

import com.google.gson.*
import no.nordicsemi.android.ei.model.*
import java.lang.reflect.Type

class DeviceMessageTypeAdapter: JsonSerializer<DeviceMessage>, JsonDeserializer<DeviceMessage> {

    override fun serialize(
        src: DeviceMessage?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        TODO("Not yet implemented")
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): DeviceMessage {
        val root = json?.asJsonObject
        val type = root?.get("type")?.asString
        val targetType: Type = when (type) {
            "ws" -> WebSocketMessage::class.java
            "configure" -> ConfigureMessage::class.java
            "http" -> SendDataMessage::class.java
            else -> return InvalidMessage
        }
        return context!!.deserialize(json, targetType)
    }
}