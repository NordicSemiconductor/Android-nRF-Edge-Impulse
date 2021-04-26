package no.nordicsemi.android.ei.model

data class Device(
    val id: Int,
    val deviceId: String,
    val created: String,
    val lastSeen: String,
    val name: String,
    val deviceType: String,
    val sensors: List<Sensor>,
    val remote_mgmt_connected: Boolean,
    val supportsSnapshotStreaming: Boolean
)
