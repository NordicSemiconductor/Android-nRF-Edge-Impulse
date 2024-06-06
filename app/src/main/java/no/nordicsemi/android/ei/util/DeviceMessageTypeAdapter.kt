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
import no.nordicsemi.android.ei.model.ConfigureMessage
import no.nordicsemi.android.ei.model.DataSample
import no.nordicsemi.android.ei.model.DeviceMessage
import no.nordicsemi.android.ei.model.InferencingMessage.InferenceResults
import no.nordicsemi.android.ei.model.InferencingMessage.InferencingRequest
import no.nordicsemi.android.ei.model.InferencingMessage.InferencingResponse
import no.nordicsemi.android.ei.model.InvalidMessage
import no.nordicsemi.android.ei.model.WebSocketMessage
import java.lang.reflect.Type

class DeviceMessageTypeAdapter : JsonDeserializer<DeviceMessage> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): DeviceMessage = json?.asJsonObject?.let { root ->
        val targetType: Type = when (root.get("type")?.asString) {
            "ws" -> WebSocketMessage::class.java
            "configure" -> ConfigureMessage::class.java
            "http" -> {
                DataSample::class.java
            }
            "start-inferencing" -> {
                InferencingRequest::class.java
            }
            "start-inferencing-response" -> {
                InferencingResponse.Start::class.java
            }
            "stop-inferencing-response" -> {
                InferencingResponse.Stop::class.java
            }
            "inference-results" -> {
                InferenceResults::class.java
            }
            else -> return InvalidMessage
        }
        return context!!.deserialize(json, targetType)
    } ?: InvalidMessage
}