/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.util

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import no.nordicsemi.android.ei.model.Message
import no.nordicsemi.android.ei.model.Message.Configure
import no.nordicsemi.android.ei.model.Message.Error
import no.nordicsemi.android.ei.model.Message.Hello
import no.nordicsemi.android.ei.model.Message.HelloResponse
import no.nordicsemi.android.ei.model.Message.Sample.Finished
import no.nordicsemi.android.ei.model.Message.Sample.ProgressEvent.Processing
import no.nordicsemi.android.ei.model.Message.Sample.ProgressEvent.Reading
import no.nordicsemi.android.ei.model.Message.Sample.ProgressEvent.Started
import no.nordicsemi.android.ei.model.Message.Sample.ProgressEvent.Uploading
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
                is Hello -> {
                    deviceMessage.add("hello", context?.serialize(message))
                    deviceMessage
                }
                is HelloResponse, is Configure -> {
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
        json?.asJsonObject?.let { root ->
            // Check if the element exists before checking if it's a primitive
            when {
                root.has("hello") -> {
                    root.get("hello")?.apply {
                        takeIf { isJsonPrimitive }?.let {
                            return context!!.deserialize(root, HelloResponse::class.java)
                        }
                        return context!!.deserialize(this, Hello::class.java)
                    }
                }
                root.has("apiKey") -> {
                    root.get("apiKey")?.apply {
                        return context!!.deserialize(root, Configure::class.java)
                    }
                }
                root.has("sample") -> {
                    root.get("sample")?.let { sample ->
                        return when {
                            sample.isJsonPrimitive -> {
                                context!!.deserialize(
                                    root,
                                    Response::class.java
                                )
                            }
                            else -> {
                                sample.asJsonObject?.let {
                                    return when {
                                        it.has("path") -> {
                                            context!!.deserialize(it, Request::class.java)
                                        }
                                        else -> {
                                            throw InvalidParameterException("Type not supported: $typeOfT")
                                        }
                                    }
                                }
                                throw InvalidParameterException("Type not supported: $typeOfT")
                            }
                        }
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
                root.has("err") -> {
                    return context!!.deserialize(root, Error::class.java)
                }
                else -> {
                    throw InvalidParameterException("Type not supported: $typeOfT")
                }
            }
        }
        throw InvalidParameterException("Type not supported: $typeOfT")
    }
}