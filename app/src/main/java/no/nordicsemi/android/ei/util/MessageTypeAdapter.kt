package no.nordicsemi.android.ei.util

import com.google.gson.*
import no.nordicsemi.android.ei.model.Message
import java.lang.reflect.Type

class MessageTypeAdapter : JsonSerializer<Message>, JsonDeserializer<Message> {

    override fun serialize(
        src: Message?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val deviceMessage = JsonObject()
        return src?.let { message ->
            when (message) {
                is Message.Hello -> {
                    deviceMessage.add("hello", context?.serialize(message))
                    deviceMessage
                }
                is Message.HelloResponse, is Message.Configure -> {
                    context?.serialize(message)!!
                }
                is Message.SampleRequest -> {
                    deviceMessage.add("sample", context?.serialize(message))
                    deviceMessage
                }
                //TODO check other messages
                else -> {
                    deviceMessage
                }
            }
        } ?: deviceMessage
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Message {
        TODO("Not yet implemented")
    }
}