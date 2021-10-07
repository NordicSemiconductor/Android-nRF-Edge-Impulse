package no.nordicsemi.android.ei.model

import com.google.gson.annotations.SerializedName

data class Device(
    val id: Int,
    val deviceId: String,
    val created: String,
    val lastSeen: String,
    val name: String,
    val deviceType: String,
    val sensors: List<Sensor>,
    @SerializedName("remote_mgmt_connected")
    val remoteMgmtConnected: Boolean,
    val supportsSnapshotStreaming: Boolean
)
