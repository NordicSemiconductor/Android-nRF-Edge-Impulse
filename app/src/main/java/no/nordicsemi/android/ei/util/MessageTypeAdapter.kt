package no.nordicsemi.android.ei.util

import android.util.Log
import com.google.gson.*
import no.nordicsemi.android.ei.model.Message
import java.lang.reflect.Type
import java.security.InvalidParameterException

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
        Log.d("AAAA", "MessageTypeAdapter deserializing: $json")
        json?.asJsonObject?.let { root ->
            // Check if the element exists before checking if it's a primitive
            when {
                root.has("hello") -> {
                    root.get("hello")?.apply {
                        takeIf { isJsonPrimitive }?.let {
                            return context!!.deserialize(root, Message.HelloResponse::class.java)
                        }
                        return context!!.deserialize(this, Message.Hello::class.java)
                    }
                }
                root.has("sample") -> {
                    root.get("sample")?.apply {
                        takeIf { isJsonPrimitive }?.let {
                            return context!!.deserialize(root, Message.SampleRequestResponse::class.java)
                        }
                        return context!!.deserialize(this, Message.SampleRequest::class.java)
                    }
                }
                root.has("sampleStarted") -> {
                    root.get("sampleStarted")?.apply {
                        takeIf { isJsonPrimitive }?.let {
                            return context!!.deserialize(root, Message.SampleStarted::class.java)
                        }
                    }
                }
                root.has("apiKey") -> {
                    root.get("apiKey")?.apply {
                        return context!!.deserialize(root, Message.Configure::class.java)
                    }
                }
                else -> {
                    throw InvalidParameterException("Type not supported: $typeOfT")
                }
            }
        }
        throw InvalidParameterException("Type not supported: $typeOfT")
    }
}