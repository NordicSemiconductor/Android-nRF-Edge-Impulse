package no.nordicsemi.android.ei.service.param

/**
 * Response for RenameDeviceRequest
 * @see https://docs.edgeimpulse.com/reference#renamedevice
 *
 * @param success   Whether the option succeeded.
 * @param error     Optional error description (set if 'success' was false).
 */
data class RenameDeviceResponse(
    val success: Boolean,
    val error: String?,
)
