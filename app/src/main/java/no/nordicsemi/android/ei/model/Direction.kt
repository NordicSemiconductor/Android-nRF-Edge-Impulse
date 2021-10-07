package no.nordicsemi.android.ei.model

import com.google.gson.annotations.SerializedName

/**
 * Direction
 */
enum class Direction {
    @SerializedName("rx")
    RECEIVE,

    @SerializedName("tx")
    SEND
}
