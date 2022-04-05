package no.nordicsemi.android.ei.service.param

/**
 * Set the current name for a device.
 * @see https://docs.edgeimpulse.com/reference#renamedevice
 *
 * @param name  Device name
 */
data class RenameDeviceRequest(
    val name: String
)
