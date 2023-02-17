/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.service.param

import no.nordicsemi.android.ei.model.Device

/**
 * Retrieve list of devices for a project.
 *
 * @see <a href="https://docs.edgeimpulse.com/reference#listdevices">Docs: List devices</a>
 */
data class ListDevicesResponse(
    val devices: List<Device>,
    val success: Boolean = false,
    val error: String?
)
