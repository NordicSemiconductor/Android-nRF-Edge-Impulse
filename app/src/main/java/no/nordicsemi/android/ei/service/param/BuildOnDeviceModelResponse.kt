/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.service.param

/**
 * Response to build on-device model job.
 * @see https://docs.edgeimpulse.com/reference#buildondevicemodeljob
 */
data class BuildOnDeviceModelResponse(val success: Boolean, val error: String?, val id: Int)