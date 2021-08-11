package no.nordicsemi.android.ei.util

import android.util.Log
import com.google.gson.*
import no.nordicsemi.android.ei.model.Message
import no.nordicsemi.android.ei.model.Message.Sample.ProgressEvent.*
import no.nordicsemi.android.ei.model.Message.Sample.Request
import no.nordicsemi.android.ei.model.Message.Sample.Response
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
                is Request -> {
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
                root.has("apiKey") -> {
                    root.get("apiKey")?.apply {
                        return context!!.deserialize(root, Message.Configure::class.java)
                    }
                }
                root.has("sample") -> {
                    root.get("sample")?.apply {
                        takeIf { isJsonPrimitive }?.let {
                            return context!!.deserialize(
                                root,
                                Response::class.java
                            )
                        }
                        return context!!.deserialize(this,Response::class.java)
                    }
                }
                root.has("sampleStarted") -> {
                    root.get("sampleStarted")?.apply {
                        takeIf { isJsonPrimitive }?.let {
                            return context!!.deserialize(root, Started::class.java)
                        }
                    }
                }
                root.has("sampleProcessing") -> {
                    root.get("sampleProcessing")?.apply {
                        takeIf { isJsonPrimitive }?.let {
                            return context!!.deserialize(
                                root,
                                Processing::class.java
                            )
                        }
                    }
                }
                root.has("sampleReading") -> {
                    root.get("sampleReading")?.apply {
                        takeIf { isJsonPrimitive }?.let {
                            return context!!.deserialize(root, Reading::class.java)
                        }
                    }
                }
                root.has("sampleUploading") -> {
                    root.get("sampleUploading")?.apply {
                        takeIf { isJsonPrimitive }?.let {
                            return context!!.deserialize(root, Uploading::class.java)
                        }
                    }
                }
                root.has("sampleFinished") -> {
                    root.get("sampleFinished")?.apply {
                        takeIf { isJsonPrimitive }?.let {
                            return context!!.deserialize(root, Finished::class.java)
                        }
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