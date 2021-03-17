package no.nordicsemi.android.ei.service.param

/**
 * Response body for a GetDevelopmentKeysRequest
 */
data class DevelopmentKeysResponse(
    val apiKey: String = "undefined",
    val hmacKey: String = "undefined",
    val success: Boolean,
    val error: String,
)
