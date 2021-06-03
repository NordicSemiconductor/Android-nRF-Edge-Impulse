package no.nordicsemi.android.ei.util

import com.google.gson.*
import no.nordicsemi.android.ei.model.*
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
                is Hello -> {
                    deviceMessage.add("hello", context?.serialize(message))
                    deviceMessage
                }
                is Success -> {
                    context?.serialize(message)!!
                }
                is Error -> {
                    context?.serialize(message)!!
                }
                is SampleRequest -> {
                    deviceMessage.add("sample", context?.serialize(message))
                    deviceMessage
                }
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