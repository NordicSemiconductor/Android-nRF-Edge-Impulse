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

    /**
     * Progress event messages
     *
     * @see https://docs.edgeimpulse.com/reference#remote-management
     */
    sealed class Sample : Message() {
        data class Request(
            val label: String,
            val length: Int,
            val path: String = "/api/training/data",
            val hmacKey: String,
            val interval: Int,
            val sensor: String
        ) : Sample()

        data class Response(
            val sample: Boolean,
            val error: String? = null
        ) : Sample()

        sealed class ProgressEvent : Sample() {

            /**
             * Indicates that the device started sampling. Send the following message right when sampling starts:
             */
            data class Started(val sampleStarted: Boolean) : ProgressEvent()

            /**
             * If the device is done sampling, but is processing the sample before uploading,
             * such as a device preprocessing audio or signing the file. Send the following message
             * to indicate that the device is processing:
             */
            data class Processing(val sampleProcessing: Boolean) : ProgressEvent()

            /**
             * If the device is not connected directly to the internet, the daemon needs to pull the data
             * from the device over serial, which could be slow for large files. Send the following message
             * to indicate that this process started, and how far the process is along:
             */
            data class Reading(val sampleReading: Boolean, val progressPercentage: Int) :
                ProgressEvent()

            /**
             * Before you start the upload, send:
             */
            data class Uploading(val sampleUploading: Boolean) : ProgressEvent()

            data class Finished(
                val sampleFinished: Boolean
            ) : Sample()
        }

        object Unknown : Sample()
    }
}