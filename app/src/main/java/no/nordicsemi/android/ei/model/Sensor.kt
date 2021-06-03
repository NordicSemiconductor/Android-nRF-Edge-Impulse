package no.nordicsemi.android.ei.model

import com.google.gson.annotations.SerializedName

data class Sensor(
    val name: String,
    @SerializedName("maxSampleLengthS")
    val maxSampleLengths: Int,
    val frequencies: List<Number>
)
