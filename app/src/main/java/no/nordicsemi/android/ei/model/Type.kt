package no.nordicsemi.android.ei.model

import com.google.gson.annotations.SerializedName

/**
 * Direction
 */
enum class Type {
    @SerializedName("ws")
    WEBSOCKET,

    @SerializedName("configure")
    CONFIGURE,

    @SerializedName("http")
    HTTP
}
