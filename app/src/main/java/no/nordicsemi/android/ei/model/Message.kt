package no.nordicsemi.android.ei.model

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

    data class Success(val hello: Boolean) : Message()

    data class Error(val hello: Boolean, val error: String = "Unknown error!") : Message()

    data class Configure(
        val apiKey: String,
        val address: String
    ) : Message()

    data class SampleRequest(
        val label: String = "wave",
        val length: Int = 10000,
        val path: String = "/api/training/data",
        val hmacKey: String = "e561ff...",
        val interval: Int = 10,
        val sensor: String = "Accelerometer"
    ) : Message()
}