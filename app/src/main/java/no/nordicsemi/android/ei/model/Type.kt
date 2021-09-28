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
    HTTP,

    @SerializedName("invalid")
    INVALID,

    @SerializedName("start-inferencing")
    START_INFERENCING,

    @SerializedName("start-inferencing-respons")
    START_INFERENCING_RESPONSE,

    @SerializedName("stop-inferencing")
    STOP_INFERENCING,

    @SerializedName("stop-inferencing-response")
    STOP_INFERENCING_RESPONSE,

    @SerializedName("inference-results")
    INFERENCING_RESULTS

}