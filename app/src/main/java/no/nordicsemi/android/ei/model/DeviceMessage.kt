package no.nordicsemi.android.ei.model

//TODO make Message a sealed interface when compose supports kotlin 1.5.10
abstract class DeviceMessage

data class Hello(
    val version: Int = 3,
    val apiKey: String = "ei_1234",
    val deviceId: String = "01:23:45:67:89:AA",
    val deviceType: String = "NRF5340_DK",
    val connection: String = "ip",
    val sensors: List<Sensor> = listOf(
        Sensor(
            name = "Accelerometer",
            maxSampleLengths = 60000,
            frequencies = listOf(62.5, 100)
        ),
        Sensor(
            name = "Microphone",
            maxSampleLengths = 4000,
            frequencies = listOf(16000)
        )
    ),
    val supportsSnapshotStreaming: Boolean = false
) : DeviceMessage()

data class Success(val hello: Boolean) : DeviceMessage()

data class Error(val hello: Boolean, val error: String = "Unknown error!") : DeviceMessage()

data class SampleRequest(
    val label: String,
    val length: Int = 1000,
    val path: String = "/api/training/data"
) : DeviceMessage()