/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.service.param

/**
 * Create new project response.
 *
 * @see <a href="https://docs.edgeimpulse.com/reference#createproject">Docs: Create new project</a>
 */
data class CreateProjectResponse(
    val id: Int,
    val success: Boolean = false,
    val error: String?
)
