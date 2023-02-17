/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.service.param

/**
 * Body parameters for "Create New Project" request.
 *
 * @see <a href="https://docs.edgeimpulse.com/reference#createproject</a>
 */
data class CreateProjectRequest(
    /** Project name */
    val projectName: String
)
