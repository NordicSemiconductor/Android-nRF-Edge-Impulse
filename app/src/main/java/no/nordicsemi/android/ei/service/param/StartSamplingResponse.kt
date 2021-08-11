package no.nordicsemi.android.ei.service.param

/**
 * Start sampling response
 * @see https://docs.edgeimpulse.com/reference#startsampling
 *
 * @param success   Whether the option succeeded.
 * @param error     Optional error description (set if 'success' was false).
 * @param id        Sampling ID.
 */
data class StartSamplingResponse(
    val success: Boolean,
    val error: String?,
    val id: Int?
)
