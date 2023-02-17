/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.service.param

import no.nordicsemi.android.ei.model.DevelopmentKeys

/**
 * Response body for a GetDevelopmentKeysRequest
 */
data class DevelopmentKeysResponse(
    val apiKey: String = "undefined",
    val hmacKey: String = "undefined",
    val success: Boolean,
    val error: String,
)

fun DevelopmentKeysResponse.developmentKeys() = DevelopmentKeys(
    apiKey = apiKey,
    hmacKey = hmacKey,
)