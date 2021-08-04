package no.nordicsemi.android.ei.model

import com.google.gson.annotations.SerializedName

sealed class Message {

    data class Hello(
        val version: Int = 3,
        val apiKey: String,
        //TODO device address is hardcoded until it is in place in the fw. Change it to val after
        var deviceId: String,
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
        val address: String = "wss://studio.edgeimpulse.com"
    ) : Message()

    data class SampleRequest(
        val label: String,
        val length: Int,
        val path: String = "/api/training/data",
        val hmacKey: String,
        val interval: Int,
        val sensor: String
    ) : Message()

    data class SampleRequestResponse(
        val sample: Boolean,
        val error: String? = null
    ) : Message()

    data class SampleStarted(
        val sampleStarted: Boolean
    ) : Message()

    data class SampleUploading(
        val sampleUploading: Boolean
    ) : Message()

    data class SampleFinished(
        val sampleFinished: Boolean
    ) : Message()

    /*sealed class Sample : Message() {
        data class Request(
            val label: String,
            val length: Int,
            val path: String = "/api/training/data",
            val hmacKey: String,
            val interval: Int,
            val sensor: String
        ) : Message()

        data class Response(
            val sample: Boolean,
            val error: String? = null
        ) : Message()

        data class Started(
            val sampleStarted: Boolean
        ) : Message()

        data class Uploading(
            val sampleUploading: Boolean
        ) : Message()

        data class Finished(
            val sampleFinished: Boolean
        ) : Message()
    }*/
}