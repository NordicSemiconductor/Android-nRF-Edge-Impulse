/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.service.param

/**
 * Delete device response.
 *
 * @see https://docs.edgeimpulse.com/reference#deletedevice
 */
data class DeleteDeviceResponse(
    val success: Boolean = false,
    val error: String?
)
