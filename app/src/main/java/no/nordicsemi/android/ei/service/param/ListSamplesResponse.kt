/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.service.param

import no.nordicsemi.android.ei.model.Sample

/**
 * Retrieve Retrieve all raw data by category.
 *
 * @see <a href="https://docs.edgeimpulse.com/reference#listsamples">Docs: List Sample</a>
 */
data class ListSamplesResponse(
    val success: Boolean = false,
    val samples: List<Sample>,
    val error: String?
)
