package no.nordicsemi.android.ei.model

import com.google.gson.annotations.SerializedName

sealed class Message {

    data class Hello(
        val version: Int = 3,
        val apiKey: String,
        val deviceId: String,
        val deviceType: String,
        val connection: String,
        val sensors: List<Sensor>,
        val supportsSnapshotStreaming: Boolean = false
    ) : Message()

    data class HelloResponse(
        val hello: Boolean,
        @SerializedName(value = "err") val error: String? = null
    ) : Message()

    data class Configure(
        val apiKey: String,
        val address: String
    ) : Message()

    data class SampleRequest(
        val label: String,
        val length: Int,
        val path: String,
        val hmacKey: String,
        val interval: Int,
        val sensor: String
    ) : Message()

    data class SampleRequestResponse(
        val sample: Boolean,
        val error: String? = null
    ) : Message()
}