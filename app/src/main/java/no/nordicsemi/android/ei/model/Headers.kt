package no.nordicsemi.android.ei.model

import androidx.annotation.IntRange
import com.google.gson.annotations.SerializedName

data class Headers(
    @SerializedName("x-api-key")
    val xApiKey: String,
    @SerializedName("x-label")
    val xLabel: String,
    @SerializedName("x-file-name")
    val xFileName: String,
    @IntRange(from = 0, to = 1)
    @SerializedName("x-disallow-duplicates")
    val xDisallowDuplicates: Int = 0
)
