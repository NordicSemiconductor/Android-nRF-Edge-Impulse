package no.nordicsemi.android.ei.model

import androidx.annotation.IntRange
import com.google.gson.annotations.SerializedName

data class Headers(
    @SerializedName("x-api-key")
    val xApiKey: String,
    @SerializedName("wave")
    val xLabel: String,
    @IntRange(from = 0, to = 1)
    @SerializedName("x-allow-duplicates")
    val xAllowDuplicates: Int
)
